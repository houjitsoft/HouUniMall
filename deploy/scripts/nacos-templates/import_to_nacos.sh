#!/usr/bin/env bash
# 把渲染后的配置批量导入 Nacos（幂等：同名 dataId 直接覆盖）。
#
# 前置：
#   1) python3 apply_templates.py         # templates + variables.env -> build/
#   2) 环境变量：NACOS_ADDR / NACOS_NAMESPACE / NACOS_USER / NACOS_PASSWORD
# 用法：
#   bash import_to_nacos.sh
# 说明：
#   - build/<group>/<dataId>.yaml 中的 <group> 即 Nacos group（如 DEFAULT_GROUP）。
#   - 命名空间用命名空间 ID（不是名称），未指定则写入 public 命名空间。
set -euo pipefail

NACOS_ADDR="${NACOS_ADDR:-127.0.0.1:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USER="${NACOS_USER:-}"
NACOS_PASSWORD="${NACOS_PASSWORD:-}"
BUILD_DIR="$(cd "$(dirname "$0")" && pwd)/build"

[ -d "$BUILD_DIR" ] || { echo "!! 未找到 build/，请先执行 python3 apply_templates.py"; exit 1; }

ACCESS_TOKEN=""
if [ -n "$NACOS_USER" ] && [ -n "$NACOS_PASSWORD" ]; then
  ACCESS_TOKEN=$(curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
    -d "username=${NACOS_USER}&password=${NACOS_PASSWORD}" \
    | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  [ -n "$ACCESS_TOKEN" ] || { echo "!! Nacos 登录失败，请检查用户名/密码"; exit 1; }
fi

ok=0; fail=0
for f in "$BUILD_DIR"/*/*.yaml; do
  group="$(basename "$(dirname "$f")")"
  data_id="$(basename "$f")"
  args=(--data-urlencode "dataId=${data_id}" --data-urlencode "group=${group}"
        --data-urlencode "type=yaml" --data-urlencode "content@${f}")
  [ -n "$NACOS_NAMESPACE" ] && args+=(--data-urlencode "tenant=${NACOS_NAMESPACE}")
  [ -n "$ACCESS_TOKEN" ] && args+=(--data-urlencode "accessToken=${ACCESS_TOKEN}")
  if curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" "${args[@]}" | grep -q true; then
    echo "  [ok]   ${group}/${data_id}"
    ok=$((ok+1))
  else
    echo "  [FAIL] ${group}/${data_id}"
    fail=$((fail+1))
  fi
done

echo "==> 完成：成功 ${ok}，失败 ${fail}"
[ "$fail" -eq 0 ] || exit 1
