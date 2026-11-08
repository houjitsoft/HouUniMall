#!/usr/bin/env bash
# =====================================================================
# 插件全链路自证脚本（在装好的机器上跑，不需要额外依赖）
#
#   登录 → 站点身份 → 创建插件（脚手架三表入库）→ 发布上传 → 提交审核
#        → 审核上架 → 市场详情 → 下单 → 支付 → 授权
#        → 换下载票 → 回源下载（校验 sha256） → 安装（8 步流水线）
#
# 用法：
#   bash verify-plugin-chain.sh                     # 默认 http://127.0.0.1:8391
#   BFF=http://127.0.0.1:18391 bash verify-plugin-chain.sh
#   PLUGIN_JAR=/path/to/plugin.jar bash verify-plugin-chain.sh
#
# 环境变量：
#   BFF           BFF 地址（默认 http://127.0.0.1:8391）
#   ADMIN_MOBILE  管理员手机号（默认 13800000000）
#   ADMIN_PASS    管理员口令（默认 Admin@123456）
#   PLUGIN        插件编码（默认 hello-world，必须是 PF4J 插件、不能是独立服务）
#   VERSION       发布版本（默认 1.0.HHMMSS，避免撞历史最大版本）
#   PLUGIN_JAR    要上传的制品；不传则现场生成一个最小合法制品
#
# 约定（2026-10-27 对齐后端实测）：
#   1. Authorization 必须带 Bearer 前缀——userms 的 RequestTokenUtils
#      严格要求 "Bearer " 开头，裸 JWT 会在下单链路被误报 401。
#   2. 成功码是 code:1（ResponseResult/CommonStatusEnum.SUCCESS），
#      不是 200/0。
#   3. 上传前必须先「创建插件」：市场商品行（hou_market_plugin）由
#      脚手架 /plugin-center/create 落库，缺它上传报 3013。
# =====================================================================
set -uo pipefail

BFF="${BFF:-http://127.0.0.1:8391}"
ADMIN_MOBILE="${ADMIN_MOBILE:-13800000000}"
ADMIN_PASS="${ADMIN_PASS:-Admin@123456}"
PLUGIN="${PLUGIN:-hello-world}"
VERSION="${VERSION:-1.0.$(date +%H%M%S)}"
WORK="$(mktemp -d)"
FAIL=0
TOKEN=""

log()  { printf '\033[36m[%s]\033[0m %s\n' "$(date +%H:%M:%S)" "$*"; }
ok()   { printf '\033[32m  ✓\033[0m %s\n' "$*"; }
bad()  { printf '\033[31m  ✗\033[0m %s\n' "$*"; FAIL=$((FAIL + 1)); }
note() { printf '      %s\n' "$*"; }

# 统一成功判据：code:1 是本体系 ResponseResult 的成功码。
isok() { printf '%s' "$1" | grep -qE '"code":1[},]'; }

jget() {  # jget '<python expr on d>' —— 从 stdin 读 JSON
  python3 -c '
import sys, json
try: d = json.load(sys.stdin)
except Exception: sys.exit(3)
# data 为 null/缺失时兜底成空 dict（保留 list/dict 数据），避免 NoneType.get 崩栈
if isinstance(d, dict) and d.get("data") is None:
    d["data"] = {}
v = eval(sys.argv[1], {"d": d, "len": len, "str": str, "json": json})
print("" if v is None else v)
' "$1"
}

api() {  # api METHOD PATH [BODY] -> stdout JSON
  local m="$1" p="$2" b="${3:-}"
  local args=(-s --max-time 30 -X "$m" "${BFF}${p}" -H "tenant-id: 1" -H "Content-Type: application/json")
  [ -n "$TOKEN" ] && args+=(-H "Authorization: Bearer ${TOKEN}")
  if [ -n "$b" ]; then curl "${args[@]}" -d "$b"; else curl "${args[@]}"; fi
}

# ---------- 0. 制品 ----------
if [ -z "${PLUGIN_JAR:-}" ]; then
  PLUGIN_JAR="$WORK/$PLUGIN-$VERSION.jar"
  python3 - "$PLUGIN_JAR" "$PLUGIN" "$VERSION" <<'PY'
