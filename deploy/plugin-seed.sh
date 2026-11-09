#!/bin/sh
# =====================================================================
# 插件制品投放（一次性容器，幂等）
#
# builder 镜像一次性构建了「服务 jar + 插件 jar」。服务 jar 由各服务镜像
# 用 `COPY --from` 取走，插件 jar 没有这层搬运 —— 宿主插件引擎只认磁盘上的
# 落盘契约：
#   {pluginsRoot}/{code}/{version}/{code}-{version}.jar
# 且 pluginsRoot 在 compose 里是宿主目录（PLUGINS_DIR），不在镜像里。
# 本脚本负责把镜像内的 /jars/plugins 铺到那个宿主目录。
#
# 为什么必须单独一步：插件是 embedded 运行时，源码不参与宿主启动；不铺 jar
# 时宿主引擎扫到一个空目录，插件中心有记录、后台菜单有入口，点进去全是 503。
# =====================================================================
set -eu

SRC="${PLUGIN_ARTIFACT_DIR:-/jars/plugins}"
DEST="${PLUGINS_DEST:-/dest}"

mkdir -p "$DEST"

if [ ! -d "$SRC" ]; then
  echo "[plugin-seed] 镜像内没有插件制品目录 $SRC"
  echo "[plugin-seed] 说明这次构建没产出插件 jar（build-jars.sh 的插件步骤未执行）"
  exit 0
fi

# cp -a 保留目录结构；重复执行是覆盖，不产生 <code>/<version>/<code>/<version> 套娃。
cp -a "$SRC/." "$DEST/"

# ---------------------------------------------------------------- 制品文件名归一
# maven 产出的 jar 名是 <artifactId>-<pom.version>.jar，而 pom 里的版本是构件版本
# （1.0.0/1.0.2 这类），跟 plugin.yaml 的发布版本不是一回事：插件升到 1.0.3 之后，
# 目录是 1.0.3、jar 还叫 xxx-plugin-1.0.2.jar。安装记录（jar_path）与应用启动时的
# PluginRuntimeBootstrap 都是按路径精确取的，名字一旦漂移就「记录在、包找不到」，
# 插件静默不加载。
# 这里统一改名为 <code>-plugin-<plugin.yaml version>.jar，与 register.sql 的登记口径一致。
renamed=0
for d in "$DEST"/*/*; do
  [ -d "$d" ] || continue
  code=$(basename "$(dirname "$d")")
  ver=$(basename "$d")
  for j in "$d"/*.jar; do
    [ -f "$j" ] || continue
    case "$j" in *-sources.jar|*-javadoc.jar) continue ;; esac
    target="$d/$code-plugin-$ver.jar"
    [ "$j" = "$target" ] && continue
    mv -f "$j" "$target"
    renamed=$((renamed + 1))
  done
done
[ "$renamed" -gt 0 ] && echo "[plugin-seed] 制品文件名归一 ${renamed} 个（<code>-plugin-<版本>.jar）"

count=$(find "$DEST" -type f -name '*.jar' | wc -l | tr -d ' ')
echo "[plugin-seed] 插件卷内共 ${count} 个 jar："
find "$DEST" -type f -name '*.jar' | sort | sed 's|^|  |'

# ---------------------------------------------------------------- 前端产物
# 插件页面不是编译进管理端的，而是运行时 `import('/plugin-assets/<code>/<version>/index.mjs')`。
# base 的 PluginCenterService.frontendArtifactOnDisk 在同一目录判定「这个插件到底有没有前端」：
# 目录里没有 index.mjs 就不下发 frontendUrl，前端 PluginPending 立刻判「无前端入口」并渲染
# 「页面不存在」—— 用户从插件中心点任何插件都是 404，看着像路由坏了，其实只是产物没铺。
# 所以前端产物必须和 jar 一起落到 {pluginsRoot}/{code}/{version}/frontend/。
SRC_TREE="${PLUGIN_SOURCE_DIR:-/src-tree}"
if [ -d "$SRC_TREE/plugins" ]; then
  seeded=0
  skipped=""
  for pdir in "$SRC_TREE"/plugins/*/*; do
    [ -f "$pdir/plugin.yaml" ] || continue
    code=$(awk '/^code:/{print $2; exit}' "$pdir/plugin.yaml" 2>/dev/null)
    ver=$(awk '/^version:/{print $2; exit}' "$pdir/plugin.yaml" 2>/dev/null)
    [ -n "$code" ] && [ -n "$ver" ] || continue
    if [ ! -f "$pdir/frontend/index.mjs" ]; then
      skipped="$skipped $code"
      continue
    fi
    mkdir -p "$DEST/$code/$ver/frontend"
    cp -a "$pdir/frontend/." "$DEST/$code/$ver/frontend/"
    seeded=$((seeded + 1))
  done
  echo "[plugin-seed] 前端产物投放 ${seeded} 个插件"
  if [ -n "$skipped" ]; then
    echo "[plugin-seed] 以下插件源码树无 frontend/index.mjs，页面点开会是「页面不存在」：${skipped}"
  fi
else
  echo "[plugin-seed] 未挂载源码树（$SRC_TREE），跳过前端产物投放"
fi
