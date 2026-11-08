#!/usr/bin/env bash
#
# new-extension.sh
# ---------------------------------------------------------------------------
# deploy/scripts/new-extension.ps1 的 POSIX 等价实现（R4.4 跨平台脚手架）。
# 生成内容、占位符替换、聚合 pom 登记、已存在模块追加 Bean 的行为与 .ps1 一致，
# 复用同一套模板 deploy/scripts/templates/extension/*.tpl。
#
# 用法：
#   bash deploy/scripts/new-extension.sh --module goods --point PriceCalc \
#        --package com.houjiang.plugin.api.extension.goods [--site acme] \
#        [--mode APPEND|REPLACE] [--description "..."]
#   亦支持位置参数：bash deploy/scripts/new-extension.sh <Module> <Point> <Package> [Site]
#
# 跨平台差异（相对 .ps1）：
#   1) 换行用 LF（.ps1 用 CRLF）；模板内容逐字拷贝，不改变语义。
#   2) 收尾提示改用 `bash deploy/scripts/verify-extensions.sh`（等价于 .ps1 的 powershell 命令）。
#   3) 本机（macOS）**无 pwsh**，未在本机对照 `.ps1` 实跑；生成行为为逐条人工对照的镜像
#      （此注记请勿删除）。
# ---------------------------------------------------------------------------
set -u

usage() {
    cat >&2 <<'EOF'
用法: bash deploy/scripts/new-extension.sh [--module M] [--point P] [--package PKG] [--site S] [--mode APPEND|REPLACE] [--description D]
     或: bash deploy/scripts/new-extension.sh <Module> <Point> <Package> [Site]
EOF
}

module=""
point=""
package=""
site=""
mode="APPEND"
description=""
positionals=()

while [ $# -gt 0 ]; do
    case "$1" in
        --module|-m)    [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; module="$2"; shift 2 ;;
        --point|-p)     [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; point="$2"; shift 2 ;;
        --package|-k)   [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; package="$2"; shift 2 ;;
        --site|-s)      [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; site="$2"; shift 2 ;;
        --mode)         [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; mode="$2"; shift 2 ;;
        --description|-d) [ $# -ge 2 ] || { echo "参数 $1 缺少值" >&2; exit 2; }; description="$2"; shift 2 ;;
        --help|-h)      usage; exit 0 ;;
        --*)            echo "未知参数: $1" >&2; usage; exit 2 ;;
        *)              positionals+=("$1"); shift ;;
    esac
done

# 位置参数回填（module / point / package / site）
idx=0
for slot in module point package site; do
    if [ -z "${!slot}" ] && [ "$idx" -lt "${#positionals[@]}" ]; then
        printf -v "$slot" '%s' "${positionals[$idx]}"
        idx=$((idx + 1))
    fi
done

if [ -z "$module" ] || [ -z "$point" ] || [ -z "$package" ]; then
    echo "缺少必填参数：--module / --point / --package" >&2
    usage
    exit 2
fi
case "$mode" in
    APPEND|REPLACE) ;;
    *) echo "非法 --mode：$mode（只能是 APPEND|REPLACE）" >&2; exit 2 ;;
esac

script_dir="$(cd "$(dirname "$0")" && pwd)"
repo_root="$(cd "$script_dir/../.." && pwd)"
ext_root="$repo_root/backend/extensions"
tpl_dir="$script_dir/templates/extension"

if [ -n "$site" ]; then
    module_name="custom-${module}-${site}"
else
    module_name="custom-${module}"
fi
module_dir="$ext_root/$module_name"
ext_package="com.houjiang.ext.${module}"
pkg_dir="com/houjiang/ext/${module}"

if [ -z "$description" ]; then
    description="二开：${point} 定制"
fi
if [ ! -d "$tpl_dir" ]; then
    echo "未找到模板目录：$tpl_dir" >&2
    exit 1
fi
if [ ! -d "$ext_root" ]; then
    echo "未找到二开根目录：$ext_root" >&2
    exit 1
fi

# 标题化模块名（PowerShell (Get-Culture).TextInfo.ToTitleCase）：首字母大写
first="$(printf '%s' "$module" | cut -c1 | tr '[:lower:]' '[:upper:]')"
rest="$(printf '%s' "$module" | cut -c2-)"
module_title="${first}${rest}"
config_name="Custom${module_title}ExtensionsAutoConfiguration"

if [ "$mode" = "REPLACE" ]; then
    mode_anno=$'\n        mode = ExtensionMode.REPLACE,'
    mode_import=$'\nimport com.houjiang.plugin.sdk.extension.ExtensionMode;'
else
    mode_anno=""
    mode_import=""
fi

is_new_module=0
if [ ! -d "$module_dir" ]; then
    is_new_module=1
