#!/usr/bin/env bash
# =====================================================================
# Nacos 配置初始化（一次性任务容器）：
#   1) 等 Nacos 就绪  2) 建命名空间  3) 渲染变量表  4) 渲染配置  5) 批量导入
# 幂等：重复执行只是覆盖同名 dataId，可随时 `docker compose run --rm nacos-init` 重来。
# =====================================================================
set -euo pipefail

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-houeoc}"
WORKDIR="${WORKDIR:-/work}"

# 🔴 2026-10-27：模板目录现在是只读挂载（compose 里 :ro）。
# 原因：这个容器以 root 运行，渲染产物（variables.env / build/）会直接落在挂载的
# 宿主机源码树里，属主变成 root —— 之后普通用户 rm -rf / git clean 全部
# Permission denied，装一次机就把源码目录弄脏。
# 所以：不可写时先拷一份到容器内可写目录再渲染，宿主机源码树保持干净。
# ⚠️ 不能用 `[ -w /work ]` 判断：容器是 root，权限位恒为可写，只读挂载也一样返回真，
#    必须真写一次看会不会 EROFS。
if ! ( : > "${WORKDIR}/.write-probe" ) 2>/dev/null; then
  STAGE=/tmp/nacos-init-work
  rm -rf "$STAGE"; mkdir -p "$STAGE"
  cp -a "$WORKDIR"/. "$STAGE"/
  echo "==> 模板目录只读，已在容器内暂存到 ${STAGE} 渲染（不改宿主机源码树）"
  WORKDIR="$STAGE"
else
  rm -f "${WORKDIR}/.write-probe"
fi
cd "$WORKDIR"

ORIG_PW="$NACOS_PASSWORD"

echo "==> Nacos 地址：${NACOS_ADDR}  命名空间：${NACOS_NAMESPACE:-public}"

# ---------------------------------------------------------------- 1. 等就绪
login() { # login <口令> -> 输出 accessToken
  curl -s -m 5 -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
    -d "username=${NACOS_USER}&password=$1" \
    | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p'
}

echo "==> [1/5] 等待 Nacos 就绪"
TOKEN=""
AUTH_ENABLED="${NACOS_AUTH_ENABLED:-true}"
for i in $(seq 1 180); do
  if curl -s -m 3 -o /dev/null "http://${NACOS_ADDR}/nacos/"; then
    if [ "$AUTH_ENABLED" != "false" ]; then
      TOKEN="$(login "$NACOS_PASSWORD")"
      # Nacos 镜像不认 NACOS_AUTH_PASSWORD 时口令仍是默认的 nacos，自动回退并提示
      if [ -z "$TOKEN" ] && [ "$NACOS_PASSWORD" != "nacos" ]; then
        TOKEN="$(login nacos)"
        if [ -n "$TOKEN" ]; then
          # 🔴 2026-10-27：不要只回退 —— 回退后「服务端口令 = nacos，客户端口令 = .env 里的值」
          # 两边不一致，所有微服务会静默鉴权失败（表现为拉不到配置、崩在 RedisConfig）。
          # 这里直接把服务端口令改成 .env 里配置的那个，让两端真正一致。
          # 失败要重试：Nacos 刚起来时 auth 子系统可能还没热，第一次 PUT 会失败。
          local_synced=0
          for _try in 1 2 3 4 5; do
            _code=$(curl -s -m 10 -o /dev/null -w '%{http_code}' -X PUT \
                      "http://${NACOS_ADDR}/nacos/v1/auth/users?accessToken=${TOKEN}" \
                      --data-urlencode "username=${NACOS_USER}" \
                      --data-urlencode "newPassword=${NACOS_PASSWORD}" || echo 000)
            if [ "$_code" = "200" ]; then
              # 改完必须用新口令真登一次，确认生效（PUT 返回 200 不等于已生效）
              if NEW_TOKEN="$(login "$NACOS_PASSWORD")" && [ -n "$NEW_TOKEN" ]; then
                TOKEN="$NEW_TOKEN"
                local_synced=1
                echo "  已把 Nacos 服务端口令改为 .env 配置的口令（两端一致）"
                break
              fi
            fi
            echo "  口令同步第 ${_try} 次未成功（HTTP ${_code}），2s 后重试"
            sleep 2
          done
          if [ "$local_synced" != "1" ]; then
            # 不再「悄悄用 nacos 继续」——那样导入的配置看着成功，但所有微服务会连不上，
            # 症状是服务启动就崩在 RedisConfig/配置拉取，极难定位。直接失败并说清怎么做。
            echo "!! 无法把 Nacos 服务端口令同步成 .env 的 NACOS_PASSWORD"
            echo "   请二选一后重跑："
            echo "   1) 在 Nacos 控制台把 nacos 用户口令改成 .env 里的值"
            echo "   2) 把 .env 的 NACOS_PASSWORD 改成 nacos 并 docker compose up -d --force-recreate nacos-init"
            exit 1
          fi
        fi
      fi
    fi
    if [ -n "$TOKEN" ] || [ "$AUTH_ENABLED" = "false" ]; then
      echo "  Nacos 已就绪（第 ${i} 次探测，口令：${NACOS_PASSWORD}）"
      break
    fi
  fi
  sleep 2
