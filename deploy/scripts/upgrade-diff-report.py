#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成二开 / 插件目录相对官方基线的升级差异报告（用户可见产物）。

用途（架构 §5 升级前检查）：
  - 升级官方 Core 或某服务前，先跑本脚本，把 custom-* / 前端扩展 /
    plugins 下相对基线的改动列成 Markdown，交给负责人确认。
  - 默认对比当前工作区与 git HEAD；也可指定 --base <ref>。

用法
----
  python deploy/scripts/upgrade-diff-report.py
  python deploy/scripts/upgrade-diff-report.py --base origin/master --out D:/tmp/upgrade-diff.md
  python deploy/scripts/upgrade-diff-report.py --service goods
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

WATCH_GLOBS = [
    "backend/extensions/custom-{service}/",
    "frontend/admin/src/extensions/{service}/",
    "plugins/{service}/",
]


def run_git(args: list[str]) -> str:
    proc = subprocess.run(
        ["git", *args],
        cwd=ROOT,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if proc.returncode != 0 and not proc.stdout:
        raise RuntimeError(proc.stderr.strip() or f"git {' '.join(args)} failed")
    return proc.stdout


def list_services(explicit: str | None) -> list[str]:
    if explicit:
        return [explicit]
    custom_root = os.path.join(ROOT, "backend", "extensions")
    names = []
    if os.path.isdir(custom_root):
        for name in sorted(os.listdir(custom_root)):
            if name.startswith("custom-") and os.path.isdir(os.path.join(custom_root, name)):
                names.append(name[len("custom-") :])
    return names or ["goods", "order", "base"]


def collect_paths(service: str) -> list[str]:
    paths = []
    for pattern in WATCH_GLOBS:
        rel = pattern.format(service=service).rstrip("/")
        abs_path = os.path.join(ROOT, rel.replace("/", os.sep))
        if os.path.exists(abs_path):
            paths.append(rel)
    return paths


def diff_stat(base: str, path: str) -> str:
    out = run_git(["diff", "--stat", f"{base}...HEAD", "--", path])
    if not out.strip():
        # also include unstaged
        out = run_git(["diff", "--stat", "--", path])
    return out.strip() or "(无差异)"


def main() -> int:
    parser = argparse.ArgumentParser(description="UniMall 二开升级差异报告")
    parser.add_argument("--base", default="HEAD", help="对比基线 git ref（默认 HEAD，含工作区改动用 --base HEAD~0 并看工作区）")
    parser.add_argument("--service", default=None, help="只报告某一个服务，如 goods")
    parser.add_argument("--out", default="", help="写入 Markdown 文件；默认打印到 stdout")
    args = parser.parse_args()

    services = list_services(args.service)
    now = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines = [
        f"# UniMall 升级差异报告",
        "",
        f"- 生成时间：{now}",
        f"- 对比基线：`{args.base}`",
        f"- 说明：下列路径是客户二开 / 插件落点；升级官方源码前请逐项确认。",
        "",
    ]
    for svc in services:
        paths = collect_paths(svc)
        lines.append(f"## 服务 `{svc}`")
        lines.append("")
        if not paths:
            lines.append("_未发现 custom / 扩展 / 插件目录。_")
            lines.append("")
            continue
        for path in paths:
            lines.append(f"### `{path}`")
            lines.append("")
            lines.append("```")
            lines.append(diff_stat(args.base, path))
            lines.append("```")
            lines.append("")
        lines.append(
            f"- 脚手架：`bash deploy/scripts/new-extension.sh --module {svc} ...` "
            f"或 `powershell -File deploy/scripts/new-extension.ps1`"
        )
        lines.append(f"- 手册：[backend/extensions/README.md](backend/extensions/README.md)")
        lines.append("")

    lines.append("## 下一步")
    lines.append("")
    lines.append("1. 备份库与配置；")
    lines.append("2. 在影子环境应用官方升级包；")
    lines.append("3. 对照本报告回放二开；")
    lines.append("4. 跑 `bash deploy/scripts/verify-extensions.sh` 与关键检查。")
    lines.append("")

    text = "\n".join(lines)
    if args.out:
        os.makedirs(os.path.dirname(os.path.abspath(args.out)) or ".", exist_ok=True)
        with open(args.out, "w", encoding="utf-8") as fh:
            fh.write(text)
        print("wrote", args.out)
    else:
        print(text)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:  # noqa: BLE001
        print("ERROR:", exc, file=sys.stderr)
        raise SystemExit(2)
