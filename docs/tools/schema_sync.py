# -*- coding: utf-8 -*-
"""ASAK 스키마 문서 동기화 도구.

운영 DB(asak_db)의 실제 DDL 을 읽어 docs/아삭_mysql.sql(테이블)과
docs/view.sql(뷰)이 실제와 맞는지 대조하고, 어긋나면 갱신한다.

읽기 전용이다. SELECT / SHOW / DESCRIBE 외의 SQL 은 실행 자체를 막아둔다.
DB 는 팀 공용이므로 이 도구로 스키마를 바꾸지 않는다.

사용법
    python docs/tools/schema_sync.py dump      # 실제 DDL 을 out/ 에 덤프
    python docs/tools/schema_sync.py diff      # 문서 vs 실제 대조표 생성
    python docs/tools/schema_sync.py sync      # 아삭_mysql.sql 을 실제 기준으로 재생성
    python docs/tools/schema_sync.py verify    # 문서와 실제가 같은지 검증
    python docs/tools/schema_sync.py all       # dump -> diff -> verify (sync 는 제외)

    python docs/tools/schema_sync.py sync --write   # sync 는 --write 없으면 미리보기만

필요 조건
    pip install pymysql
    저장소 루트에 .env (DB_URL / DB_USERNAME / DB_PASSWORD)

Windows 콘솔에서 한글이 깨지면 PYTHONIOENCODING=utf-8 을 주고 실행한다.
"""

import argparse
import difflib
import io
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
DOCS = os.path.dirname(HERE)
ROOT = os.path.dirname(DOCS)
ENV_PATH = os.path.join(ROOT, ".env")
TABLE_DOC = os.path.join(DOCS, "아삭_mysql.sql")
VIEW_DOC = os.path.join(DOCS, "view.sql")
OUT_DIR = os.path.join(HERE, "out")

COLLATION = "utf8mb4_unicode_ci"

# SHOW CREATE 가 내놓는 타입 중 문서에서 대문자로 적는 것들
TYPE_KEYWORDS = [
    "bigint", "int", "smallint", "tinyint", "varchar", "char", "text",
    "timestamp", "datetime", "date", "decimal", "double", "float", "json",
]


# ---------------------------------------------------------------- 공통


def log(msg):
    print(msg)


def is_backup(name):
    """일회성 백업 테이블은 문서 대상이 아니다."""
    return name.startswith("backup_") or "_backup_" in name