done
if [ -z "$TOKEN" ] && [ "$AUTH_ENABLED" != "false" ]; then
  echo "!! 180 次探测后仍未拿到 accessToken"
  echo "   请检查：Nacos 是否已启动、.env 里的 NACOS_AUTH_PASSWORD 与容器实际口令是否一致"
  echo "   （Nacos 镜像未生效 NACOS_AUTH_PASSWORD 时实际口令是默认的 nacos）"
  exit 1
fi
if [ "$AUTH_ENABLED" != "false" ] && [ "$NACOS_PASSWORD" != "$ORIG_PW" ]; then
  echo "  [警告] 自动同步口令失败：Nacos 实际口令是 ${NACOS_PASSWORD}，与 .env 里配置的 ${ORIG_PW} 不一致。"
  echo "         请把 .env 的 NACOS_AUTH_PASSWORD 改成 ${NACOS_PASSWORD} 并重建服务容器，"
  echo "         否则微服务连不上 Nacos（服务侧口令取自 NACOS_AUTH_PASSWORD）。"
  echo "         起服务前先用这条确认渲染值："
  echo "           docker compose config | grep NACOS_PASSWORD"
fi

# ---------------------------------------------------------------- 2. 命名空间
echo "==> [2/5] 建命名空间 ${NACOS_NAMESPACE}"
if [ -n "$NACOS_NAMESPACE" ] && [ "$NACOS_NAMESPACE" != "public" ]; then
  ns_args=(--data-urlencode "customNamespaceId=${NACOS_NAMESPACE}"
           --data-urlencode "namespaceName=${NACOS_NAMESPACE}"
           --data-urlencode "namespaceDesc=HouCloud")
  [ -n "$TOKEN" ] && ns_args+=(--data-urlencode "accessToken=${TOKEN}")
  curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/console/namespaces" "${ns_args[@]}" >/dev/null || true
  echo "  已提交（若已存在会被忽略）"
fi

# ---------------------------------------------------------------- 3. 变量表
echo "==> [3/5] 生成变量表 variables.env"
# 🔴 2026-10-31：variables.env 的持久位置是 /persist（compose 挂 named volume
# nacos-init-data）。此前它写在渲染暂存目录里（只读挂载时在 /tmp），容器销毁即丢 ——
# 重跑 nacos-init 时 HOUCLOUD_PLATFORM_SITE_CODE 等「每安装实例唯一」的标识每次
# 换新码，平台侧会把同一站点登记成多行（2026-10-31 评估站实锤：两个 unimall-site-*）。
# /persist 不可写（未按 compose 部署）时退回旧行为并提示。
PERSIST_ENV=/persist/variables.env
persist_ok=0
if mkdir -p /persist 2>/dev/null && [ -w /persist ]; then persist_ok=1; fi
# 🔴 2026-11-05：持久卷里的 variables.env 必须「重新渲染」而不是「原样沿用」。
# 原样沿用会把后来新增的变量键永久挡在门外 —— 模板引用了新键、旧变量表没有，
# 渲染结果带着 ${VAR} 进 Nacos，服务启动才炸。本次开源包就踩到一次
# （mutual.yaml 新增 MALL_PLATFORM_FINANCE_SERVICE_SECRET，老站点重跑 nacos-init 直接失败）。
# 但也不能无条件重算：人工改过的第三方凭据写在持久卷里，重算会丢。
# 所以 render_variables.py 的取值优先级是
#   容器环境变量 > 持久卷旧值 > 内置默认值 > variables.example 原值
# —— 新键会被补上，旧值（含实例唯一标识 site-code、人工凭据）保持不变。
if [ "$persist_ok" = "1" ]; then
  # 直接把持久文件当输出目标：脚本会自动把它当「上次的值」读入并逐键沿用。
  python3 /render_variables.py variables.example "$PERSIST_ENV"
  cp -f "$PERSIST_ENV" variables.env
  echo "  已重新渲染并回写 ${PERSIST_ENV}（旧值沿用，新增键自动补齐）"
