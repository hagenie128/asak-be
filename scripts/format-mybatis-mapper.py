#!/usr/bin/env python3
"""Format MyBatis mapper XML using AdminCommonCodeMapper conventions."""

from __future__ import annotations

import re
import sys
from pathlib import Path
from xml.sax.saxutils import escape as xml_escape

from lxml import etree

MAPPER_CHILD = "  "
ATTR_ALIGN = "          "
SQL_BASE = "      "
SQL_INNER = "          "
RESULT_CHILD = "    "

MAJOR_KEYWORDS = [
    "INSERT INTO",
    "DELETE FROM",
    "ORDER BY",
    "GROUP BY",
    "LEFT JOIN",
    "RIGHT JOIN",
    "INNER JOIN",
    "SELECT",
    "UPDATE",
    "FROM",
    "WHERE",
    "HAVING",
    "LIMIT",
    "OFFSET",
    "VALUES",
    "WITH",
    "SET",
    "JOIN",
]

CLAUSE_RE = re.compile(
    r"\b(INSERT INTO|DELETE FROM|ORDER BY|GROUP BY|LEFT JOIN|RIGHT JOIN|INNER JOIN|"
    r"SELECT|UPDATE|FROM|WHERE|HAVING|LIMIT|OFFSET|VALUES|WITH|SET)\b",
    re.IGNORECASE,
)


def sanitize_text(text: str) -> str:
    """Remove lone UTF-16 surrogates that Node/Cursor may pass via stdin."""
    return "".join(ch for ch in text if not (0xD800 <= ord(ch) <= 0xDFFF))


def read_stdin_text() -> str:
    raw = sys.stdin.buffer.read()
    return sanitize_text(raw.decode("utf-8", errors="replace"))


def write_stdout_text(text: str) -> None:
    sys.stdout.buffer.write(text.encode("utf-8"))


def is_inside_mybatis_expr(text: str, pos: int) -> bool:
    segment = text[:pos]
    return segment.count("#{") > segment.count("}")


def escape_xml_text(text: str) -> str:
    """Re-escape comparison operators after lxml decodes element text."""
    return xml_escape(text)


def sql_content_line(indent: str, text: str) -> str:
    return f"{indent}{escape_xml_text(text)}"


def local_tag(element: etree._Element) -> str:
    tag = element.tag
    if isinstance(tag, str) and "}" in tag:
        return tag.rsplit("}", 1)[-1]
    return str(tag)


def collapse_spaces(text: str) -> str:
    return re.sub(r"\s+", " ", text.strip())


def split_commas(text: str) -> list[str]:
    parts: list[str] = []
    current: list[str] = []
    depth = 0
    in_string = False
    quote = ""

    for char in text:
        if in_string:
            current.append(char)
            if char == quote:
                in_string = False
            continue
        if char in "\"'":
            in_string = True
            quote = char
            current.append(char)
            continue
        if char == "(":
            depth += 1
            current.append(char)
            continue
        if char == ")":
            depth -= 1
            current.append(char)
            continue
        if char == "," and depth == 0:
            part = "".join(current).strip()
            if part:
                parts.append(part)
            current = []
            continue
        current.append(char)

    part = "".join(current).strip()
    if part:
        parts.append(part)
    return parts


def split_joins(text: str) -> list[str]:
    parts = re.split(r"\s+(?=(?:LEFT JOIN|RIGHT JOIN|INNER JOIN|JOIN)\b)", text, flags=re.IGNORECASE)
    return [part.strip() for part in parts if part.strip()]


def split_where_conditions(text: str) -> list[str]:
    parts = re.split(r"\s+(AND|OR)\s+", text, flags=re.IGNORECASE)
    if len(parts) == 1:
        return [text.strip()]
    lines = [parts[0].strip()]
    index = 1
    while index < len(parts):
        lines.append(f"{parts[index].upper()} {parts[index + 1].strip()}")
        index += 2
    return lines


def format_case_item(item: str) -> list[str]:
    text = collapse_spaces(item)
    match = re.match(
        r"CASE\s+(WHEN\s+.+\s+THEN\s+.+\s+)(ELSE\s+.+\s+)(END(?:\s+AS\s+\S+)?)$",
        text,
        flags=re.IGNORECASE,
    )
    if not match:
        return [sql_content_line(SQL_INNER, text)]

    when_part = match.group(1).strip()
    else_part = match.group(2).strip()
    end_part = match.group(3).strip()
    inner = SQL_INNER + "    "
    return [
        f"{SQL_INNER}CASE",
        sql_content_line(inner, when_part),
        sql_content_line(inner, else_part),
        sql_content_line(SQL_INNER, end_part),
    ]


