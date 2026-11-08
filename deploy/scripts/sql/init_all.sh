#!/usr/bin/env bash
# HouCloud 数据库一键初始化（Linux / macOS）
# 用法：
#   export MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=xxx
#   export I_ACCEPT_EULA=yes    # 非交互必须：表示已阅读并同意开源 EULA
#   bash init_all.sh            # 全新安装（建库 + Core 全量表结构 + Core 增量迁移 + 初始数据）
#   bash init_all.sh --schema-only   # 执行核心结构及控制面迁移，不写初始数据
#   说明：本脚本只安装商城核心（Core），不创建任何插件表；插件装好后按需执行 install_plugin_sql.sh。
#
# 🔴 执行顺序的唯一权威来源是 scripts/sql/init/order.txt（Docker 轨的
#    deploy/mysql-init/00-init.sql 由同一份清单渲染生成）。不要在本脚本里再维护一份顺序。
set -euo pipefail

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
DB_NAME="meoc"
SCHEMA_ONLY=0
SKIP_DEMO_TENANT="${SKIP_DEMO_TENANT:-0}"
[ "${1:-}" = "--schema-only" ] && SCHEMA_ONLY=1
# 演示租户（105-tenant-seed.sql）在对外公开的生产部署里通常不需要：
#   SKIP_DEMO_TENANT=1 bash init_all.sh
# 默认装上，是因为「按站点买授权 / 看订单」这条链路没有组织就无可验证对象。
[ "${1:-}" = "--skip-demo-tenant" ] && SKIP_DEMO_TENANT=1

SQL_ROOT="$(cd "$(dirname "$0")" && pwd)"
DIR="$SQL_ROOT/init"
ORDER_FILE="$DIR/order.txt"
MYSQL="mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER}"
[ -n "$MYSQL_PASSWORD" ] && MYSQL="$MYSQL -p${MYSQL_PASSWORD}"

# 未勾选/未确认 EULA 不得继续
bash "$SQL_ROOT/../install/accept-eula.sh"

[ -f "$ORDER_FILE" ] || { echo "!! 缺少顺序清单 $ORDER_FILE"; exit 1; }

# ---- 解析唯一有序清单 ----
SCHEMA_FILES=()
DATA_FILES=()
STAGE=""
while IFS= read -r raw; do
  line="${raw%%#*}"
  line="$(printf '%s' "$line" | tr -d '\r' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
  [ -z "$line" ] && continue
  case "$line" in
    "[schema]") STAGE="schema"; continue ;;
    "[data]")   STAGE="data";   continue ;;
  esac
  [ -f "$DIR/$line" ] || { echo "!! order.txt 引用了不存在的脚本：$line"; exit 1; }
  case "$STAGE" in
    schema) SCHEMA_FILES+=("$line") ;;
    data)   DATA_FILES+=("$line") ;;
    *) echo "!! order.txt 里的 $line 没有归属阶段（缺 [schema]/[data] 标记）"; exit 1 ;;
  esac
done < "$ORDER_FILE"

# +1 是「建库」那一步，否则进度会显示成 18/17 这种自相矛盾的编号
TOTAL=$(( ${#SCHEMA_FILES[@]} + ${#DATA_FILES[@]} + 1 ))

echo "==> 目标：${MYSQL_HOST}:${MYSQL_PORT}  库：${DB_NAME}"
$MYSQL -e "SELECT VERSION();" >/dev/null || { echo "!! 连接失败，请检查 MYSQL_HOST/PORT/USER/PASSWORD"; exit 1; }

echo "==> [1/${TOTAL}] 建库"
$MYSQL < "$DIR/00-create-database.sql"

i=1
echo "==> schema 阶段（建表 / 加列 / 改表）"
for f in "${SCHEMA_FILES[@]}"; do
  i=$((i + 1))
  echo "==> [${i}/${TOTAL}] ${f}"
  $MYSQL --default-character-set=utf8mb4 "$DB_NAME" < "$DIR/$f"
done

# 安装 EULA 由部署者在上方确认；协议表（21-agreement-consent.sql）已在 schema 阶段建好，
# 这里写入站点级配置，不关联任何站内用户。
EULA_MARKER="$SQL_ROOT/../install/.eula-accepted"
EULA_ACCEPTED_VERSION=""
EULA_ACCEPTED_TIME=""
EULA_ACCEPTED_BY="installer"
while IFS='=' read -r key value; do
  case "$key" in
    accepted_eula_version) EULA_ACCEPTED_VERSION="$value" ;;
    accepted_eula_time) EULA_ACCEPTED_TIME="$value" ;;
    accepted_by) EULA_ACCEPTED_BY="$value" ;;
  esac