def read_env():
    if not os.path.exists(ENV_PATH):
        sys.exit("ERROR: .env 를 찾을 수 없다: %s" % ENV_PATH)
    env = {}
    with io.open(ENV_PATH, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            env[key.strip()] = value.strip()
    for required in ("DB_URL", "DB_USERNAME", "DB_PASSWORD"):
        if required not in env:
            sys.exit("ERROR: .env 에 %s 가 없다" % required)
    return env


class ReadOnlyDB(object):
    """SELECT / SHOW / DESCRIBE 만 허용하는 커넥션 래퍼."""

    ALLOWED = ("select", "show", "describe", "desc", "explain")

    def __init__(self):
        env = read_env()
        matched = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", env["DB_URL"])
        if not matched:
            sys.exit("ERROR: DB_URL 형식을 해석할 수 없다: %s" % env["DB_URL"])
        self.host, self.port, self.db = (
            matched.group(1), int(matched.group(2)), matched.group(3))
        try:
            import pymysql
        except ImportError:
            sys.exit("ERROR: pymysql 이 필요하다.  pip install pymysql")
        self.pymysql = pymysql
        self.conn = pymysql.connect(
            host=self.host, port=self.port, user=env["DB_USERNAME"],
            password=env["DB_PASSWORD"], database=self.db, charset="utf8mb4")

    def query(self, sql, args=None, as_dict=False):
        head = sql.lstrip().split(None, 1)[0].lower()
        if head not in self.ALLOWED:
            raise RuntimeError("읽기 전용 도구다. 허용되지 않는 SQL: %s" % head)
        cursor_type = self.pymysql.cursors.DictCursor if as_dict else None
        cur = self.conn.cursor(cursor_type) if cursor_type else self.conn.cursor()
        try:
            cur.execute(sql, args)
            return cur.fetchall()
        finally:
            cur.close()

    def close(self):
        self.conn.rollback()
        self.conn.close()


def ensure_out_dir():
    if not os.path.isdir(OUT_DIR):
        os.makedirs(OUT_DIR)


# ---------------------------------------------------------------- dump


def cmd_dump(args):
    """실제 테이블/뷰 정의를 out/ 에 덤프한다."""
    ensure_out_dir()
    db = ReadOnlyDB()
    log("접속: %s:%s/%s (읽기 전용)" % (db.host, db.port, db.db))

    objects = db.query(
        "SELECT TABLE_NAME, TABLE_TYPE FROM information_schema.TABLES "
        "WHERE TABLE_SCHEMA=%s ORDER BY TABLE_NAME", (db.db,))
    tables = [n for n, t in objects if t == "BASE TABLE" and not is_backup(n)]
    backups = [n for n, t in objects if t == "BASE TABLE" and is_backup(n)]
    views = [n for n, t in objects if t == "VIEW"]

    parts = []
    for name in tables:
        ddl = db.query("SHOW CREATE TABLE `%s`" % name)[0][1]
        parts.append("-- ===== TABLE %s =====\n%s;" % (name, ddl))
    write_text(os.path.join(OUT_DIR, "tables.sql"), "\n\n".join(parts) + "\n")

    parts = []
    view_meta = {}
    columns = db.query(
        "SELECT TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, COLUMN_TYPE, IS_NULLABLE "
        "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=%s "
        "ORDER BY TABLE_NAME, ORDINAL_POSITION", (db.db,), as_dict=True)
    by_table = {}
    for row in columns:
        by_table.setdefault(row["TABLE_NAME"], []).append(row)

    view_rows = db.query(
        "SELECT TABLE_NAME, DEFINER, SECURITY_TYPE FROM information_schema.VIEWS "
        "WHERE TABLE_SCHEMA=%s ORDER BY TABLE_NAME", (db.db,), as_dict=True)
    security = {r["TABLE_NAME"]: (r["DEFINER"], r["SECURITY_TYPE"]) for r in view_rows}

    for name in views:
        ddl = db.query("SHOW CREATE VIEW `%s`" % name)[0][1]
        parts.append("-- ===== VIEW %s =====\n%s;" % (name, ddl))
        definer, sec = security.get(name, ("?", "?"))
        view_meta[name] = {
            "definer": definer,
            "security": sec,
            "columns": [(c["COLUMN_NAME"], c["COLUMN_TYPE"], c["IS_NULLABLE"])
                        for c in by_table.get(name, [])],
        }
    write_text(os.path.join(OUT_DIR, "views.sql"), "\n\n".join(parts) + "\n")
    write_json(os.path.join(OUT_DIR, "views.json"), view_meta)
    write_json(os.path.join(OUT_DIR, "inventory.json"),
               {"database": db.db, "tables": tables, "views": views, "backups": backups})

    db.close()
    log("덤프 완료: 테이블 %d개, 뷰 %d개 (백업 테이블 %d개 제외)"
        % (len(tables), len(views), len(backups)))
    log("  -> %s" % OUT_DIR)


def write_text(path, text):
    with io.open(path, "w", encoding="utf-8") as f:
        f.write(text)


def write_json(path, obj):
    with io.open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(obj, ensure_ascii=False, indent=2))


def read_text(path):
    with io.open(path, encoding="utf-8") as f:
        return f.read()


def require_dump():
    needed = ["tables.sql", "views.sql", "views.json", "inventory.json"]
    missing = [n for n in needed if not os.path.exists(os.path.join(OUT_DIR, n))]
    if missing:
        sys.exit("ERROR: 먼저 dump 를 실행할 것 (없는 파일: %s)" % ", ".join(missing))


# ---------------------------------------------------------------- 파싱