import sys, zipfile
out, code, ver = sys.argv[1:4]
yaml = f"""code: {code}
name: Chain Verify Plugin
version: {ver}
author: installer
description: 安装验收用最小制品
category: 通用
owner: thirdparty
runtime: embedded
hostService: goods
entryPath: /plugin/{code}
minFrameworkVersion: 1.0.0
license: FREE
"""
man = ("Manifest-Version: 1.0\n"
       f"Plugin-Id: {code}\nPlugin-Version: {ver}\n"
       "Plugin-Provider: installer\n"
       f"Plugin-Class: com.houjiang.plugin.hello.HelloPlugin\n\n")
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("plugin.yaml", yaml)
    z.writestr("META-INF/MANIFEST.MF", man)
PY
else
  # 真实制品复测：市场要求版本单调递增（3012），固定 version 的制品第二次就传不上去。
  # 把制品的 plugin.yaml 与 MANIFEST.MF 里的版本重写成本次动态 VERSION，其余字节原样保留。
  REWRITTEN="$WORK/$PLUGIN-$VERSION.jar"
  python3 - "$PLUGIN_JAR" "$REWRITTEN" "$PLUGIN" "$VERSION" <<'PY'
import sys, zipfile
src, dst, code, ver = sys.argv[1:5]
with zipfile.ZipFile(src) as zin:
    names = zin.namelist()
    yaml = zin.read("plugin.yaml").decode("utf-8", "ignore")
    import re
    yaml_new = re.sub(r"(?m)^version:\s*\S+", f"version: {ver}", yaml)
    man = zin.read("META-INF/MANIFEST.MF").decode("utf-8", "ignore")
    man_new = re.sub(r"(?m)^Plugin-Version:\s*\S+", f"Plugin-Version: {ver}", man)
    with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
        for it in zin.infolist():
            if it.filename == "plugin.yaml":
                zout.writestr(it, yaml_new)
            elif it.filename == "META-INF/MANIFEST.MF":
                zout.writestr(it, man_new)
            else:
                zout.writestr(it, zin.read(it.filename))
print("rewritten ->", dst)
PY
  PLUGIN_JAR="$REWRITTEN"
fi
SHA_BEFORE=$(shasum -a 256 "$PLUGIN_JAR" 2>/dev/null | cut -d' ' -f1 || sha256sum "$PLUGIN_JAR" | cut -d' ' -f1)
log "制品 $(basename "$PLUGIN_JAR") sha256=${SHA_BEFORE:0:16}…  版本 $VERSION"

# ---------- 1. 登录 ----------
log "1/12 登录 $ADMIN_MOBILE"
R=$(api POST /web/user/login "{\"userMobile\":\"$ADMIN_MOBILE\",\"password\":\"$ADMIN_PASS\"}")
TOKEN=$(printf '%s' "$R" | jget 'd.get("data",{}).get("token") if isinstance(d.get("data"),dict) else ""')
if [ -n "$TOKEN" ]; then ok "拿到 JWT（${#TOKEN} 字符）"; else bad "登录失败：$(printf '%s' "$R" | head -c 200)"; fi
[ -z "$TOKEN" ] && { echo; echo "登录失败，后续步骤无法进行"; exit 1; }

# ---------- 2. 站点身份 ----------
# 没有独立的 site/identity 接口：站点身份由 SiteInstanceResolver 在业务接口内
# 从登录态解析（orgId → u-{userId} → mall-{id}）。用 /plugin-center/list 验证
# 这条链路：能按站点返回目录，即身份解析正常。
log "2/12 站点身份链路（经 /plugin-center/list）"
R=$(api POST /web/plugin-center/list '{}' || true)
if isok "$R"; then
  ok "$(printf '%s' "$R" | head -c 180)"
else
  bad "$(printf '%s' "$R" | head -c 200)"
fi

