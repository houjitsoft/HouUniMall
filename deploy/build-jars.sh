#!/usr/bin/env bash
# Build independently deployable domain services from the architecture catalog.
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"
MVN="${MVN:-mvn}"
# 国内构建到 maven central 实测 77 B/s 量级（2026-10-30 ① 上全量依赖要数小时），
# 检测到镜像配置存在时走阿里云公共仓库；文件不存在的树（历史快照）行为不变。
# 注意用数组追加（"$MVN" 是带引号的整体命令名，往里拼 " -s path" 会 127）。
mvn_settings_args=()
MAVEN_SETTINGS="$REPO_ROOT/deploy/maven-settings.xml"
if [ -f "$MAVEN_SETTINGS" ]; then mvn_settings_args=(-s "$MAVEN_SETTINGS"); fi
JARS_OUT_DIR="${JARS_OUT_DIR:-$REPO_ROOT/.build/jars}"
SKIP_TESTS="${SKIP_TESTS:-true}"
flags=(-B -Dmaven.javadoc.skip=true)
if [ "$SKIP_TESTS" = true ]; then flags+=(-Dmaven.test.skip=true); fi
# 服务列表取「目录清单 ∩ 盘上真有 pom.xml 的模块」：
# 公开裁切包不含商业模块（crm/cms/erp/...），直接按清单 -pl 会 BUILD FAILURE
# 「Could not find the selected project in the reactor」；主树里两者本就一致，过滤无副作用。
# 2026-10-29 修复：服务名（services.json name）与模块名（artifactId）分离的情况
# （首例：ai 服务的源码模块是 aiagent）——mvn -pl 与 jar 收集路径都必须用模块真实的
# artifactId（jar 路径 backend/paas/<artifactId>/target/<artifactId>.jar 对全量服务成立），
# 不能拿服务名拼接，否则 reactor 报「Could not find the selected project: ai」。
services="$(python3 -c 'import json,os,xml.etree.ElementTree as ET
d=json.load(open("architecture/services.json"))
mods=[]
for s in d["services"]:
  p=os.path.join(s["source"],"pom.xml")
  if os.path.isfile(p):
    m=None
    for child in ET.parse(p).getroot():
      if child.tag.split("}")[-1]=="artifactId": m=(child.text or "").strip(); break
    mods.append(m if m else s["name"])
print(",".join(mods))')"
# 插件宿主服务：mall 承载商城域 8 个插件，是一个要独立部署的服务（compose 里有它、
# 有 jar、有端口 8220），但它**不在业务服务清单 services.json 里** —— 那个清单刻意
# 不含 mall（BFF 的 PluginServiceResolver 见到清单里有 mall 会直接拒绝启动，
# 为的是拦住「把所有插件一律挂回 mall」的统一宿主回归）。
# 两件事都是真的：mall 是合法宿主 ≠ mall 是业务服务清单成员。
# 所以这里单独补进构建与归集，不能往 services.json 里加。
HOST_SERVICES="mall"
services="${services},${HOST_SERVICES}"
# 分发树（开源导出包，根目录带 install.sh）默认 dist 门禁口径：商业/内部模块
# 不随包属预期（verify.py 对 Missing 只记 WARN、只校验随包资产自相一致）；
# 主树/生产树没有 install.sh，不受影响，仍按 full 全量门禁。
if [ -z "${HOU_ARCH_PROFILE:-}" ] && [ -f "$REPO_ROOT/install.sh" ]; then
  HOU_ARCH_PROFILE=dist
fi
python3 deploy/scripts/architecture/verify.py --profile "${HOU_ARCH_PROFILE:-full}"
"$MVN" "${mvn_settings_args[@]}" "${flags[@]}" -f backend/paas/pom.xml -pl "$services" -am clean install
"$MVN" "${mvn_settings_args[@]}" "${flags[@]}" -f backend/bff/bff-web/pom.xml clean package
# OCR 对外网关：独立进程、零外部依赖（不连 Nacos/DB），单独 pom，只产一个 jar。
# 2026-10-28 补：此前只在 ① 手工构建，统一构建/导出/compose 里都没有它，导致
# 任何「构建产物看 bff 目录」的场景只看到 bff-web，误以为网关没建。
# 2026-10-31 补：开源分发树不带 bff-ocr（商业模块裁切），存在才建——
# 无条件 -f 不存在的 pom 会让开源树 builder 必炸（POM file does not exist）。
if [ -f "$REPO_ROOT/backend/bff/bff-ocr/pom.xml" ]; then
  "$MVN" "${mvn_settings_args[@]}" "${flags[@]}" -f backend/bff/bff-ocr/pom.xml clean package
