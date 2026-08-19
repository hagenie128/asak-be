# -*- coding: utf-8 -*-
"""결측 영양정보·재료량 채우기 (2026-08-19).

근거
  * menu_nutr  <- 영양성분표 PDF nutrition_260813 (파싱 결과 JSON)
  * menu_ing   <- ing_nutr.serving_g (재료 표준 제공량)

원칙
  * NULL 인 컬럼만 채운다. 기존 값은 어떤 경우에도 덮지 않는다.
  * 실행 전에 대상 테이블을 백업 테이블로 통째 복사한다.
  * --apply 없이 실행하면 무엇을 바꿀지 출력만 하고 끝난다(기본값).

사용법
    python docs/tools/fill_nutrition.py            # 미리보기
    python docs/tools/fill_nutrition.py --apply    # 실제 반영
"""

import argparse
import io
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
ENV_PATH = os.path.join(ROOT, ".env")

NUTR_FIELDS = ["serving_g", "kcal", "carb_g", "sugar_g",
               "protein_g", "fat_g", "saturated_fat_g", "sodium_mg"]

BACKUP_NUTR = "backup_20260819_menu_nutr_before_pdf_fill"
BACKUP_ING = "backup_20260819_menu_ing_before_serving_g"

# 생수는 PDF 에 없다. 물이므로 제공량만 기록하고 영양성분은 비워 둔다.
WATER_MENU_PATTERN = re.compile(r"^생수\s*(\d+)\s*ml", re.I)


