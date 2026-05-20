"""Rewrite text files to UTF-8 without BOM when UTF-16 or UTF-8 BOM is detected (ASCII-only source)."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(r"D:\CODE\Sdp")


def sniff(raw: bytes) -> str:
    if raw.startswith(b"\xff\xfe"):
        return "utf-16-le-bom"
    if raw.startswith(b"\xfe\xff"):
        return "utf-16-be-bom"
    if raw.startswith(b"\xef\xbb\xbf"):
        return "utf-8-bom"
    return "utf-8-or-binary"


def load_text(path: Path) -> tuple[str, str]:
    raw = path.read_bytes()
    label = sniff(raw)
    if label == "utf-16-le-bom":
        return raw.decode("utf-16-le"), label
    if label == "utf-16-be-bom":
        return raw.decode("utf-16-be"), label
    if label == "utf-8-bom":
        return raw.decode("utf-8-sig"), label
    return raw.decode("utf-8"), label


def main() -> None:
    candidates: list[Path] = []
    rels = [
        Path("docs") / "sdp-auth-rbac-design.md",
        Path("MAVEN_IDEA.md"),
    ]
    for rel in rels:
        p = ROOT / rel
        if p.is_file():
            candidates.append(p)

    db_dir = ROOT / "back-end" / "sdp-admin" / "sdp-server" / "src" / "main" / "resources" / "db"
    if db_dir.is_dir():
        candidates.extend(sorted(db_dir.rglob("*.sql")))

    rbac = (
        ROOT
        / "back-end"
        / "sdp-admin"
        / "sdp-modules"
        / "sdp-module-system"
        / "src"
        / "main"
        / "resources"
        / "rbac-dm.sql"
    )
    if rbac.is_file():
        candidates.append(rbac)

    extra = [
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "system" / "controller" / "SysMenuController.java",
        ROOT / "front-end" / "sdp-system" / "sdp-system" / "src" / "stores" / "menu.ts",
        ROOT / "front-end" / "sdp-system" / "sdp-system" / "src" / "views" / "Login.vue",
        ROOT / "front-end" / "sdp-system" / "sdp-system" / "src" / "router" / "index.ts",
        ROOT / "front-end" / "sdp-system" / "sdp-system" / "src" / "components" / "menus" / "SideMenu.vue",
        ROOT / "front-end" / "sdp-system" / "sdp-system" / "src" / "layouts" / "BasicLayout.vue",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "controller" / "AuthController.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "config" / "WebCorsConfig.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "service" / "AuthService.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "dto" / "LoginResponse.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "entity" / "SysLoginLog.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "entity" / "SysUser.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "common" / "ApiResponse.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "java" / "org" / "nmgyj" / "authentication" / "config" / "MybatisPlusConfig.java",
        ROOT / "back-end" / "sdp-admin" / "sdp-server" / "src" / "main" / "resources" / "application-dev.yml",
        ROOT / "back-end" / "sdp-admin" / "sdp-server" / "src" / "main" / "resources" / "application.yml",
        ROOT / "back-end" / "sdp-admin" / "sdp-modules" / "sdp-module-system" / "src" / "main" / "resources" / "application.yml",
    ]
    for p in extra:
        if p.is_file():
            candidates.append(p)

    for base in (
        ROOT / "front-end" / "sdp-system" / "sdp-system",
        ROOT / "back-end" / "sdp-admin",
    ):
        if base.is_dir():
            for child in base.iterdir():
                if child.is_file() and child.name.startswith(".env"):
                    candidates.append(child)

    report: list[dict] = []
    seen: set[str] = set()
    for path in candidates:
        key = str(path.resolve())
        if key in seen:
            continue
        seen.add(key)
        raw = path.read_bytes()
        label = sniff(raw)
        converted = False
        if label in ("utf-16-le-bom", "utf-16-be-bom", "utf-8-bom"):
            text, _ = load_text(path)
            path.write_bytes(text.encode("utf-8"))
            converted = True
        report.append(
            {
                "file": key,
                "before": label,
                "converted": converted,
                "after": sniff(path.read_bytes()),
            }
        )

    out = ROOT / "back-end" / "sdp-admin" / "normalize_encoding_report.json"
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
