#!/usr/bin/env python3
"""Scan MyBatis mapper XML files for known formatter corruption patterns."""
from __future__ import annotations

import re
import sys
from pathlib import Path

PATTERNS: list[tuple[str, str]] = [
    ("broken_neq", r"&lt; >"),
    ("broken_lte", r"&lt; ="),
    ("broken_gte", r"&gt; ="),
    ("lowercase_resultmap", r"<resultmap\b|</resultmap>"),
    ("corrupt_datefmt", r"%d\?\?"),
    ("broken_limit", r"#\{filter\.lim\s"),
    ("broken_offset", r"#\{filter\.off\s"),
    ("group_concat_split", r"GROUP_CONCAT\([^)]*\n\s+ORDER BY"),
    ("unescaped_lt_in_if", r"<if[^>]*>[^<]*(?<!&)(?<!&lt;) < #"),
    ("unescaped_gte_in_if", r"<if[^>]*>[^<]*(?<!&)(?<!&gt;)>= #"),
    ("mojibake_menu_summary", r"' [^']*\?[^']*'"),
]

MAPPER_DIR = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "mappers"


def audit_file(path: Path) -> list[str]:
    raw = path.read_bytes()
    issues: list[str] = []
    if b"\x00" in raw:
        issues.append("null_bytes")

    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        issues.append(f"encoding_error:{exc.start}")
        text = raw.decode("utf-8", errors="replace")

    for name, pattern in PATTERNS:
        if re.search(pattern, text):
            issues.append(name)

    return issues


def main() -> int:
    mapper_dir = MAPPER_DIR
    if len(sys.argv) > 1:
        mapper_dir = Path(sys.argv[1])

    any_issue = False
    for path in sorted(mapper_dir.glob("*.xml")):
        issues = audit_file(path)
        if issues:
            any_issue = True
            print(f"{path.name}: {', '.join(issues)}")

    if not any_issue:
        print("All mapper files passed corruption pattern scan.")
    return 1 if any_issue else 0


if __name__ == "__main__":
    raise SystemExit(main())