done < "$EULA_MARKER"
[ -n "$EULA_ACCEPTED_VERSION" ] && [ -n "$EULA_ACCEPTED_TIME" ] || {
  echo "!! 安装 EULA 接受记录不完整，无法写入数据库"
  exit 1
}
EULA_ACCEPTED_VERSION_SQL="${EULA_ACCEPTED_VERSION//\'/\'\'}"
EULA_ACCEPTED_TIME_SQL="${EULA_ACCEPTED_TIME//\'/\'\'}"
EULA_ACCEPTED_BY_SQL="${EULA_ACCEPTED_BY//\'/\'\'}"
$MYSQL --default-character-set=utf8mb4 "$DB_NAME" -e "
INSERT INTO hou_base_install_agreement_acceptance
  (agreement_code, version_no, accepted_time, accepted_by, source)
VALUES
  ('OPEN_SOURCE_EULA', '${EULA_ACCEPTED_VERSION_SQL}', '${EULA_ACCEPTED_TIME_SQL}', '${EULA_ACCEPTED_BY_SQL}', 'installer');

UPDATE hou_base_sys_configs
SET config_value = CASE config_key
  WHEN 'accepted_eula_version' THEN '${EULA_ACCEPTED_VERSION_SQL}'
  WHEN 'accepted_eula_time' THEN '${EULA_ACCEPTED_TIME_SQL}'
  ELSE config_value
END,
update_by = 'installer'
WHERE config_key IN ('accepted_eula_version', 'accepted_eula_time');"
echo "==> 安装者 EULA 接受记录已写入站点数据库（${EULA_ACCEPTED_VERSION}）"

if [ "$SCHEMA_ONLY" -eq 1 ]; then
  echo "==> 已跳过初始数据（--schema-only，data 阶段 ${#DATA_FILES[@]} 个脚本未执行）"
else
  echo "==> data 阶段（初始数据：菜单 / 角色 / 管理员 / 插件分类导航）"
  for f in "${DATA_FILES[@]}"; do
    i=$((i + 1))
    if [ "$f" = "105-tenant-seed.sql" ] && [ "$SKIP_DEMO_TENANT" = "1" ]; then
      echo "==> [${i}/${TOTAL}] ${f}（已按 SKIP_DEMO_TENANT 跳过演示租户）"
      continue
    fi
    echo "==> [${i}/${TOTAL}] ${f}"
    $MYSQL --default-character-set=utf8mb4 "$DB_NAME" < "$DIR/$f"
  done
  echo "==> 初始管理员：admin / Admin@123456（请首次登录后立即修改）"
fi

TABLES=$($MYSQL -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';")
echo "==> 完成。${DB_NAME} 当前共 ${TABLES} 张表。"
echo "==> 本次只安装了商城核心（Core），未创建任何插件表。"
echo "    插件请在商城装好并登录后台后，按需在「插件中心」单独安装；"
echo "    需要手工补插件表时执行：bash install_plugin_sql.sh <code>   （可选 code：$(ls "$DIR/plugins" 2>/dev/null | sed 's/\.sql$//' | tr '\n' ' '))"