def parse_actual_tables():
    """out/tables.sql 의 SHOW CREATE TABLE 을 {이름: [줄]} 로 파싱."""
    text = read_text(os.path.join(OUT_DIR, "tables.sql"))
    result = {}
    pattern = (r"-- ===== TABLE (\w+) =====\nCREATE TABLE `\w+` \((.*?)\n\) "
               r"ENGINE=(\w+) (?:AUTO_INCREMENT=\d+ )?DEFAULT CHARSET=(\w+) COLLATE=(\w+);")
    for name, body, engine, charset, collate in re.findall(pattern, text, re.S):
        lines = []
        for line in body.split("\n"):
            line = line.strip().rstrip(",")
            if not line:
                continue
            lines.append(strip_column_collation(line, collate))
        result[name] = {"lines": lines, "engine": engine,
                        "charset": charset, "collate": collate}
    return result


def strip_column_collation(line, collate):
    """컬럼마다 붙는 COLLATE 는 테이블 기본값과 같으므로 지운다."""
    return re.sub(r"\s*(CHARACTER SET \w+\s*)?COLLATE " + collate, "", line)


def parse_doc_tables():
    """docs/아삭_mysql.sql 을 파싱해 테이블 본문과 FK 를 합쳐 실제 형태로 복원."""
    text = read_text(TABLE_DOC)
    tables = {}
    for name, body in re.findall(r"CREATE TABLE `(\w+)` \((.*?)\n\) ENGINE", text, re.S):
        tables[name] = [l.strip().rstrip(",") for l in body.split("\n") if l.strip()]

    fk_pattern = (r"ALTER TABLE `(\w+)` ADD CONSTRAINT `(\w+)`\s*\n\s*"
                  r"FOREIGN KEY \((.+?)\) REFERENCES `(\w+)` \((.+?)\);")
    for table, name, cols, ref_table, ref_cols in re.findall(fk_pattern, text):
        if table in tables:
            tables[table].append(
                "CONSTRAINT `%s` FOREIGN KEY (%s) REFERENCES `%s` (%s)"
                % (name, cols, ref_table, ref_cols))
    return tables


def split_table_lines(lines):
    """테이블 정의 줄을 컬럼 / 키·인덱스 / FK 로 나눈다."""
    columns, keys, foreign = [], [], []
    for line in lines:
        if line.startswith("CONSTRAINT"):
            foreign.append(line)
        elif line.startswith(("PRIMARY KEY", "UNIQUE KEY", "KEY", "FULLTEXT KEY")):
            keys.append(line)
        else:
            columns.append(line.lower())
    return columns, keys, sorted(foreign)


VIEW_BODY_RE = (r"CREATE (?:OR REPLACE )?(?:ALGORITHM=\w+ )?"
                r"(?:DEFINER=`[^`]*`@`[^`]*` )?(?:SQL SECURITY \w+ )?"
                r"VIEW `(\w+)` AS\s*(.*?);\s*(?=\n--|\nCREATE|\Z)")


def parse_actual_views():
    text = read_text(os.path.join(OUT_DIR, "views.sql"))
    result = {}
    for match in re.finditer(r"-- ===== VIEW (\w+) =====\n(.*?);\n", text, re.S):
        name, ddl = match.group(1), match.group(2)
        body = re.split(r"\bVIEW `" + name + r"` AS\b", ddl, maxsplit=1)[1]
        result[name] = body.strip()
    return result


def parse_doc_views():
    text = read_text(VIEW_DOC)
    return {m.group(1): m.group(2).strip()
            for m in re.finditer(VIEW_BODY_RE, text, re.S)}


def normalize_sql(sql):
    """문자열 리터럴은 보존하고 키워드는 소문자화, 공백은 정규화."""
    literals = []

    def stash(match):
        literals.append(match.group(0))
        return "\x00%d\x00" % (len(literals) - 1)

    text = re.sub(r"'[^']*'", stash, sql)
    text = text.lower()
    text = re.sub(r"\s+", " ", text)
    text = re.sub(r"\s*([(),])\s*", r"\1", text)
    text = text.strip().rstrip(";").strip()
    for index, literal in enumerate(literals):
        text = text.replace("\x00%d\x00" % index, literal)
    return text


def sql_tokens(sql):
    return re.findall(r"`[^`]+`|'[^']*'|[A-Za-z_][A-Za-z0-9_]*|[0-9]+|[^ ]", sql)