def format_clause_body(keyword: str, body: str) -> list[str]:
    keyword = keyword.upper()
    body = body.strip()
    if not body:
        return []

    if keyword == "SELECT":
        items = split_commas(body)
        lines: list[str] = []
        for index, item in enumerate(items):
            if item.upper().startswith("CASE"):
                case_lines = format_case_item(item)
                if index < len(items) - 1:
                    case_lines[-1] = case_lines[-1] + ","
                lines.extend(case_lines)
            else:
                suffix = "," if index < len(items) - 1 else ""
                lines.append(sql_content_line(SQL_INNER, f"{item}{suffix}"))
        return lines

    if keyword == "FROM":
        return [sql_content_line(SQL_INNER, line) for line in split_joins(body)]

    if keyword in {"WHERE", "HAVING"}:
        return [sql_content_line(SQL_INNER, line) for line in split_where_conditions(body)]

    if keyword in {"ORDER BY", "GROUP BY", "LIMIT", "OFFSET"}:
        items = split_commas(body)
        lines = []
        for index, item in enumerate(items):
            suffix = "," if keyword in {"ORDER BY", "GROUP BY"} and index < len(items) - 1 else ""
            lines.append(sql_content_line(SQL_INNER, f"{item}{suffix}"))
        return lines

    if keyword == "SET":
        items = split_commas(body)
        lines = []
        for index, item in enumerate(items):
            suffix = "," if index < len(items) - 1 else ""
            lines.append(sql_content_line(SQL_INNER, f"{item.strip()}{suffix}"))
        return lines

    if keyword == "VALUES":
        values_match = re.match(r"\((.+)\)$", body, flags=re.DOTALL)
        if values_match:
            items = split_commas(values_match.group(1))
            lines = [f"{SQL_INNER}("]
            for index, item in enumerate(items):
                suffix = "," if index < len(items) - 1 else ""
                lines.append(sql_content_line(SQL_INNER + "    ", f"{item.strip()}{suffix}"))
            lines.append(f"{SQL_INNER})")
            return lines
        if body.startswith("("):
            return [sql_content_line(SQL_INNER, body)]
        return [sql_content_line(SQL_INNER, item) for item in split_commas(body)]

    if keyword in {"INSERT INTO", "UPDATE", "DELETE FROM"}:
        insert_match = re.match(r"(\S+)\s*\((.+)\)$", body, flags=re.DOTALL)
        if keyword == "INSERT INTO" and insert_match:
            table = insert_match.group(1)
            lines = [sql_content_line(SQL_INNER, f"{table} (")]
            columns = split_commas(insert_match.group(2))
            for index, column in enumerate(columns):
                suffix = "," if index < len(columns) - 1 else ""
                lines.append(sql_content_line(SQL_INNER + "    ", f"{column.strip()}{suffix}"))
            lines.append(f"{SQL_INNER})")
            return lines
        return [sql_content_line(SQL_INNER, body)]

    if keyword.endswith("JOIN") or keyword == "JOIN":
        return [sql_content_line(SQL_INNER, body)]

    if keyword == "WITH":
        return format_sql_text(body)

    return [sql_content_line(SQL_INNER, body)]


def format_sql_text(text: str) -> list[str]:
    normalized = collapse_spaces(text)
    if not normalized:
        return []

    matches = [
        match
        for match in CLAUSE_RE.finditer(normalized)
        if not is_inside_mybatis_expr(normalized, match.start())
    ]
    if not matches:
        return [sql_content_line(SQL_BASE, normalized)]

    lines: list[str] = []
    for index, match in enumerate(matches):
        keyword = match.group(1).upper()
        start = match.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(normalized)
        body = normalized[start:end].strip()
        lines.append(f"{SQL_BASE}{keyword}")
        lines.extend(format_clause_body(keyword, body))
    return lines


def format_opening_tag(tag: str, attributes: dict[str, str], indent: str, self_close: bool = False) -> str:
    if not attributes:
        suffix = " />" if self_close else ">"
        return f"{indent}<{tag}{suffix}"

    items = list(attributes.items())
    first_key, first_value = items[0]
    suffix = " />" if self_close else ">"
    if len(items) == 1:
        return f'{indent}<{tag} {first_key}="{first_value}"{suffix}'

    lines = [f'{indent}<{tag} {first_key}="{first_value}"']
    for key, value in items[1:]:
        lines.append(f'{ATTR_ALIGN}{key}="{value}"')
    lines[-1] = lines[-1] + suffix
    return "\n".join(lines)


def format_if_element(element: etree._Element, indent: str) -> list[str]:
    tag = local_tag(element)
    attrs = " ".join(f'{key}="{value}"' for key, value in element.attrib.items())
    open_tag = f"{indent}<{tag} {attrs}>" if attrs else f"{indent}<{tag}>"

    if element.text and element.text.strip() and len(element) == 0:
        return [f"{open_tag}{escape_xml_text(element.text.strip())}</{tag}>"]

    lines = [open_tag]
    inner = indent + "  "
    if element.text and element.text.strip():
        for line in element.text.strip().splitlines():
            lines.append(f"{inner}{escape_xml_text(line.strip())}")
    for child in element:
        lines.extend(format_element(child, inner))
        if child.tail and child.tail.strip():
            for line in child.tail.strip().splitlines():
                lines.append(f"{inner}{escape_xml_text(line.strip())}")
    lines.append(f"{indent}</{tag}>")
    return lines


