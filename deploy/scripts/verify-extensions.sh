#!/usr/bin/env bash
#
# verify-extensions.sh
# ---------------------------------------------------------------------------
# deploy/scripts/verify-extensions.ps1 的 POSIX 等价实现（R4.4 跨平台门禁）。
# 断言与 .ps1 **逐条对应**，退出码语义一致（有错误 = 1，无错误 = 0）。
#
# 用法：
#   bash deploy/scripts/verify-extensions.sh [Root]
#   （Root 省略时取脚本上一级目录，即仓库根，与 .ps1 默认行为一致）
#
# 跨平台差异（相对 .ps1）：
#   1) 路径分隔符：.ps1 用 '\\src\\main\\java\\'（Windows）；POSIX 用 '/src/main/java/'。
#   2) 多行 @HouExtension(...) 解析用 perl（macOS/Linux 自带）替代 PowerShell 正则，语义一致。
#   3) @HouExtension 计数用「匹配行数」；本仓库每行仅一个注解，与 .ps1 的「匹配次数」等价。
#   4) 本机（macOS）**无 pwsh**，未在本机对照 `.ps1` 实跑；A1–A22 为逐条人工对照的镜像，
#      等价性靠对照而非实跑（此注记请勿删除）。
#
# 断言对照表（.ps1 → 本脚本）：
#   [A1] Missing extension root                                   → add_error "Missing extension root"
#   [A2] Missing build-time extension descriptor                  → add_error "Missing build-time extension descriptor"
#   [A3] code 必须等于模块目录名                                    → add_error "code must match module directory"
#   [A4] version 必须 X.Y.Z                                        → add_error "version must be semantic X.Y.Z"
#   [A5] hostVersion 必须 (x|数字)(.x|.数字){1,2}                  → add_error "hostVersion must be a compatible release"
#   [A6] scope 必须 site                                           → add_error "must declare scope=site"
#   [A7] contracts 必须含 plugin-api                               → add_error "must declare plugin-api contract"
#   [A8] AutoConfiguration.imports 必须存在且非空                   → add_error "must expose explicit AutoConfiguration.imports"
#   [A9] 统计 @HouExtension 数（成功输出里的 N）                     → custom_extension_count
#   [A10] 禁 @Primary                                              → add_error "@Primary is not allowed"
#   [A11] 禁 import com.houjiang.<官方域>                           → add_error "Official implementation package imported"
#   [A12] 禁 import com.houjiang.plugin.engine|plugin.runtime       → add_error "Runtime implementation package imported"
#   [A13] 禁 @ComponentScan / @SpringBootApplication               → add_error "must not scan or own the host application"
#   [A14] 禁 return new Default*(                                  → add_error "must not register Default* reference implementations"
#   [A15] owner=custom 必须声明稳定 code                            → add_error "must declare a stable code"
#   [A16] 禁 owner=thirdparty                                      → add_error "cannot use owner=thirdparty"
#   [A17] mode=REPLACE 必须 owner=custom                            → add_error "REPLACE is reserved for owner=custom"
#   [A18] extensions 下 pom.xml 禁依赖官方实现 artifactId           → add_error "Official implementation dependency is not allowed"
#   --- 以下为 .sh 与 .ps1 同步新增的「五件套约定」门禁（设计 §1.2） ---
#   [A19] 二开源码禁 @RefreshScope（配置禁热更新）                  → add_error "@RefreshScope is forbidden"
#   [A20] 二开源码禁 @FeignClient（出站须放 client/）              → add_error "@FeignClient is forbidden in build-time extensions"
#   [A21] plugin-api 禁 @FeignClient（契约面禁出站）               → add_error "@FeignClient is forbidden in plugin-api"
#   [A22] 二开资源禁声明独立端口 server.port / server:             → add_error "must not declare a standalone server port"
# ---------------------------------------------------------------------------
set -u

script_dir="$(cd "$(dirname "$0")" && pwd)"
if [ -n "${1:-}" ]; then
    root="$1"
else
    root="$(cd "$script_dir/../.." && pwd)"
fi

extension_root="$root/backend/extensions"
errors=0
custom_extension_count=0

