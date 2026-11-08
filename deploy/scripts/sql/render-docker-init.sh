#!/usr/bin/env bash
# =====================================================================
# 从 deploy/scripts/sql/init/order.txt 生成 deploy/mysql-init/00-init.sql
#
# 为什么要有这个脚本：Docker 轨（docker-entrypoint-initdb.d）与传统轨
# （deploy/scripts/sql/init_all.sh）以前各自硬编码了一份 SQL 执行顺序，结果 Docker
# 那份漏了 6 个必需脚本，permission 服务启动即崩、交付表缺失。
# 现在唯一清单是 order.txt，本脚本负责把它渲染成 Docker 入口能执行的
# `source` 语句；`--check` 供提交前 / CI 校验生成物没有漂移。
#
# 用法：
#   bash deploy/scripts/sql/render-docker-init.sh          # 生成/覆盖 00-init.sql
#   bash deploy/scripts/sql/render-docker-init.sh --check  # 只校验是否与 order.txt 一致
# =====================================================================
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
INIT_DIR="$HERE/init"
ORDER_FILE="$INIT_DIR/order.txt"
TARGET="$(cd "$HERE/../../.." && pwd)/deploy/mysql-init/00-init.sql"

CHECK=0
[ "${1:-}" = "--check" ] && CHECK=1

[ -f "$ORDER_FILE" ] || { echo "!! 缺少 $ORDER_FILE"; exit 1; }

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
  [ -f "$INIT_DIR/$line" ] || { echo "!! order.txt 引用了不存在的脚本：$line"; exit 1; }
  case "$STAGE" in
    schema) SCHEMA_FILES+=("$line") ;;
    data)   DATA_FILES+=("$line") ;;
    *) echo "!! order.txt 里的 $line 没有归属阶段（缺 [schema]/[data] 标记）"; exit 1 ;;
  esac
done < "$ORDER_FILE"

[ "${#SCHEMA_FILES[@]}" -gt 0 ] || { echo "!! order.txt 未列出任何 [schema] 脚本"; exit 1; }

render() {
  cat <<'HEADER'
-- =====================================================================
-- Docker 轨：MySQL 容器首次启动时的数据库初始化（docker-entrypoint-initdb.d）
-- 只在数据卷为空时执行一次；改过数据后要重来请 `bash deploy/scripts/docker.sh reset`。
--
-- 🔴 本文件由 deploy/scripts/sql/render-docker-init.sh 从 deploy/scripts/sql/init/order.txt 生成，
--    请勿手改。改顺序改 order.txt，然后重跑生成脚本并一起提交。
--    校验：bash deploy/scripts/sql/render-docker-init.sh --check
--
-- 这里只装商城核心（Core）：建库 + 基表 + 增量迁移 + 菜单/角色/管理员。
-- 插件表不在这里建，插件是装完商城后在后台按需安装的独立制品。
--
-- 🔴 库名一律不写死。官方 entrypoint 会先按 MYSQL_DATABASE 建库，再以
--    `mysql --database="$MYSQL_DATABASE"` 执行本文件，所以这里既不需要
--    `CREATE DATABASE`，也不需要 `USE`。
--    2026-10-27 实测：这里原先写死 `USE \`meoc\``，把 MYSQL_DATABASE 配成别的名字时
--    compose 那行配置看起来生效、实际上所有表都落在 meoc 库，服务连新库直接报表不存在；
--    更糟的情形是 meoc 也不存在时，700 多条 DDL 逐条报「无库可用」而初始化进程还在跑，
--    表现为「装了很久没动静」。
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

HEADER

  echo "-- ---------- schema 阶段（建表 / 加列 / 改表） ----------"
  local f
  for f in "${SCHEMA_FILES[@]}"; do
    echo "source /opt/houcloud-sql/$f;"
  done
  if [ "${#DATA_FILES[@]}" -gt 0 ]; then
    echo ""
    echo "-- ---------- data 阶段（初始数据：菜单 / 角色 / 管理员） ----------"
    for f in "${DATA_FILES[@]}"; do
      echo "source /opt/houcloud-sql/$f;"
    done
  fi
  cat <<'FOOTER'

SET FOREIGN_KEY_CHECKS = 1;
FOOTER
}

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
render > "$TMP"

if [ "$CHECK" -eq 1 ]; then
  if diff -u "$TARGET" "$TMP" > /dev/null 2>&1; then
    echo "OK：deploy/mysql-init/00-init.sql 与 order.txt 一致（schema ${#SCHEMA_FILES[@]} 个 / data ${#DATA_FILES[@]} 个）"
    exit 0
  fi
  echo "!! deploy/mysql-init/00-init.sql 与 order.txt 不一致，差异如下："
  diff -u "$TARGET" "$TMP" || true
  echo "   修复：bash deploy/scripts/sql/render-docker-init.sh"
  exit 1
fi

mkdir -p "$(dirname "$TARGET")"
cp "$TMP" "$TARGET"
echo "已生成 ${TARGET}（schema ${#SCHEMA_FILES[@]} 个 / data ${#DATA_FILES[@]} 个）"
