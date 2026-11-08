#!/usr/bin/env bash
# 按需执行某个插件的建表脚本（商城核心装好、且已安装该插件之后才执行）。
#
# 用法：
#   export MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=xxx
#   bash install_plugin_sql.sh erp          # 执行 init/plugins/erp.sql
#   bash install_plugin_sql.sh --list       # 列出可用的插件脚本
#
# 说明：
# - 插件是独立制品，装完商城后由用户在「插件中心」按需安装；本脚本只处理它的建表 SQL。
# - 2026-09-17 起 10 个插件（erp / mes / oa / orderfood / crm / cms / pm / webpos / finance / im）
#   全部在 plugin.yaml 声明了 tables.migrate，后台安装时由平台自动建表（取插件包内 db/<code>.sql，
#   与本目录同源）。本脚本保留用于手工补表、排查结构差异，以及自动建表失败后的兜底。
# - 全部 CREATE TABLE 均带 IF NOT EXISTS，可重复执行；只建表，不删数据。
set -euo pipefail

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
DB_NAME="${DB_NAME:-meoc}"
DIR="$(cd "$(dirname "$0")" && pwd)/init/plugins"

if [ "${1:-}" = "--list" ] || [ -z "${1:-}" ]; then
  echo "可用插件建表脚本："
  ls "$DIR" 2>/dev/null | sed 's/\.sql$//' | sed 's/^/  - /'
  echo "用法：bash install_plugin_sql.sh <code>"
  exit 0
fi

CODE="$1"
FILE="$DIR/${CODE}.sql"
[ -f "$FILE" ] || { echo "!! 没有 $CODE 的建表脚本（可用：$(ls "$DIR" | sed 's/\.sql$//' | tr '\n' ' ')）"; exit 1; }

MYSQL="mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER}"
[ -n "$MYSQL_PASSWORD" ] && MYSQL="$MYSQL -p${MYSQL_PASSWORD}"

echo "==> 为插件「${CODE}」建表：$FILE -> ${DB_NAME}"
$MYSQL --default-character-set=utf8mb4 "$DB_NAME" < "$FILE"
echo "==> 完成（幂等，可重复执行）。若插件仍在后台未启用，请在「插件中心」安装并启用后再验收菜单与接口。"