fi
if [ "$is_new_module" -eq 1 ]; then action="新建"; else action="追加"; fi
echo "模块：${module_name} [${action}]   扩展点：${package}.${point}Extension   模式：${mode}"

write_text_file() { # <path> <content>
    local path="$1" content="$2" dir rel
    dir="$(dirname "$path")"
    [ -d "$dir" ] || mkdir -p "$dir"
    printf '%s' "$content" > "$path"
    rel="${path#"$repo_root"/}"
    echo "  + $rel"
}

render_template() { # <tplName> <destPath>
    local tpl="$tpl_dir/$1" dest="$2" text
    [ -f "$tpl" ] || { echo "缺少模板：$tpl" >&2; exit 1; }
    # 保留模板尾部换行（命令替换会吞掉尾部换行，故加哨兵 X 再剥除）
    text="$(cat "$tpl"; printf X)"
    text="${text%X}"
    text="${text//\{EXTPACKAGE\}/$ext_package}"
    text="${text//\{PACKAGE\}/$package}"
    text="${text//\{POINT\}/$point}"
    text="${text//\{MODULE\}/$module}"
    text="${text//\{CODE\}/$module_name}"
    text="${text//\{CONFIGNAME\}/$config_name}"
    text="${text//\{DESCRIPTION\}/$description}"
    text="${text//\{MODE\}/$mode_anno}"
    text="${text//\{MODEIMPORT\}/$mode_import}"
    write_text_file "$dest" "$text"
}

main_java="$module_dir/src/main/java/$pkg_dir"
test_java="$module_dir/src/test/java/$pkg_dir"

render_template CustomExtension.java.tpl "$main_java/Custom${point}Extension.java"
render_template DefaultExtension.java.tpl "$main_java/Default${point}Extension.java"
render_template ContractTest.java.tpl "$test_java/${point}ExtensionContractTest.java"
render_template DefaultTest.java.tpl "$test_java/Default${point}ExtensionTest.java"
render_template CustomTest.java.tpl "$test_java/Custom${point}ExtensionTest.java"

if [ "$is_new_module" -eq 1 ]; then
    render_template AutoConfiguration.java.tpl "$main_java/${config_name}.java"
    render_template AutoConfiguration.imports.tpl \
        "$module_dir/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
    render_template houcloud-extension.properties.tpl \
        "$module_dir/src/main/resources/META-INF/houcloud-extension.properties"
    render_template pom.xml.tpl "$module_dir/pom.xml"
else
    config_path="$main_java/${config_name}.java"
    if [ ! -f "$config_path" ]; then
        echo "已存在模块但找不到自动配置类，请手工确认模块结构：$config_path" >&2
        exit 1
    fi
    bean_method="custom${point}Extension()"
    if grep -qF "$bean_method" "$config_path"; then
        echo "  = 自动配置已存在 ${bean_method}，跳过"
    else
        text="$(cat "$config_path"; printf X)"
        text="${text%X}"
        brace='}*'
        prefix="${text%$brace}"
        # rtrim（去除尾随空白，含换行）
        prefix="${prefix%"${prefix##*[![:space:]]}"}"
        {
            printf '%s' "$prefix"
            printf '\n\n    @Bean\n'
            printf '    public Custom%sExtension custom%sExtension() {\n' "$point" "$point"
            printf '        return new Custom%sExtension();\n' "$point"
            printf '    }\n}\n'
        } > "$config_path"
        echo "  ~ 已向 ${config_name} 追加 Bean"
    fi
fi

if [ "$is_new_module" -eq 1 ]; then
    agg="$ext_root/pom.xml"
    if grep -qF "<module>${module_name}</module>" "$agg"; then
        : # 已登记，跳过（与 .ps1 的 Contains 判断一致）
    else
        tmp_file="$agg.tmp.$$"
        awk -v module_name="$module_name" -v module="$module" '
            BEGIN { inserted = 0; anchor = "        <module>custom-" module "</module>" }
            {
                if (!inserted && $0 == anchor) {
                    print
                    print "        <module>" module_name "</module>"
                    inserted = 1
                    next
                }
                if (!inserted && $0 ~ /^[[:space:]]*<\/modules>[[:space:]]*$/) {
                    print "        <module>" module_name "</module>"
                    print
                    inserted = 1
                    next
                }
                print
            }
        ' "$agg" > "$tmp_file" && mv "$tmp_file" "$agg"
        echo "  ~ 已登记到 backend/extensions/pom.xml"
    fi
fi

echo ''
echo '生成完成。下一步：'
echo '  1) 补齐生成的 TODO（契约断言 + Custom* 业务规则）'
echo '  2) mvn -f backend/extensions/pom.xml -DforkCount=0 clean test'
echo '  3) bash deploy/scripts/verify-extensions.sh'
