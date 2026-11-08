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

count=$(find "$DEST" -type f -name '*.jar' | wc -l | tr -d ' ')
echo "[plugin-seed] 插件卷内共 ${count} 个 jar："
find "$DEST" -type f -name '*.jar' | sort | sed 's|^|  |'