def paren_only_difference(left_tokens, right_tokens):
    """두 토큰열의 차이가 괄호뿐인지 판정한다.

    MySQL 은 조인 트리에 중첩 괄호를 붙여 저장하고 view.sql 은 가독성을 위해 그것을
    생략한다. 그런 차이는 의미가 같다. 반면 리터럴이나 식별자가 다르면 정의가 다른 것이며,
    현재 데이터에서 실행 결과가 우연히 같더라도 같다고 판정해서는 안 된다.
    """
    matcher = difflib.SequenceMatcher(None, left_tokens, right_tokens, autojunk=False)
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag == "equal":
            continue
        for token in left_tokens[i1:i2] + right_tokens[j1:j2]:
            if token not in ("(", ")"):
                return False
    return True


# ---------------------------------------------------------------- diff


def cmd_diff(args):
    """문서 vs 실제 대조표를 만든다."""
    require_dump()
    inventory = json.loads(read_text(os.path.join(OUT_DIR, "inventory.json")))
    view_meta = json.loads(read_text(os.path.join(OUT_DIR, "views.json")))

    lines = []
    add = lines.append
    add("# 스키마 문서 vs 실제 %s 대조표" % inventory["database"])
    add("")
    add("대상: `docs/아삭_mysql.sql`(테이블), `docs/view.sql`(뷰)")
    add("생성: `python docs/tools/schema_sync.py diff`")
    add("")
    add("---")
    add("")

    table_diff_count = build_table_diff(add, inventory)
    add("")
    add("---")
    add("")
    view_diff_count = build_view_diff(add, view_meta)

    ensure_out_dir()
    path = os.path.join(OUT_DIR, "drift_report.md")
    write_text(path, "\n".join(lines) + "\n")
    log("대조표 생성: %s" % path)
    log("  테이블 차이 %d건 / 뷰 표기 차이 %d건" % (table_diff_count, view_diff_count))
    if view_diff_count:
        log("  (뷰 표기 차이는 괄호 등 표기만일 수 있다. verify 로 의미까지 확인할 것)")
    return table_diff_count, view_diff_count


def build_table_diff(add, inventory):
    actual = parse_actual_tables()
    doc = parse_doc_tables()

    add("# 1부. 테이블 — `docs/아삭_mysql.sql`")
    add("")
    add("- 실제 운영 테이블 %d개 / 문서 기재 %d개 (백업 테이블 %d개는 대상 아님)"
        % (len(actual), len(doc), len(inventory["backups"])))
    add("")

    missing = [t for t in sorted(actual) if t not in doc]
    extra = [t for t in sorted(doc) if t not in actual]
    add("## 1-1. 테이블 존재 차이")
    add("")
    add("- 문서에 없는 실제 테이블: %s"
        % (", ".join("`%s`" % t for t in missing) or "없음"))
    add("- 실제에 없는 문서 테이블: %s"
        % (", ".join("`%s`" % t for t in extra) or "없음"))
    add("")
    add("## 1-2. 테이블별 정의 차이")

    total = len(missing) + len(extra)
    for name in sorted(actual):
        if name not in doc:
            add("")
            add("### `%s` — 문서에 없음" % name)
            continue
        a_cols, a_keys, a_fks = split_table_lines(actual[name]["lines"])
        d_cols, d_keys, d_fks = split_table_lines(doc[name])
        blocks = [("컬럼", a_cols, d_cols), ("키·인덱스", a_keys, d_keys),
                  ("외래키", a_fks, d_fks)]
        issues = []
        for label, left, right in blocks:
            if left == right:
                continue
            for line in difflib.unified_diff(
                    left, right, "실제-" + label, "문서-" + label, lineterm="", n=0):
                if line.startswith(("---", "+++", "@@")):
                    continue
                mark = "실제에만" if line.startswith("-") else "문서에만"
                issues.append("- %s (%s): `%s`" % (mark, label, line[1:].strip()))
        if issues:
            total += len(issues)
            add("")
            add("### `%s` — 차이 %d건" % (name, len(issues)))
            for issue in issues:
                add(issue)

    if total == 0:
        add("")
        add("차이 없음. 문서가 실제와 일치한다.")
    return total


