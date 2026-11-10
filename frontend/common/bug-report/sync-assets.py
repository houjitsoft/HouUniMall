#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""同步公开端 Bug 提交资产；默认仅 admin，其他公开端须显式选择。

权威源为同目录 bug-report.js。--check 只校验，不写入。
示例：python3 frontend/common/bug-report/sync-assets.py --targets admin --check
Nuxt pc 仅校验 nuxt.config.ts 中的脚本登记，不改写 TypeScript。
"""
import argparse
import hashlib
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[3]
FRONTEND = REPO / "frontend"
SOURCE = Path(__file__).resolve().parent / "bug-report.js"
PUBLIC_TARGETS = ["admin", "bcenter", "pc", "uniapp"]
MARK_START = "<!-- unimall-bug-report:start -->"
MARK_END = "<!-- unimall-bug-report:end -->"
SCRIPT = '<script src="bug-report/bug-report.js" defer></script>'
ADMIN_SCRIPT = '<script src="/bug-report/bug-report.js" defer></script>'


def main():
    parser = argparse.ArgumentParser(description="仅同步已有公开端的 Bug 提交资产")
    parser.add_argument("--targets", nargs="+", choices=PUBLIC_TARGETS, default=["admin"])
    parser.add_argument("--check", action="store_true", help="仅校验，不写入")
    args = parser.parse_args()
    source = SOURCE.read_bytes()
    print("[资产] 权威源 SHA256 " + hashlib.sha256(source).hexdigest())
    failures = []
    changes = []
    for target in dict.fromkeys(args.targets):
        directory = FRONTEND / target
        if not directory.is_dir():
            failures.append("公开端不存在：" + target)
            continue
        dest = directory / "public/bug-report/bug-report.js"
        if not dest.is_file() or dest.read_bytes() != source:
            if args.check:
                failures.append("副本缺失或不一致：" + str(dest.relative_to(REPO)))
            else:
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(source)
                changes.append(str(dest.relative_to(REPO)))
        if target == "pc":
            config = directory / "nuxt.config.ts"
            if not config.is_file() or "bug-report/bug-report.js" not in config.read_text(encoding="utf-8"):
                failures.append("Nuxt app.head.script 未登记：frontend/pc/nuxt.config.ts")
            continue
        entry = directory / "index.html"
        if not entry.is_file():
            failures.append("入口不存在：" + str(entry.relative_to(REPO)))
            continue
        raw = entry.read_bytes().decode("utf-8")
        newline = "\r\n" if "\r\n" in raw else "\n"
        block = newline.join([MARK_START, ADMIN_SCRIPT if target == "admin" else SCRIPT, MARK_END])
        if raw.count(MARK_START) != raw.count(MARK_END) or raw.count(MARK_START) > 1:
            failures.append("入口注入标记异常：" + str(entry.relative_to(REPO)))
            continue
        match = re.search(re.escape(MARK_START) + r".*?" + re.escape(MARK_END), raw, re.S)
        if match and match.group() == block:
            continue
        if "</body>" not in raw:
            failures.append("入口缺少 </body>：" + str(entry.relative_to(REPO)))
            continue
        if args.check:
            failures.append("入口未注入或引用不一致：" + str(entry.relative_to(REPO)))
            continue
        if match:
            updated = raw[:match.start()] + block + raw[match.end():]
        else:
            updated = raw.replace("</body>", block + newline + "</body>", 1)
        entry.write_bytes(updated.encode("utf-8"))
        changes.append(str(entry.relative_to(REPO)))
    for path in changes:
        print("[变更] " + path)
    for failure in failures:
        print("[失败] " + failure)
    print("变更文件数：%d" % len(changes))
    if failures:
        return 1
    print("[完成] 所选公开端资产一致、入口已注入")
    return 0


if __name__ == "__main__":
    sys.exit(main())