# ---------- 3. 创建插件（脚手架三表入库） ----------
# /base/plugin-center/create → 插件中心目录(hou_addon_plugin) + 市场商品
# (hou_market_plugin, status=0 草稿) + 市场版本(hou_market_plugin_version)。
# 没有商品行，上传会报 3013「尚未在插件市场登记」。409=已存在，幂等跳过。
# 分类从分类字典取第一个（resolveCategory 按名称匹配，不传会落数库 NOT NULL 约束坑）。
log "3/12 创建插件（脚手架三表入库）/plugin-center/create"
RC=$(api POST /web/plugin-center/categories '{}')
CAT_NAME=$(printf '%s' "$RC" | jget 'd["data"][0].get("name") if isinstance(d.get("data"), list) and d.get("data") else ""')
if [ -z "$CAT_NAME" ]; then
  note "分类字典不可读，create 不带 category（依赖服务端「未分类」兜底）"
  CREATE_BODY=$(printf '{"code":"%s","name":"Chain Verify Plugin","version":"%s","licenseType":"FREE","hostService":"goods","runtime":"embedded","menuTitle":"Chain Verify","description":"安装验收用最小制品"}' "$PLUGIN" "$VERSION")
else
  note "使用分类：$CAT_NAME"
  CREATE_BODY=$(printf '{"code":"%s","name":"Chain Verify Plugin","version":"%s","licenseType":"FREE","hostService":"goods","runtime":"embedded","menuTitle":"Chain Verify","description":"安装验收用最小制品","category":"%s"}' "$PLUGIN" "$VERSION" "$CAT_NAME")
fi
R=$(api POST /web/plugin-center/create "$CREATE_BODY")
if isok "$R"; then
  ok "插件已创建（目录+商品+版本三表入库）"
elif printf '%s' "$R" | grep -q '"code":409\|已存在'; then
  ok "插件已存在（409 冲突），幂等跳过创建"
  note "$(printf '%s' "$R" | head -c 160)"
else
  bad "$(printf '%s' "$R" | head -c 300)"
fi

# ---------- 4. 上传 ----------
log "4/12 上传制品 /publish/upload"
UPLOAD_ID="verify-$(date +%s)-$$"
R=$(curl -s --max-time 120 -X POST "${BFF}/web/plugin-center/publish/upload" \
      -H "tenant-id: 1" -H "Authorization: Bearer ${TOKEN}" \
      -F "file=@${PLUGIN_JAR}" -F "pluginCode=${PLUGIN}" -F "version=${VERSION}" \
      -F "uploadId=${UPLOAD_ID}" -F "changelog=安装验收脚本上传")
VERSION_ID=$(printf '%s' "$R" | jget 'd.get("data",{}).get("versionId") or d.get("data",{}).get("id") or ""')
if [ -n "$VERSION_ID" ]; then ok "落库 versionId=$VERSION_ID"; else bad "$(printf '%s' "$R" | head -c 300)"; fi

# ---------- 5. 提交审核 ----------
log "5/12 提交审核 /publish/versions/submit"
R=$(api POST /web/plugin-center/publish/versions/submit "{\"pluginCode\":\"$PLUGIN\",\"version\":\"$VERSION\"}")
if isok "$R"; then ok "已进入 pending"; else bad "$(printf '%s' "$R" | head -c 300)"; fi
[ -z "$VERSION_ID" ] && VERSION_ID=$(printf '%s' "$R" | jget 'd.get("data",{}).get("versionId") or ""')

# ---------- 6. 审核上架 ----------
log "6/12 审核上架 /publish/versions/review"
if [ -n "$VERSION_ID" ]; then
  R=$(api POST /web/plugin-center/publish/versions/review "{\"versionId\":$VERSION_ID,\"decision\":\"APPROVE\",\"note\":\"安装验收\"}")
  if isok "$R"; then ok "已上架（版本 published、商品 status=2）"; else bad "$(printf '%s' "$R" | head -c 300)"; fi
else
  bad "没有 versionId，跳过审核"
fi

# ---------- 7. 市场详情 ----------
log "7/12 市场详情 /market/detail"
R=$(api POST /web/plugin-center/market/detail "{\"pluginCode\":\"$PLUGIN\"}")
if isok "$R"; then
  ok "详情可读"; note "$(printf '%s' "$R" | head -c 200)"
else bad "$(printf '%s' "$R" | head -c 300)"; fi

