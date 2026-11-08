#!/usr/bin/env python3
"""用 variables.env 渲染 templates/ 下的 Nacos 配置模板，输出到 build/。

用法：
  cp variables.example variables.env   # 然后填入真实值（该文件已在 .gitignore 中）
  python3 apply_templates.py

规则：模板中的 ${VAR} 会被 variables.env 中同名变量替换；
未提供值的占位符会原样保留并在结束时汇总提示（缺值时导入后服务可能起不来）。
"""
import os
import re
import sys
import pathlib

HERE = pathlib.Path(__file__).parent
TPL = HERE / 'templates'
BUILD = HERE / 'build'
ENV_FILE = pathlib.Path(os.environ.get('VARIABLES_FILE', HERE / 'variables.env'))


def load_vars(path: pathlib.Path) -> dict:
    if not path.exists():
        print(f'!! 找不到变量文件 {path}，请先 cp variables.example variables.env 并填写')
        sys.exit(1)
    out = {}
    for line in path.read_text(encoding='utf-8').splitlines():
        line = line.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        k, v = line.split('=', 1)
        v = v.split('#')[0].strip().strip('"\'')
        # 空值也必须登记：早期版本用 `if v:` 跳过空值，导致模板里的 ${VAR} 原样
        # 留在渲染结果中，Spring 启动阶段解析占位符失败
        # （Could not resolve placeholder 'XXX'）会让整个服务起不来。
        # 第三方凭据留空时应当渲染成空串，而不是留下非法占位符。
        out[k.strip()] = v
    return out


def main():
    vars_ = load_vars(ENV_FILE)
    missing = set()
    count = 0
    for src in sorted(TPL.rglob('*.yaml')):
        # 跳过 macOS 解压/传输产生的 AppleDouble 旁文件（._xxx.yaml）等隐藏文件
        if src.name.startswith('._') or src.name.startswith('.'):
            print(f'  [跳过] {src.name}（隐藏/旁文件）')
            continue
        rel = src.relative_to(TPL)
        try:
            text = src.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            text = src.read_text(encoding='utf-8', errors='replace')
            print(f'  [警告] {rel} 不是合法 UTF-8，已按替换模式读取（建议用 UTF-8 重新保存）')

        def repl(m):
            name = m.group(1)
            if name in vars_:
                return vars_[name]
            missing.add(name)
            return m.group(0)

        rendered = re.sub(r'\$\{([A-Z0-9_]+)\}', repl, text)
        dst = BUILD / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(rendered, encoding='utf-8')
        count += 1

    print(f'==> 已渲染 {count} 个配置文件到 {BUILD}')
    if missing:
        print(f'!! 以下 {len(missing)} 个变量未赋值（配置中将保留 ${{VAR}} 原样）：')
        for m in sorted(missing):
            print('  ', m)
    else:
        print('==> 所有变量已赋值。')


if __name__ == '__main__':
    main()
