#!/bin/sh
# =====================================================================
# 插件登记（一次性容器，幂等）
#
# 逐个执行 plugins/<域>/<插件>/deploy/register.sql，各脚本自身是幂等的，
# 重复执行只更新不重复插入。登记内容（每个插件四件事）：
#   1. hou_addon_plugin        插件中心条目
#   2. hou_addon_plugin_install 安装记录 installed=1 / enabled=1
#   3. hou_permission_menu     后台菜单（plugin_code 关联，role=1 自动授权）
#   4. hou_market_plugin       市场条目（FREE/0 元）
#
# 为什么必须在装完库之后单独跑：这些 SQL 曾经全仓没有任何脚本执行，
# 结果「11 个插件默认安装」在开源包里从未真正生效 —— 插件中心永远是空表，
# 而后台照着文档点「插件中心」只能看到一片空白。
# =====================================================================
set -eu

SRC="${PLUGIN_SOURCE_DIR:-/src/plugins}"
DB_HOST="${MYSQL_HOST:-mysql}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_USER="${MYSQL_USER:-meoc}"
DB_NAME="${MYSQL_DATABASE:-meoc}"
DB_PASS="${MYSQL_PWD:-${MYSQL_PASSWORD:-}}"

export MYSQL_PWD="$DB_PASS"

mysql_exec() {
  # 🔴 必须显式指定 utf8mb4：各插件 register.sql 里的插件名/描述/分类都是 UTF-8 中文，
  #    mysql 客户端在非 UTF-8 locale 的容器里默认按 latin1 协商，会把「统一登录」写成
  #    「ç»Ÿä¸€ç™»å½•」（双重编码）。整个安装链路没有第二处兜底，漏掉这一处就等于
  #    每个新装站的插件中心全是乱码。
  mysql --protocol=TCP --default-character-set=utf8mb4 \
    -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" "$@"
}

# 库没初始化完就等：mysql 容器 healthy 只代表进程起得来，704 张表还在灌。
i=0
while [ "$i" -lt 60 ]; do
  if mysql_exec -N -e "SELECT 1 FROM hou_addon_plugin LIMIT 1" >/dev/null 2>&1; then
    break
  fi
  i=$((i + 1))
  sleep 2
done
if ! mysql_exec -N -e "SELECT 1 FROM hou_addon_plugin LIMIT 1" >/dev/null 2>&1; then
  echo "[plugin-register] 等待 hou_addon_plugin 超时（库可能未初始化完成）" >&2
  exit 1
fi

ok=0
fail=0
for f in "$SRC"/*/*/deploy/register.sql; do
  [ -f "$f" ] || continue
  name=$(printf '%s' "$f" | awk -F/ '{print $(NF-3)"/"$(NF-2)}')
  if mysql_exec < "$f" 2>/tmp/reg-err.log; then
    echo "[plugin-register] ok   $name"
    ok=$((ok + 1))
  else
    echo "[plugin-register] FAIL $name" >&2
    sed 's/^/    /' /tmp/reg-err.log >&2
    fail=$((fail + 1))
  fi
done

total=$(mysql_exec -N -e "SELECT COUNT(*) FROM hou_addon_plugin_install WHERE installed=1 AND deleted=0" 2>/dev/null || echo '?')
echo "[plugin-register] 完成：成功 ${ok} / 失败 ${fail}，已安装记录 ${total} 条"

# 有一条失败就退出非零：让安装脚本停下，而不是装出「菜单有、点进去 500」的半成品。
[ "$fail" -eq 0 ]