def build_view_diff(add, view_meta):
    actual = parse_actual_views()
    doc = parse_doc_views()

    add("# 2부. 뷰 — `docs/view.sql`")
    add("")
    add("- 실제 뷰 %d개 / 문서 기재 %d개" % (len(actual), len(doc)))
    add("")

    missing = [v for v in sorted(actual) if v not in doc]
    extra = [v for v in sorted(doc) if v not in actual]
    add("## 2-1. 뷰 존재 차이")
    add("")
    add("- 문서에 없는 실제 뷰: %s"
        % (", ".join("`%s`" % v for v in missing) or "없음"))
    add("- 실제에 없는 문서 뷰: %s"
        % (", ".join("`%s`" % v for v in extra) or "없음"))
    add("")
    add("## 2-2. 정의 차이")
    add("")
    add("`SHOW CREATE VIEW` 본문을 문자열 리터럴 보존 + 키워드 소문자화 + 공백")
    add("정규화한 뒤 토큰 단위로 비교한다.")

    total = len(missing) + len(extra)
    identical, differing = [], []
    for name in sorted(actual):
        if name not in doc:
            continue
        left = normalize_sql(actual[name])
        right = normalize_sql(doc[name])
        if left == right:
            identical.append(name)
            continue
        differing.append(name)
        left_tokens, right_tokens = sql_tokens(left), sql_tokens(right)
        matcher = difflib.SequenceMatcher(None, left_tokens, right_tokens, autojunk=False)
        add("")
        add("### `%s` — 정의 불일치" % name)
        for tag, i1, i2, j1, j2 in matcher.get_opcodes():
            if tag == "equal":
                continue
            total += 1
            context = " ".join(left_tokens[max(0, i1 - 6):i1])
            add("- 앞 문맥: `%s`" % context)
            add("  - 실제: `%s`" % (" ".join(left_tokens[i1:i2]) or "(없음)"))
            add("  - 문서: `%s`" % (" ".join(right_tokens[j1:j2]) or "(없음)"))

    add("")
    add("## 2-3. 요약")
    add("")
    add("- 완전 일치 %d개" % len(identical))
    add("- 불일치 %d개: %s"
        % (len(differing), ", ".join("`%s`" % v for v in differing) or "없음"))
    if differing:
        add("")
        add("괄호 표기만 다른 경우가 있다. 의미까지 같은지 확인하려면 verify 를 실행한다.")
    add("")
    add("## 2-4. 실제 뷰 컬럼 (참고)")
    add("")
    add("| 뷰 | 컬럼 수 | 컬럼 |")
    add("|---|---|---|")
    for name in sorted(view_meta):
        cols = [c[0] for c in view_meta[name]["columns"]]
        add("| `%s` | %d | %s |" % (name, len(cols), ", ".join(cols)))
    return total


# ---------------------------------------------------------------- sync


def uppercase_type(token):
    matched = re.match(r"([a-z]+)(\(.*?\))?(.*)$", token)
    if not matched or matched.group(1) not in TYPE_KEYWORDS:
        return token
    return matched.group(1).upper() + (matched.group(2) or "") + matched.group(3)