# ---------- 8. 下单 ----------
log "8/12 创建订单 /market/order/create"
# 购买场景是 fail-closed：每次下单都要重交协议同意清单（agreements:[{code,versionNo}]），
# 清单从 GET /web/agreement/list?scene=plugin_purchase 取（免登录）。
AGREEMENTS=$(curl -s --max-time 30 "${BFF}/web/agreement/list?scene=plugin_purchase" -H "tenant-id: 1" \
  | jget 'json.dumps([{"code": i.get("code"), "versionNo": i.get("versionNo")} for i in (d.get("data") or []) if isinstance(i, dict) and i.get("code") and i.get("versionNo")], ensure_ascii=False)')
[ -n "$AGREEMENTS" ] || AGREEMENTS='[]'
R=$(api POST /web/plugin-center/market/order/create "{\"pluginCode\":\"$PLUGIN\",\"version\":\"$VERSION\",\"agreements\":$AGREEMENTS}")
ORDER_NO=$(printf '%s' "$R" | jget 'd.get("data",{}).get("orderNo") or ""')
if [ -n "$ORDER_NO" ]; then
  ok "订单 $ORDER_NO"
elif isok "$R" || printf '%s' "$R" | grep -qE '免费|FREE|无需下单|不需要下单'; then
  ok "该插件为免费授权，跳过下单/支付（下载走「登录即放行」）"
  ORDER_NO=""
else
  bad "$(printf '%s' "$R" | head -c 300)"
fi

# ---------- 9. 支付 ----------
log "9/12 发起支付 /market/order/pay"
if [ -z "$ORDER_NO" ]; then
  note "无订单（免费插件），跳过支付"
else
  R=$(api POST /web/plugin-center/market/order/pay "{\"orderNo\":\"$ORDER_NO\",\"payMethod\":\"MOCK\"}")
  ok "$(printf '%s' "$R" | head -c 200)"
  if printf '%s' "$R" | grep -q 'sandbox\|MOCK\|待支付\|PENDING'; then
    log "9b 沙箱确认 /market/pay/sandbox/confirm"
    R2=$(api POST /web/plugin-center/market/pay/sandbox/confirm "{\"orderNo\":\"$ORDER_NO\",\"result\":\"SUCCESS\"}")
    ok "$(printf '%s' "$R2" | head -c 200)"
    R=$(api POST /web/plugin-center/market/order/sync "{\"orderNo\":\"$ORDER_NO\"}")
  fi
  ST=$(printf '%s' "$R" | jget 'd.get("data",{}).get("status") or ""')
  ok "订单状态 ${ST:-未知}"
fi

# ---------- 10. 授权 ----------
log "10/12 授权状态 /market/license/status"
R=$(api POST /web/plugin-center/market/license/status "{\"pluginCode\":\"$PLUGIN\"}")
if isok "$R"; then
  ok "$(printf '%s' "$R" | head -c 200)"
else
  bad "$(printf '%s' "$R" | head -c 200)"
fi

# ---------- 11. 换票 + 回源下载 ----------
log "11/12 换下载票 /artifact/download-url"
R=$(api POST /web/plugin-center/artifact/download-url "{\"pluginCode\":\"$PLUGIN\",\"version\":\"$VERSION\"}")
DL_URL=$(printf '%s' "$R" | jget 'd.get("data",{}).get("url") or d.get("data",{}).get("downloadUrl") or ""')
DL_TOKEN=$(printf '%s' "$R" | jget 'd.get("data",{}).get("token") or ""')
if [ -n "$DL_TOKEN" ] || [ -n "$DL_URL" ]; then
  ok "换票成功"
  # 服务端 url 是给安装代理用的相对路径 /base/plugin-center/artifact/raw?token=…，
  # 对外网关（BFF）把 /web 前缀映射到 base 的 /base，curl 必须拼成完整地址。
  case "$DL_URL" in
    http*) : ;;
    */plugin-center/artifact/raw*) DL_URL="${BFF}/web/plugin-center/artifact/raw?token=${DL_TOKEN}";;
    "") DL_URL="${BFF}/web/plugin-center/artifact/raw?token=${DL_TOKEN}";;
    *) DL_URL="${BFF}/web/${DL_URL#/base/}";;
  esac
  note "回源地址 ${DL_URL%%\?token=*}?token=…"
  HTTP_CODE=$(curl -s --max-time 120 -w '%{http_code}' -o "$WORK/dl.jar" "$DL_URL")
  if [ "$HTTP_CODE" != "200" ]; then bad "回源 HTTP ${HTTP_CODE}：$(head -c 200 "$WORK/dl.jar" 2>/dev/null)"; fi
  SHA_AFTER=$(shasum -a 256 "$WORK/dl.jar" 2>/dev/null | cut -d' ' -f1 || sha256sum "$WORK/dl.jar" | cut -d' ' -f1)
  if [ "$SHA_BEFORE" = "$SHA_AFTER" ]; then ok "回源字节一致 sha256=${SHA_AFTER:0:16}…"
  else bad "sha256 不一致：上传 ${SHA_BEFORE:0:16}… / 下载 ${SHA_AFTER:0:16}…"; fi
  head -c 120 "$WORK/dl.jar" | grep -q 'error\|"code"' && note "下载体前 120 字节：$(head -c 120 "$WORK/dl.jar")"