fi
mkdir -p "$JARS_OUT_DIR"
# jar 收集清单与构建同步收敛：开源树没有 bff-ocr 时不能把它列入必交付清单
# （否则 Missing service artifact 直接判死），有则照常归集。
COLLECT_SERVICES="${services//,/ } bff-web"
if [ -f "$REPO_ROOT/backend/bff/bff-ocr/target/bff-ocr.jar" ]; then
  COLLECT_SERVICES="$COLLECT_SERVICES bff-ocr"
fi
for service in $COLLECT_SERVICES; do
  case "$service" in
    bff-web) jar="backend/bff/bff-web/target/bff-web.jar" ;;
    bff-ocr) jar="backend/bff/bff-ocr/target/bff-ocr.jar" ;;
    *) jar="backend/paas/$service/target/$service.jar" ;;
  esac
  if [ ! -f "$jar" ]; then echo "Missing service artifact: $jar" >&2; exit 1; fi
  if ! unzip -p "$jar" META-INF/MANIFEST.MF | grep -q 'Start-Class:'; then
    echo "Not an executable service JAR: $jar" >&2; exit 1
  fi
  cp "$jar" "$JARS_OUT_DIR/$service.jar"
done

# ---------------------------------------------------------------- 插件 jar
# 商城域 8 个插件（hostService=mall）是 embedded 运行时：宿主插件引擎只认落盘 jar
# （契约 {pluginsRoot}/{code}/{version}/{code}-{version}.jar，见 PluginArtifactStore），
# 不认源码。它们的 pom 刻意没登记进 paas reactor —— 插件要能独立升级、不该跟着宿主
# 版本走 —— 所以必须在这里单独构建。不建这一步，「11 个插件默认安装」在开源包里
# 根本没有 jar 可装，插件中心只能是空表。
#
# 只构建带 runtime/java 的插件：unilogin 的后端在 auth 服务里、mall-distribution 的
# 在 mall 服务里（都是宿主原生 Controller），没有插件 jar，它们只需 register.sql
# 登记 + 前端产物。
PLUGIN_JARS_OUT="$JARS_OUT_DIR/plugins"
built_plugins=0
for ppom in plugins/*/*/pom.xml; do
  [ -f "$ppom" ] || continue
  pdir=$(dirname "$ppom")
  [ -d "$pdir/runtime/java" ] || continue
  code=$(awk '/^code:/{print $2; exit}' "$pdir/plugin.yaml" 2>/dev/null)
  ver=$(awk '/^version:/{print $2; exit}' "$pdir/plugin.yaml" 2>/dev/null)
  if [ -z "$code" ] || [ -z "$ver" ]; then
    echo "插件清单缺 code/version：$pdir/plugin.yaml" >&2; exit 1
  fi
  "$MVN" "${mvn_settings_args[@]}" "${flags[@]}" -q -f "$ppom" clean package
  pjar=$(find "$pdir/target" -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -1)
  if [ ! -f "$pjar" ]; then echo "插件 jar 未产出：$pdir" >&2; exit 1; fi
  mkdir -p "$PLUGIN_JARS_OUT/$code/$ver"
  cp "$pjar" "$PLUGIN_JARS_OUT/$code/$ver/$(basename "$pjar")"
  built_plugins=$((built_plugins + 1))
done
printf 'Built %s plugin JARs into %s\n' "$built_plugins" "$PLUGIN_JARS_OUT"
printf 'Built %s independent services and 2 BFF in %s\n' "$(echo "$services" | tr ',' '\n' | wc -l | tr -d ' ')" "$JARS_OUT_DIR"