def cmd_sync(args):
    """실제 DDL 기준으로 docs/아삭_mysql.sql 을 재생성한다."""
    require_dump()
    actual = parse_actual_tables()

    table_blocks = []
    foreign_keys = []
    for name in sorted(actual):
        info = actual[name]
        body = []
        for line in info["lines"]:
            fk = re.match(
                r"CONSTRAINT `(\w+)` FOREIGN KEY \((.+?)\) REFERENCES `(\w+)` \((.+?)\)(.*)$",
                line)
            if fk:
                foreign_keys.append(
                    (name, fk.group(1), fk.group(2), fk.group(3),
                     fk.group(4), fk.group(5).strip()))
                continue
            column = re.match(r"`(\w+)` (.+)$", line)
            if column:
                parts = column.group(2).split(" ", 1)
                rest = uppercase_type(parts[0])
                if len(parts) > 1:
                    rest += " " + parts[1]
                body.append("    `%s` %s," % (column.group(1), rest))
            else:
                body.append("    %s," % line)
        body[-1] = body[-1].rstrip(",")
        table_blocks.append(
            "CREATE TABLE `%s` (\n%s\n) ENGINE=%s DEFAULT CHARSET=%s COLLATE=%s;"
            % (name, "\n".join(body), info["engine"], info["charset"], info["collate"]))

    lines = []
    add = lines.append
    add("-- =============================================")
    add("-- Project: 아삭")
    add("-- DBMS: MySQL 8")
    add("-- Target/model charset: utf8mb4")
    add("-- SQL file encoding: UTF-8")
    add("-- Source: 운영 DB(asak_db) SHOW CREATE TABLE 실측")
    add("-- Synced: %s" % args.date)
    add("-- Generated by: docs/tools/schema_sync.py sync --write")
    add("--")
    add("-- 주의")
    add("--  * 이 파일은 운영 DB 실제 DDL 을 그대로 옮긴 정본이다. 손으로 고치지 말고,")
    add("--    스키마가 바뀌면 schema_sync.py 로 다시 생성한다.")
    add("--  * AUTO_INCREMENT 현재값은 런타임 값이라 기록하지 않는다.")
    add("--  * 컬럼 단위 COLLATE 는 테이블 기본값(utf8mb4_unicode_ci)과 같아 생략했다.")
    add("--  * 뷰(vw_*) 정의는 docs/view.sql 에 있다.")
    add("--  * backup_* / *_backup_* 테이블은 일회성 백업본이라 여기 포함하지 않는다.")
    add("-- =============================================")
    add("")
    add("SET NAMES 'utf8mb4';")
    add("SET FOREIGN_KEY_CHECKS = 0;")
    add("")
    add("-- =============================================")
    add("-- Tables")
    add("-- =============================================")
    add("")
    add("\n\n\n".join(table_blocks))
    add("")
    add("")
    add("-- =============================================")
    add("-- Foreign Key Constraints")
    add("-- =============================================")
    add("")
    for table, name, cols, ref_table, ref_cols, extra in foreign_keys:
        add("ALTER TABLE `%s` ADD CONSTRAINT `%s`" % (table, name))
        add("    FOREIGN KEY (%s) REFERENCES `%s` (%s)%s;"
            % (cols, ref_table, ref_cols, (" " + extra) if extra else ""))
        add("")
    add("")
    add("SET FOREIGN_KEY_CHECKS = 1;")
    add("")
    add("-- =============================================")
    add("-- End of script")
    add("-- =============================================")

    content = "\n".join(lines) + "\n"
    if args.write:
        write_text(TABLE_DOC, content)
        log("갱신: %s (테이블 %d개, FK %d개)"
            % (TABLE_DOC, len(table_blocks), len(foreign_keys)))
    else:
        preview = os.path.join(OUT_DIR, "아삭_mysql.sql.new")
        ensure_out_dir()
        write_text(preview, content)
        log("미리보기 생성: %s" % preview)
        log("문서에 반영하려면 --write 를 붙여 다시 실행한다.")


# ---------------------------------------------------------------- verify