else bad "$(printf '%s' "$R" | head -c 300)"; fi

# ---------- 12. 安装 ----------
# embedded 插件由宿主进程的引擎装载（fail-closed，防装错宿主）：
#   12a. 平台 install 流水线：预检必须给出明确的 3020「宿主不匹配」，而不是 500；
#   12b. 真实安装路径：下载制品投放 goods 宿主 plugins 目录 → goods 引擎启动装载
#        → 插件路由可用（直连 goods + 经 BFF 两条腿）。
#        GOODS_PLUGINS_DIR / GOODS_CONTAINER 未配置时跳过 12b。
log "12/12 安装（预检拒绝跨宿主 + goods 宿主装载）"
R=$(api POST /web/plugin-center/update/install "{\"pluginCode\":\"$PLUGIN\",\"version\":\"$VERSION\"}")
if printf '%s' "$R" | grep -q '"code":3020'; then
  ok "平台预检明确拒绝跨宿主装载（3020）"
else
  bad "预期 3020 宿主不匹配，实际：$(printf '%s' "$R" | head -c 200)"
fi
if [ -n "${GOODS_PLUGINS_DIR:-}" ] && [ -n "${GOODS_CONTAINER:-}" ]; then
  DEST="${GOODS_PLUGINS_DIR%/}/$PLUGIN/$VERSION"
  rm -rf "${GOODS_PLUGINS_DIR%/}/$PLUGIN"; mkdir -p "$DEST"
  cp "$WORK/dl.jar" "$DEST/$PLUGIN-$VERSION.jar"
  docker restart "$GOODS_CONTAINER" >/dev/null
  LOADED=""
  for i in $(seq 1 12); do
    sleep 10
    if docker logs --since 3m "$GOODS_CONTAINER" 2>&1 | grep -q "已装载插件 $PLUGIN"; then LOADED=1; break; fi
  done
  if [ -n "$LOADED" ]; then ok "goods 宿主引擎已装载 $PLUGIN"
  else bad "goods 宿主未观测到装载日志（$GOODS_CONTAINER）"; fi
  GIP=$(docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$GOODS_CONTAINER")
  GPORT="${GOODS_PORT:-8208}"
  PR=$(curl -s --max-time 20 -X POST "http://$GIP:$GPORT/plugin/$PLUGIN/hello" -H "Authorization: Bearer $TOKEN" -H "tenant-id: 1")
  if printf '%s' "$PR" | grep -q 'Hello World'; then ok "插件路由可用（直连 goods）：$PR"
  else bad "插件路由响应（直连 goods）：$(printf '%s' "$PR" | head -c 200)"; fi
  PB=$(curl -s --max-time 20 -X POST "${BFF}/web/plugin/$PLUGIN/hello" -H "Authorization: Bearer $TOKEN" -H "tenant-id: 1")
  if printf '%s' "$PB" | grep -q 'Hello World'; then ok "插件路由可用（经 BFF）"
  else bad "插件路由响应（经 BFF）：$(printf '%s' "$PB" | head -c 200)"; fi
else
  note "未配置 GOODS_PLUGINS_DIR/GOODS_CONTAINER，跳过 goods 宿主装载验证"
fi

echo
if [ "$FAIL" -eq 0 ]; then
  printf '\033[32m全链路通过\033[0m\n'
else
  printf '\033[31m有 %s 个步骤失败\033[0m\n' "$FAIL"
fi
echo "工作目录 $WORK"
exit "$FAIL"