add_error() {
    printf 'ERROR: %s\n' "$1" >&2
    errors=$((errors + 1))
}

# 读取 properties 中某个键的值（去首尾空白）。语义等价于 .ps1 的 ^\s*key\s*=\s*(.*?)\s*$
prop() { # <file> <key>
    sed -n -E "s/^[[:space:]]*$2[[:space:]]*=[[:space:]]*(.*[^[:space:]])[[:space:]]*\$/\1/p" "$1" | head -n 1
}

if [ ! -d "$extension_root" ]; then
    add_error "Missing extension root: $extension_root"
else
    # --- 每个构建期二开：交付描述 + AutoConfiguration.imports -----------------
    for module_dir in "$extension_root"/custom-*; do
        [ -d "$module_dir" ] || continue
        descriptor="$module_dir/src/main/resources/META-INF/houcloud-extension.properties"
        if [ ! -f "$descriptor" ]; then
            add_error "Missing build-time extension descriptor: $descriptor"
            continue
        fi

        expected_code="$(basename "$module_dir")"
        code="$(prop "$descriptor" code)"
        version="$(prop "$descriptor" version)"
        host_version="$(prop "$descriptor" hostVersion)"
        scope="$(prop "$descriptor" scope)"
        contracts="$(prop "$descriptor" contracts)"

        if [ "$code" != "$expected_code" ]; then
            add_error "Extension descriptor code must match module directory ($expected_code): $descriptor"
        fi
        if ! [[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            add_error "Extension descriptor version must be semantic X.Y.Z: $descriptor"
        fi
        if ! [[ "$host_version" =~ ^([0-9]+\.){1,2}([0-9]+|x)$ ]]; then
            add_error "Extension descriptor hostVersion must be a compatible release (for example 1.x): $descriptor"
        fi
        if [ "$scope" != "site" ]; then
            add_error "Build-time extensions must declare scope=site: $descriptor"
        fi
        if ! [[ "$contracts" =~ (^|,)plugin-api(,|$) ]]; then
            add_error "Extension descriptor must declare plugin-api contract: $descriptor"
        fi

        imports="$module_dir/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        if [ ! -f "$imports" ]; then
            add_error "Build-time extension must expose explicit AutoConfiguration.imports: $imports"
        elif ! grep -q -E '^[[:space:]]*[^#[:space:]]' "$imports"; then
            add_error "AutoConfiguration.imports must list at least one configuration class: $imports"
        fi
    done

    # --- 源码级断言 ----------------------------------------------------------
    while IFS= read -r java_file; do
        [ -n "$java_file" ] || continue
        case "$java_file" in
            */src/main/java/*) ;;
            *) continue ;;
        esac

        count="$(grep -c -E '^[[:space:]]*@HouExtension([^[:alnum:]_]|$)' "$java_file")"
        custom_extension_count=$((custom_extension_count + count))

        if grep -q -E '^[[:space:]]*@Primary([^[:alnum:]_]|$)' "$java_file"; then
            add_error "@Primary is not allowed in build-time extensions: $java_file"
        fi
        if grep -q -E '^[[:space:]]*import[[:space:]]+com\.houjiang\.(auth|base|goods|job|merchant|order|payment|permission|supplier|thirdparty|userms|aicode|mall)(\.|;)' "$java_file"; then
            add_error "Official implementation package imported by extension: $java_file"
        fi
        if grep -q -E '^[[:space:]]*import[[:space:]]+com\.houjiang\.(plugin\.engine|plugin\.runtime)([^[:alnum:]_]|$)' "$java_file"; then
            add_error "Runtime implementation package imported by extension: $java_file"
        fi
        if grep -q -E '^[[:space:]]*@(ComponentScan|SpringBootApplication)([^[:alnum:]_]|$)' "$java_file"; then
            add_error "Build-time extensions must not scan or own the host application: $java_file"
        fi
        if grep -q -E '\breturn[[:space:]]+new[[:space:]]+Default[A-Za-z0-9_]+[[:space:]]*\(' "$java_file"; then
            add_error "Auto-configuration must not register Default* reference implementations: $java_file"
        fi
        # 五件套约定：配置禁热更新（禁 @RefreshScope）；出站禁 Feign（须放 client/）
        if grep -q -E '^[[:space:]]*@RefreshScope([^[:alnum:]_]|$)' "$java_file"; then
            add_error "@RefreshScope is forbidden in build-time extensions (configuration is not hot-reloadable): $java_file"
        fi
        if grep -q -E '^[[:space:]]*@FeignClient([^[:alnum:]_]|$)' "$java_file"; then
            add_error "@FeignClient is forbidden in build-time extensions (put outbound clients under client/): $java_file"
        fi

        # 解析每个 @HouExtension(...) 注解块并核对 owner/code/mode
        while IFS= read -r anno; do
            [ -n "$anno" ] || continue
            owner_custom=0
            owner_thirdparty=0
            mode_replace=0
            case "$anno" in *'owner="custom"'*) owner_custom=1 ;; esac
            case "$anno" in *'owner="thirdparty"'*) owner_thirdparty=1 ;; esac
            case "$anno" in *'mode=ExtensionMode.REPLACE'*) mode_replace=1 ;; esac

            code_re='code="[a-z0-9][a-z0-9._-]*"'
            if [ "$owner_custom" -eq 1 ] && ! [[ "$anno" =~ $code_re ]]; then
                add_error "Custom extension must declare a stable code: $java_file"
            fi
            if [ "$owner_thirdparty" -eq 1 ]; then
                add_error "Build-time extensions cannot use owner=thirdparty; ship a signed runtime plugin instead: $java_file"
            fi
            if [ "$mode_replace" -eq 1 ] && [ "$owner_custom" -eq 0 ]; then
                add_error "ExtensionMode.REPLACE is reserved for owner=custom: $java_file"
            fi
        done < <(perl -0777 -ne 'while (/^[ \t]*\@HouExtension\s*\((.*?)\)/msg) { my $a = $1; $a =~ s/\s*=\s*/=/g; $a =~ s/\s+/ /g; print "$a\n"; }' "$java_file")
    done < <(find "$extension_root" -type f -name '*.java')

    # --- pom.xml 依赖断言 ---------------------------------------------------
    while IFS= read -r pom; do
        [ -n "$pom" ] || continue
        if perl -0777 -ne 'print "HIT" if m{<artifactId>\s*(auth|base|goods|job|merchant|order|payment|permission|supplier|thirdparty|userms|aicode|mall|core-api|goods-api|order-api|payment-api|permission-api)\s*</artifactId>}s' "$pom" | grep -q HIT; then
            add_error "Official implementation dependency is not allowed: $pom"
        fi
    done < <(find "$extension_root" -type f -name 'pom.xml')

    # 五件套约定：plugin-api 是契约面，禁出站客户端（@FeignClient）
    plugin_api_root="$root/backend/contracts/plugin-api"
    if [ -d "$plugin_api_root" ]; then
        while IFS= read -r api_file; do
            [ -n "$api_file" ] || continue
            case "$api_file" in
                */src/main/java/*) ;;
                *) continue ;;
            esac
            if grep -q -E '^[[:space:]]*@FeignClient([^[:alnum:]_]|$)' "$api_file"; then
                add_error "@FeignClient is forbidden in plugin-api (contract surface must stay outbound-free): $api_file"
            fi
        done < <(find "$plugin_api_root" -type f -name '*.java')
    fi

    # 五件套约定：二开不注册微服务、不占端口（资源中不得声明 server.port / server:）
    while IFS= read -r res_file; do
        [ -n "$res_file" ] || continue
        if grep -q -E '^[[:space:]]*server\.port[[:space:]]*[:=]' "$res_file" \
            || grep -q -E '^[[:space:]]*server[[:space:]]*:[[:space:]]*$' "$res_file"; then
            add_error "Build-time extensions must not declare a standalone server port: $res_file"
        fi
    done < <(find "$extension_root" -type f \( -name '*.yml' -o -name '*.yaml' -o -name '*.properties' \) -path '*src/main/resources*')
fi

if [ "$errors" -gt 0 ]; then
    exit 1
fi

printf 'Build-time extension architecture checks passed (%s annotated source file(s)).\n' "$custom_extension_count"