def cmd_verify(args):
    """문서와 실제가 실제로 같은지 검증한다."""
    require_dump()
    ok = True

    log("[테이블] 문서를 SHOW CREATE 형태로 되돌려 실제와 비교")
    actual = parse_actual_tables()
    doc = parse_doc_tables()
    mismatch = 0
    for name in sorted(actual):
        if name not in doc:
            log("  !! 문서에 없음: %s" % name)
            mismatch += 1
            continue
        if split_table_lines(actual[name]["lines"]) != split_table_lines(doc[name]):
            log("  !! 불일치: %s" % name)
            mismatch += 1
    for name in sorted(doc):
        if name not in actual:
            log("  !! 실제에 없음: %s" % name)
            mismatch += 1
    log("  결과: %s (테이블 %d개)"
        % ("완전 일치" if mismatch == 0 else "%d건 불일치" % mismatch, len(actual)))
    ok = ok and mismatch == 0

    log("[뷰] 정의 토큰 비교")
    actual_views = parse_actual_views()
    doc_views = parse_doc_views()
    cosmetic, semantic = [], []
    for name in sorted(actual_views):
        if name not in doc_views:
            log("  !! 문서에 없음: %s" % name)
            ok = False
            continue
        left = normalize_sql(actual_views[name])
        right = normalize_sql(doc_views[name])
        if left == right:
            continue
        if paren_only_difference(sql_tokens(left), sql_tokens(right)):
            cosmetic.append(name)
        else:
            semantic.append(name)
    log("  결과: 일치 %d개 / 괄호 표기 차이 %d개 / 정의 차이 %d개"
        % (len(actual_views) - len(cosmetic) - len(semantic), len(cosmetic), len(semantic)))

    for name in semantic:
        log("  !! 정의가 다르다: %s (괄호 외 토큰이 다름)" % name)
        log("     실행 결과가 현재 데이터에서 같더라도 같다고 보지 않는다. diff 로 내용 확인할 것")
        ok = False

    if cosmetic:
        log("[뷰] 괄호만 다른 뷰는 실제로 실행해 결과가 같은지 확인")
        ok = verify_view_equivalence(cosmetic, doc_views) and ok

    log("")
    log("최종: %s" % ("문서가 실제와 일치한다" if ok else "차이가 있다. diff 를 실행할 것"))
    return 0 if ok else 1


def verify_view_equivalence(names, doc_views):
    """뷰와 문서 SQL 을 각각 실행해 행 수·체크섬·컬럼 순서를 비교한다."""
    view_meta = json.loads(read_text(os.path.join(OUT_DIR, "views.json")))
    db = ReadOnlyDB()
    sentinel = "~NULL~"
    all_same = True
    try:
        for name in names:
            columns = [c[0] for c in view_meta[name]["columns"]]
            expr = ", ".join(
                "COALESCE(CAST(`%s` AS CHAR), '%s')" % (c, sentinel) for c in columns)
            agg = ("SELECT COUNT(*), BIT_XOR(CRC32(CONCAT_WS('|', %s))), "
                   "SUM(CRC32(CONCAT_WS('|', %s)))" % (expr, expr))
            live = db.query("%s FROM `%s` t" % (agg, name))[0]
            documented = db.query("%s FROM (%s) t" % (agg, doc_views[name]))[0]
            same = live == documented
            all_same = all_same and same
            log("  %s %s — 행 %s/%s, 체크섬 %s"
                % ("OK " if same else "!! ", name, live[0], documented[0],
                   "동일" if live[1:] == documented[1:] else "다름"))
    finally:
        db.close()
    return all_same


# ---------------------------------------------------------------- main


def cmd_all(args):
    cmd_dump(args)
    log("")
    cmd_diff(args)
    log("")
    return cmd_verify(args)


def main():
    parser = argparse.ArgumentParser(
        description="ASAK 스키마 문서 동기화 도구 (읽기 전용)")
    sub = parser.add_subparsers(dest="command")

    sub.add_parser("dump", help="실제 DDL 을 out/ 에 덤프")
    sub.add_parser("diff", help="문서 vs 실제 대조표 생성")
    sync = sub.add_parser("sync", help="아삭_mysql.sql 을 실제 기준으로 재생성")
    sync.add_argument("--write", action="store_true",
                      help="문서에 직접 반영 (없으면 out/ 에 미리보기만)")
    sync.add_argument("--date", default=None,
                      help="헤더에 적을 동기화 날짜 (기본: 오늘, YYYY-MM-DD)")
    sub.add_parser("verify", help="문서와 실제가 같은지 검증")
    sub.add_parser("all", help="dump -> diff -> verify")

    args = parser.parse_args()
    if not args.command:
        parser.print_help()
        return 0
    if not hasattr(args, "write"):
        args.write = False
    if not getattr(args, "date", None):
        import datetime
        args.date = datetime.date.today().isoformat()

    handlers = {"dump": cmd_dump, "diff": cmd_diff, "sync": cmd_sync,
                "verify": cmd_verify, "all": cmd_all}
    result = handlers[args.command](args)
    return result if isinstance(result, int) else 0


if __name__ == "__main__":
    sys.exit(main())