def format_dynamic_element(element: etree._Element, indent: str) -> list[str]:
    tag = local_tag(element)
    lines = [f"{indent}<{tag}>"]
    child_indent = indent + "  "
    if element.text and element.text.strip():
        lines.append(f"{child_indent}{escape_xml_text(element.text.strip())}")
    for child in element:
        lines.extend(format_if_element(child, child_indent))
        if child.tail and child.tail.strip():
            lines.append(f"{child_indent}{escape_xml_text(child.tail.strip())}")
    lines.append(f"{indent}</{tag}>")
    return lines


def format_element(element: etree._Element, indent: str) -> list[str]:
    tag = local_tag(element)
    if tag in {"where", "set", "trim", "foreach"}:
        return format_dynamic_element(element, indent)
    if tag in {"if", "choose", "when", "otherwise"}:
        return format_if_element(element, indent)
    if tag == "include":
        attrs = " ".join(f'{key}="{value}"' for key, value in element.attrib.items())
        return [f"{indent}<{tag} {attrs}/>"]
    return format_if_element(element, indent)


def format_resultmap(element: etree._Element) -> list[str]:
    lines = [format_opening_tag(local_tag(element), dict(element.attrib), MAPPER_CHILD)]
    for child in element:
        tag = local_tag(child)
        attributes = dict(child.attrib)
        if tag == "collection" and len(attributes) > 2:
            items = list(attributes.items())
            first_key, first_value = items[0]
            lines.append(f'{RESULT_CHILD}<{tag} {first_key}="{first_value}"')
            rest = " ".join(f'{key}="{value}"' for key, value in items[1:])
            lines.append(f"{RESULT_CHILD}            {rest}/>")
        else:
            lines.append(format_opening_tag(tag, attributes, RESULT_CHILD, self_close=True))
    lines.append(f"{MAPPER_CHILD}</{local_tag(element)}>")
    return lines


def format_sql_container(element: etree._Element) -> list[str]:
    tag = local_tag(element)
    lines = [format_opening_tag(tag, dict(element.attrib), MAPPER_CHILD)]

    if element.text and element.text.strip():
        lines.extend(format_sql_text(element.text))

    for child in element:
        lines.extend(format_element(child, SQL_BASE))
        if child.tail and child.tail.strip():
            lines.extend(format_sql_text(child.tail))

    lines.append(f"{MAPPER_CHILD}</{tag}>")
    return lines


def format_sql_block(element: etree._Element) -> list[str]:
    tag = local_tag(element)
    lines = [format_opening_tag(tag, dict(element.attrib), MAPPER_CHILD)]
    dynamic_child = "    "
    for child in element:
        lines.extend(format_dynamic_element(child, dynamic_child))
    lines.append(f"{MAPPER_CHILD}</{tag}>")
    return lines


def format_comment(comment: str) -> list[str]:
    text = comment.strip()
    if text.startswith("<!--") and text.endswith("-->"):
        return [f"{MAPPER_CHILD}{text}"]
    return [line.rstrip() for line in comment.splitlines()]


def format_mapper_tree(root: etree._Element, tree: etree._ElementTree) -> str:
    output: list[str] = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">',
    ]

    pre_comments: list[str] = []
    for sibling in root.itersiblings(preceding=True):
        if isinstance(sibling, etree._Comment):
            pre_comments.append(f"<!--{sibling.text}-->")
    output.extend(reversed(pre_comments))

    output.append(f'<mapper namespace="{root.attrib["namespace"]}">')

    for node in root:
        if isinstance(node, etree._Comment):
            output.extend(format_comment(str(node)))
            continue

        tag = local_tag(node)
        if tag == "resultMap":
            output.extend(format_resultmap(node))
        elif tag in {"select", "insert", "update", "delete"}:
            output.extend(format_sql_container(node))
        elif tag == "sql":
            output.extend(format_sql_block(node))
        else:
            output.extend(format_element(node, MAPPER_CHILD))

    output.append("</mapper>")
    return "\n".join(output) + "\n"


def format_mapper_text(content: str) -> str:
    content = sanitize_text(content)
    parser = etree.XMLParser(remove_blank_text=False)
    try:
        tree = etree.ElementTree(etree.fromstring(content.encode("utf-8"), parser))
    except etree.XMLSyntaxError as error:
        raise ValueError(
            "XML parse failed. Escape comparison operators in mapper text "
            "(use &lt; and &gt;= instead of < and >=). "
            f"{error}"
        ) from error
    return format_mapper_tree(tree.getroot(), tree)


def format_mapper_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    formatted = format_mapper_text(original)
    if formatted == original:
        return False
    path.write_text(formatted, encoding="utf-8", newline="\n")
    return True


def main() -> int:
    if len(sys.argv) == 2 and sys.argv[1] == "--stdin":
        formatted = format_mapper_text(read_stdin_text())
        write_stdout_text(formatted)
        return 0

    if len(sys.argv) != 2:
        print("Usage: format-mybatis-mapper.py <mapper.xml>|--stdin", file=sys.stderr)
        return 1

    path = Path(sys.argv[1])
    if not path.exists():
        print(f"File not found: {path}", file=sys.stderr)
        return 1

    format_mapper_file(path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
