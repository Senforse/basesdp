"""Emit UTF-8 (no BOM) Chinese markdown from sdp_design_entities_zh.md (ASCII HTML entities)."""
from __future__ import annotations

import html
from pathlib import Path

HERE = Path(__file__).resolve().parent
SRC = HERE / "sdp_design_entities_zh.md"
OUT = HERE / "sdp_design_utf8_zh.md"
TAIL = "## \u751f\u6210 UTF-8 \u6b63\u6587\uff08\u53ef\u9009\uff09"


def main() -> None:
    text = html.unescape(SRC.read_text(encoding="utf-8"))
    if TAIL in text:
        text = text.split(TAIL)[0].rstrip() + "\n"
    OUT.write_bytes(text.encode("utf-8"))
    preview = OUT.read_bytes()[:12]
    (HERE / "materialize_report.txt").write_text(
        "first_bytes=" + " ".join(f"{b:02X}" for b in preview) + "\n",
        encoding="ascii",
    )


if __name__ == "__main__":
    main()