elif [ -f variables.env ]; then
  echo "  已存在 variables.env，保留（/persist 不可写，新增键需手工补）"
else
  python3 /render_variables.py variables.example variables.env
  echo "  ⚠️ /persist 不可写（未按 compose 部署？）：variables.env 不持久，重跑会换 site-code"
fi
if [ "${FORCE_RENDER:-false}" = "true" ]; then
  echo "  提示：FORCE_RENDER 已不再需要（每次都会重新渲染），该开关仅为兼容保留"
fi

# ---------------------------------------------------------------- 4/5. 渲染 + 导入
echo "==> [4/5] 渲染配置模板"
python3 apply_templates.py

# 渲染后残留检查（fail-closed）。
# 模板里引用了 variables.env 中不存在的变量时，apply_templates 只拒绝替换、把
# ${VAR} 原样留在产物里，导入照样成功 —— 问题要拖到服务启动阶段才以
# 「缺少必要的 spring.redis.* 配置」「Could not resolve placeholder」的形式暴露，
# 排查得跨 install → nacos-init → 服务日志三层。
# 2026-11-05 实测踩到：redis.yaml 用 REDIS_HOST/REDIS_PORT，而 variables.example
# 只登记了 REDIS_ADDR，导致 11 个服务全部启动失败。这里直接中断，把问题拦在
# 配置初始化的最后一步，并明确指出「模板要什么 / 变量表缺什么」。
python3 - <<'PY' || exit 1
import pathlib, re, sys

leftover = {}
for p in sorted(pathlib.Path('build').rglob('*.yaml')):
    for m in re.finditer(r'\$\{([A-Za-z0-9_]+)\}', p.read_text(encoding='utf-8', errors='replace')):
        leftover.setdefault(m.group(1), set()).add(p.name)

if leftover:
    print('!! 渲染结果仍含未赋值占位符，继续导入会让服务启动失败：', file=sys.stderr)
    for name in sorted(leftover):
        print(f'     {name}  <- 出现于 {", ".join(sorted(leftover[name]))}', file=sys.stderr)
    print('   修法：在 deploy/scripts/nacos-templates/variables.example 补上这些键'
          '（Docker 轨由 nacos-init 自动填值），再 '
          'FORCE_RENDER=true docker compose run --rm nacos-init', file=sys.stderr)
    sys.exit(1)
print('  占位符检查：全部已赋值')
PY

# 渲染后补丁（只改 build/ 产物，不动仓库模板）：
#   1) redis.yaml 里端口写死 6379，中间件栈映射到别的宿主端口时按 RENDER_REDIS_PORT 改写
#   2) Redis 无口令时，占位符 ${REDIS_PASSWORD} 会原样留下，Spring 启动阶段解析失败
#      （Could not resolve placeholder），所以统一替换成空串
python3 - "${RENDER_REDIS_PORT:-6379}" "${REDIS_PASSWORD:-}" <<'PY'
import pathlib, sys

port, pwd = sys.argv[1], sys.argv[2]
changed = []
for p in sorted(pathlib.Path('build').rglob('*.yaml')):
    t = p.read_text(encoding='utf-8')
    o = t
    if port != '6379':
        t = t.replace('port: 6379', f'port: {port}')
    if not pwd:
        t = t.replace('${REDIS_PASSWORD}', '""')
    if t != o:
        p.write_text(t, encoding='utf-8')
        changed.append(str(p))
if changed:
    print('  渲染后补丁已应用到：' + ', '.join(changed))
PY

echo "==> [5/5] 导入 Nacos"
export NACOS_ADDR NACOS_USER NACOS_PASSWORD NACOS_NAMESPACE
bash import_to_nacos.sh

echo "==> 配置初始化完成"