def read_env():
    env = {}
    with io.open(ENV_PATH, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                env[k.strip()] = v.strip()
    return env


def connect():
    env = read_env()
    m = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", env["DB_URL"])
    import pymysql
    return pymysql.connect(host=m.group(1), port=int(m.group(2)),
                           user=env["DB_USERNAME"], password=env["DB_PASSWORD"],
                           database=m.group(3), charset="utf8mb4", autocommit=False)


def norm(s):
    s = re.sub(r"\[[^\]]*\]", "", s or "")
    s = re.sub(r"(NEW MENU|NEW)", "", s)
    s = re.sub(r"\d+\s*(ml|ML)\b", "", s)
    s = re.sub(r"착즙\s*주스", "", s)
    s = re.sub(r"\((HOT|ICE)\)", "", s)
    return re.sub(r"[\s\-_()\[\]*.,]+", "", s).lower()


def bagkey(s):
    return "".join(sorted(norm(s)))


def load_pdf(path):
    items = json.load(io.open(path, encoding="utf-8"))
    exact, bags = {}, {}
    for x in items:
        exact.setdefault(norm(x["name"]), x)
        bags.setdefault(bagkey(x["name"]), x)
    return exact, bags


def build_plan(cur, exact, bags):
    """(menu_nutr 갱신 계획, menu_ing 갱신 계획) 을 만든다."""
    cur.execute(
        "SELECT m.id, m.name, n.serving_g, n.kcal, n.carb_g, n.sugar_g, "
        "       n.protein_g, n.fat_g, n.saturated_fat_g, n.sodium_mg "
        "FROM menu m JOIN menu_nutr n ON n.menu_id=m.id WHERE m.deleted_at IS NULL")
    nutr_plan = []
    for row in cur.fetchall():
        menu_id, name = row[0], row[1]
        water = WATER_MENU_PATTERN.match(name or "")
        source = exact.get(norm(name)) or bags.get(bagkey(name))
        if water and not source:
            if row[2] is None:                       # serving_g 만 기록
                nutr_plan.append((menu_id, name, "생수(중량만)",
                                  {"serving_g": float(water.group(1))}))
            continue
        if not source:
            continue
        changes = {}
        for idx, field in enumerate(NUTR_FIELDS):
            if row[2 + idx] is None:
                changes[field] = source[field]
        if changes:
            nutr_plan.append((menu_id, name, source["name"], changes))

    cur.execute(
        "SELECT mi.id, m.name, i.name, n.serving_g "
        "FROM menu_ing mi "
        "JOIN ing_nutr n ON n.ing_id = mi.ing_id AND n.serving_g IS NOT NULL "
        "JOIN menu m ON m.id = mi.menu_id JOIN ing i ON i.id = mi.ing_id "
        "WHERE mi.quantity IS NULL")
    ing_plan = list(cur.fetchall())
    return nutr_plan, ing_plan


def backup(cur, source_table, backup_table):
    cur.execute("SELECT COUNT(*) FROM information_schema.TABLES "
                "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=%s", (backup_table,))
    if cur.fetchone()[0]:
        print("   백업 테이블이 이미 있다: %s (건너뜀)" % backup_table)
        return
    cur.execute("CREATE TABLE `%s` AS SELECT * FROM `%s`" % (backup_table, source_table))
    cur.execute("SELECT COUNT(*) FROM `%s`" % backup_table)
    print("   백업 생성: %s (%d행)" % (backup_table, cur.fetchone()[0]))


def main():
    ap = argparse.ArgumentParser(description="결측 영양정보·재료량 채우기")
    ap.add_argument("--pdf-json", default=os.path.join(HERE, "out", "pdf_new.json"),
                    help="영양성분표 PDF 파싱 결과 JSON")
    ap.add_argument("--apply", action="store_true", help="실제로 DB 에 반영")
    args = ap.parse_args()

    if not os.path.exists(args.pdf_json):
        sys.exit("ERROR: PDF 파싱 JSON 이 없다: %s" % args.pdf_json)
    exact, bags = load_pdf(args.pdf_json)

    conn = connect()
    cur = conn.cursor()
    nutr_plan, ing_plan = build_plan(cur, exact, bags)

    print("== menu_nutr: 결측을 채울 메뉴 %d개 ==" % len(nutr_plan))
    counts = {}
    for _, name, src, changes in nutr_plan:
        for f in changes:
            counts[f] = counts.get(f, 0) + 1
        print("   %-26s <- %-26s %s" % (name[:26], src[:26], ", ".join(sorted(changes))))
    print("   필드별: " + " · ".join("%s %d" % (f, counts.get(f, 0)) for f in NUTR_FIELDS))

    print("\n== menu_ing: quantity 를 ing_nutr.serving_g 로 채울 행 %d개 ==" % len(ing_plan))
    for _, menu, ing, g in ing_plan[:8]:
        print("   %-24s %-16s -> %s g" % (menu[:24], ing[:16], g))
    if len(ing_plan) > 8:
        print("   ... 외 %d행" % (len(ing_plan) - 8))

    if not args.apply:
        print("\n미리보기만 했다. 반영하려면 --apply 를 붙일 것.")
        conn.rollback(); conn.close()
        return 0

    print("\n== 백업 ==")
    backup(cur, "menu_nutr", BACKUP_NUTR)
    backup(cur, "menu_ing", BACKUP_ING)

    print("\n== 반영 ==")
    n_nutr = 0
    for menu_id, _, _, changes in nutr_plan:
        sets, vals = [], []
        for f, v in changes.items():
            sets.append("`%s` = %%s" % f)            # NULL 인 컬럼만 대상
            vals.append(v)
        vals.append(menu_id)
        cur.execute("UPDATE menu_nutr SET %s WHERE menu_id = %%s AND (%s)"
                    % (", ".join(sets),
                       " OR ".join("`%s` IS NULL" % f for f in changes)), vals)
        n_nutr += cur.rowcount
    print("   menu_nutr  %d행 갱신" % n_nutr)

    cur.execute(
        "UPDATE menu_ing mi "
        "JOIN ing_nutr n ON n.ing_id = mi.ing_id AND n.serving_g IS NOT NULL "
        "SET mi.quantity = n.serving_g "
        "WHERE mi.quantity IS NULL")
    print("   menu_ing   %d행 갱신" % cur.rowcount)

    conn.commit()
    print("\n커밋 완료. 되돌리려면 백업 테이블에서 복원한다:")
    print("   %s / %s" % (BACKUP_NUTR, BACKUP_ING))
    conn.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
