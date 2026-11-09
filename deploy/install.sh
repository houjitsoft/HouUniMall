#!/usr/bin/env bash
# =====================================================================
# HouCloud 全新机器安装入口（部署轨 B：全容器化）
#
#   bash deploy/install.sh              # 最小可用集：登录 + 插件中心 + 插件市场
#   bash deploy/install.sh --with-admin # 额外起管理后台前端（需构建前端镜像）
#   bash deploy/install.sh --down       # 停止并保留数据
#   bash deploy/install.sh --purge      # 停止并删除数据卷（不可恢复）
#   bash deploy/install.sh --wizard     # 启动界面化安装向导（install profile）
#   bash deploy/install.sh --stage <name>  # 单阶段执行（向导 agent 编排用；
#        name ∈ preflight|env|env-custom|infra|build|apps|smoke|custom-db|
#        nacos-import；每阶段末尾输出 ##STAGE:<name>:ok 供 agent 解析）
#
# 设计原则：
#   1. 不维护第二份 compose。编排的唯一真相仍是仓库根的 docker-compose.yml，
#      本脚本只是「按依赖顺序挑出最小集的服务名」，避免两套 YAML 漂移。
#   2. 首次运行自动生成 .env 并随机化全部口令；不允许 change-me 进入运行态。
#   3. 每一步都做前置校验，失败即退出并给出下一步该看什么，不做「看起来成功」。
#   4. 两种安装模式由 HOUCLOUD_INSTALL_MODE 环境变量控制（向导 agent 传入）：
#      docker  = 内置中间件全容器化（默认）；custom = 外部中间件（hostnet 轨）。
#   5. 安装状态持久化在 data/install-state.json（HOUCLOUD_INSTALL_STATE 可覆盖）：
#      命令行整流程在终态写入；--stage 模式只输出 ##STAGE 标记由 agent 写
#      （单一写者原则，防止两边并发覆盖）。
# =====================================================================
set -euo pipefail

cd "$(dirname "$0")/.."
ROOT="$(pwd)"

# ---- 最小可用集：没有这 5 个服务，登录和插件全链路都走不通 ----
INFRA_SERVICES="mysql redis nacos rocketmq-namesrv rocketmq-broker nacos-init"
APP_SERVICES="permission auth userms goods order payment base mall logging bff-web admin-web"
# logging 必须在清单里：docker-compose.yml 定义了该服务、Nacos 有 logging.yaml、
# 且 logging 插件是默认安装的 11 个之一。漏掉它时 BFF 的 LogServiceFeign
# 每次轮询 /plugin/logging/alert/unread-count 都拿到 503「Load balancer does not
# contain an instance for the service logging」，后台告警角标永远取不到数。

WITH_ADMIN=0
ACTION="up"
STAGE_NAME=""
while [ $# -gt 0 ]; do
  case "$1" in
    --with-admin) WITH_ADMIN=1 ;;
    --down)       ACTION="down" ;;
    --purge)      ACTION="purge" ;;
    --wizard)     ACTION="wizard" ;;
    --stage)      ACTION="stage"; shift; STAGE_NAME="${1:-}" ;;
    -h|--help)
      sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'
      exit 0 ;;
    *) echo "未知参数：$1（用 --help 查看用法）" >&2; exit 2 ;;
  esac
  shift
done

INSTALL_MODE="${HOUCLOUD_INSTALL_MODE:-docker}"
STATE_FILE="${HOUCLOUD_INSTALL_STATE:-data/install-state.json}"

log()  { printf '\033[36m[%s]\033[0m %s\n' "$(date +%H:%M:%S)" "$*"; }
ok()   { printf '\033[32m  ✓\033[0m %s\n' "$*"; }
warn() { printf '\033[33m  !\033[0m %s\n' "$*"; }
die()  { printf '\033[31m  ✗ %s\033[0m\n' "$*" >&2; exit 1; }

# 🔴 agent 容器内跑 compose 的路径坑（2026-10-28 第五轮两连坑后的终局）：
# compose 的挂载源/build context 由 dockerd 在宿主侧解析，而 compose client 自己
# 又要读同一批路径（build context 打包、.env、相对挂载展开）。挂 /project 时宿主侧
# 被 Docker 自动建空目录（mysql 挂空 initdb、broker 读空 conf crash-loop）；
# --project-directory 传宿主路径时 client 在容器内又 lstat 不到。
# 终解：compose 里把仓库目录**原路径**同构挂进 agent（见 install-agent 注释），
# agent 的 cwd 即宿主同构路径，compose 的默认行为天然两端一致——
# 所以这里不需要任何特殊处理；宿主机直跑时行为也与以前完全一致。
# （HOUCLOUD_HOST_PROJECT_DIR 仅由 --wizard 注入给 compose 插值，本函数不再用它。）

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose -f docker-compose.yml "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose -f docker-compose.yml "$@"
  else
    die "没有 docker compose（v2 或 v1），先装 Docker Engine 24+"
  fi
}

# 外部中间件轨（install 向导 custom 模式 / 模式 B）：
# hostnet overlay 必须与主文件叠加；apps 阶段起服务必须 --no-deps，
# 否则 compose 会连带拉起 mysql/nacos/redis 去抢宿主机端口。
compose_custom() {
  if docker compose version >/dev/null 2>&1; then
    docker compose -f docker-compose.yml -f docker-compose.hostnet.yml "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose -f docker-compose.yml -f docker-compose.hostnet.yml "$@"
  else
    die "没有 docker compose（v2 或 v1），先装 Docker Engine 24+"
  fi
}

# ---------------------------------------------------------------------
# 0. 前置校验
# ---------------------------------------------------------------------
preflight() {
  log "检查运行环境"
  command -v docker >/dev/null 2>&1 || die "未安装 docker。Debian/Ubuntu：curl -fsSL https://get.docker.com | sh"
  docker info >/dev/null 2>&1 || die "docker 已安装但连不上 daemon（当前用户不在 docker 组？试试 sudo usermod -aG docker \$USER 后重新登录）"
  ok "docker $(docker version --format '{{.Server.Version}}' 2>/dev/null || echo '?')"

  compose version >/dev/null 2>&1 || die "docker compose 不可用"
  ok "compose $(compose version --short 2>/dev/null || echo 'v1')"

  local mem_kb mem_gb cpu
  mem_kb=$(awk '/MemTotal/{print $2}' /proc/meminfo 2>/dev/null || echo 0)
  mem_gb=$(( mem_kb / 1024 / 1024 ))
  cpu=$(nproc 2>/dev/null || echo 0)
  if [ "$mem_gb" -lt 8 ]; then
    die "内存 ${mem_gb}GB 不足。最小集 11 个容器建议 ≥8GB；真要跑请调低 SERVICE_MEM_LIMIT 并自担风险"
  fi
  ok "内存 ${mem_gb}GB / CPU ${cpu} 核"
}

# 端口检查必须读 .env：install.sh 里的 PORT 和 compose 实际用的 *_HOST_PORT
# 是两套名字，且 .env 只有 ensure_env 之后才存在。放在 preflight 里查会把
# 「用户特意改到 13306」误报成「3306 被占」，噪音大于价值。
#
# 容器是否属于本项目：按 compose project label 判断（重装时自己项目的容器
# 占着端口属正常，compose up 会复用；外人的容器/端口才叫冲突）。
is_mine_container() {  # $1=容器名
  [ -n "$1" ] || return 1
  local project
  project=$(env_get COMPOSE_PROJECT_NAME); project="${project:-unimall}"
  [ "$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$1" 2>/dev/null)" = "$project" ]
}

# 端口占用：ss 一层 + /dev/tcp 试探一层。
# 单靠 ss 不够（2026-10-27 实测：非 root 下 ss 对某些 IPv6 wildcard /
# docker-proxy 残留 socket 查不出来，netstat 能看到 `:::29876 LISTEN`，
# compose bind 时才炸 address already in use）。/dev/tcp 是 v4 连接试探，
# 两层都过也仍可能有漏网 —— 所以 start_infra 的失败提示里还有兜底指引。
port_busy() {  # $1=端口
  local p="$1"
  ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE "[:.]${p}\$" && return 0
  (exec 3<>"/dev/tcp/127.0.0.1/${p}") 2>/dev/null && return 0
  return 1
}

check_ports() {
  local p busy="" owner
  for p in \
    "$(env_get BFF_PORT)" "$(env_get WEB_PORT)" \
    "$(env_get MYSQL_HOST_PORT)" "$(env_get REDIS_HOST_PORT)" \
    "$(env_get NACOS_HOST_PORT)" "$(env_get NACOS_GRPC_PORT)" "$(env_get NACOS_GRPC_RAFT_PORT)" \
    "$(env_get ROCKETMQ_NAMESRV_PORT)" "$(env_get ES_HOST_PORT)"; do
    [ -n "$p" ] || continue
    port_busy "$p" || continue
    # 谁映射了这个端口？是本项目自己的容器（重装）就放行
    owner=$(docker ps --format '{{.Names}}\t{{.Ports}}' 2>/dev/null | awk -v pat=":${p}->" 'index($0,pat){print $1; exit}')
    is_mine_container "$owner" && continue
    busy="$busy $p"
  done
  if [ -n "$busy" ]; then
    # 外部占用必须中止而不是警告：继续跑会让 compose 装到一半才在
    # bind 阶段炸掉，留下半启动状态；且 docker 见到容器重名还会把
    # 既有容器改名（见 guard_project_isolation）。
    die "以下宿主机端口已被本项目之外的服务占用：${busy}
    两种情况二选一：
      1) 本机已有同类服务（如另一套安装/生产实例）→ 改 .env 的对应 *_HOST_PORT 到空闲端口；
      2) 上次安装的残留 → 先执行 ./install.sh down 清理再重跑。"
  fi
  ok "端口 $(env_get BFF_PORT) / $(env_get MYSQL_HOST_PORT) / $(env_get REDIS_HOST_PORT) / $(env_get NACOS_HOST_PORT) 均空闲"
}

# 源码指纹：builder 镜像里烤的是 jar，镜像「存在」不等于「是当前代码」。
# 原实现只判断镜像存在就复用，会在机器上留有旧 builder 时静默装出上一版代码，
# 且日志显示一切正常 —— 这是最危险的一类「看起来成功了」。
source_fingerprint() {
  if [ -d .git ] && command -v git >/dev/null 2>&1; then
    git rev-parse --short HEAD 2>/dev/null && return 0
  fi
  # 源码包没有 .git：按参与编译的源文件内容取哈希
  find backend plugin-platform -type f \( -name '*.java' -o -name 'pom.xml' -o -name '*.yml' -o -name '*.json' \) -print0 2>/dev/null \
    | sort -z | xargs -0 sha256sum 2>/dev/null | sha256sum | cut -c1-12
}

# ---------------------------------------------------------------------
# 1. .env：首次生成 + 随机化口令
# ---------------------------------------------------------------------
rand() {
  local bytes="${1:-16}" mode="${2:-hex}"
  if [ "$mode" = "b64" ]; then
    # Nacos 2.x 的 AUTH_TOKEN 必须是 base64 且解码后 ≥32 字节；换成 hex 会让它
    # 启动即崩，而报错只说 basicAuthenticationFilter 依赖失败，看不出与 token 有关。
    openssl rand -base64 "$bytes" 2>/dev/null | tr -d '\n'
  else
    openssl rand -hex "$bytes" 2>/dev/null \
      || head -c "$(( bytes * 2 ))" /dev/urandom | od -An -tx1 | tr -d ' \n'
  fi
}

ensure_env() {
  log "准备 .env"
  if [ -f .env ]; then
    ok ".env 已存在，沿用（不改动既有口令）"
  else
    [ -f .env.docker.example ] || die "缺少 .env.docker.example，代码包不完整"
    cp .env.docker.example .env
    ok "已从 .env.docker.example 生成 .env"
  fi
  chmod 600 .env

  # 只在值仍是 change-me* 时替换，避免覆盖用户已改过的口令
  local replaced=0
  local pairs=(
    "MYSQL_ROOT_PASSWORD=32:hex"
    "MYSQL_PASSWORD=24:hex"
    "REDIS_PASSWORD=24:hex"
    "NACOS_PASSWORD=24:hex"
    "NACOS_AUTH_PASSWORD=24:hex"
    "NACOS_AUTH_TOKEN=32:b64"
    "NACOS_AUTH_IDENTITY_VALUE=24:hex"
    "XXL_JOB_PASS=24:hex"
    "RESTART_ADMIN_TOKEN=32:hex"
  )
  for pair in "${pairs[@]}"; do
    local key="${pair%%=*}" spec="${pair##*=}" len mode
    len="${spec%%:*}"; mode="${spec#*:}"
    [ "$mode" = "$spec" ] && mode="hex"
    local cur
    cur=$(grep -E "^${key}=" .env | head -1 | cut -d= -f2- || true)
    case "$cur" in
      ""|change-me*|*generate-with-openssl*)
        local val; val=$(rand "$len" "$mode")
        if grep -qE "^${key}=" .env; then
          # 用 | 作分隔符，避免口令里的 / & 被 sed 当语法
          sed -i.bak "s|^${key}=.*|${key}=${val}|" .env && rm -f .env.bak
        else
          printf '%s=%s\n' "$key" "$val" >> .env
        fi
        replaced=$((replaced + 1))
        ;;
    esac
  done
  [ "$replaced" -gt 0 ] && ok "已随机化 ${replaced} 个默认口令" || ok "口令均为自定义值，未改动"

  # 兜底：运行前不允许残留 change-me
  if grep -qE '^(MYSQL_ROOT_PASSWORD|MYSQL_PASSWORD|REDIS_PASSWORD|XXL_JOB_PASS|RESTART_ADMIN_TOKEN)=change-me$' .env; then
    die ".env 里仍有 change-me 口令，拒绝启动。请手工改成强口令后重试"
  fi

  # Nacos 2.x 硬性要求 AUTH_TOKEN 是 base64 且解码后 ≥32 字节。手填一个 hex 串
  # 是最常见的坑，症状是 nacos 启动即崩、报错只说 basicAuthenticationFilter 依赖失败。
  local tok tokbytes fresh
  tok=$(env_get NACOS_AUTH_TOKEN); tok="${tok%\"}"; tok="${tok#\"}"
  tokbytes=$(printf '%s' "$tok" | base64 -d 2>/dev/null | wc -c | tr -d ' ')
  if [ "${tokbytes:-0}" -lt 32 ]; then
    fresh=$(rand 32 b64)
    sed -i.bak "s|^NACOS_AUTH_TOKEN=.*|NACOS_AUTH_TOKEN=${fresh}|" .env && rm -f .env.bak
    ok "NACOS_AUTH_TOKEN 不是合法 base64（解码 ${tokbytes:-0} 字节 < 32），已重新生成"
  fi

  ensure_plugin_delivery

  # Nacos 口令只有一个，但编排里有两个变量名：nacos 容器读 NACOS_AUTH_PASSWORD，
  # 微服务与 nacos-init 读 NACOS_PASSWORD（优先）。两个都随机生成 → 值必然不同 →
  # nacos-init 改不动服务端口令、微服务拉不到配置（症状是启动即崩在配置阶段）。
  local np napp
  np=$(env_get NACOS_PASSWORD); napp=$(env_get NACOS_AUTH_PASSWORD)
  [ -n "$np" ] || { np=$(rand 24 hex); set_env NACOS_PASSWORD "$np"; }
  if [ "$np" != "$napp" ]; then
    set_env NACOS_AUTH_PASSWORD "$np"
    ok "已对齐 NACOS_AUTH_PASSWORD 与 NACOS_PASSWORD（同一个口令）"
  fi
}

set_env() {  # set_env KEY VALUE —— 有则改，无则加
  local key="$1" val="$2"
  if grep -qE "^${key}=" .env; then
    sed -i.bak "s|^${key}=.*|${key}=${val}|" .env && rm -f .env.bak
  else
    printf '%s=%s\n' "$key" "${val}" >> .env
  fi
}

# ---------------------------------------------------------------------
# 插件「创建 → 上架 → 下单 → 支付 → 下载 → 安装」闭环所需的本地凭据
#
# 这些配置在代码里<b>全部 fail-closed</b>（留空 = 拒绝，不静默放行）。空白机上不预置
# 的话，装完能登录、能看市场，但整条交易链路一步都走不通，而报错分散在五个接口：
#   download-token-secret 空 → /artifact/download-url 直接拒绝签发（拿不到制品）
#   sign.public-keys 空      → 未签名制品安装被拒（require-signature 默认 true）
#   sign.private-key-path 空 → 上传时无法签名，发布流程断在第一步
#   pay.admin-token 空       → 支付配置接口一律 403，没人配得了收款渠道
#   pay.enabled=false        → /market/order/create 直接拒绝（下不出单）
#
# 单机试用装在本机生成一套完整凭据；生产应换成自己的密钥，且私钥只留在签名机。
# ---------------------------------------------------------------------
ensure_plugin_delivery() {
  local kid secrets _pub _priv _d

  # 0) 宿主机落盘目录：制品本体 / 已装插件 / 签名私钥必须持久化，
  #    否则容器一重建已上架 jar 全丢。统一收在 data/ 下，不污染源码树。
  local pdir adir
  pdir=$(env_get PLUGINS_DIR); pdir="${pdir:-./data/plugins}"
  adir=$(env_get ARTIFACTS_DIR); adir="${adir:-./data/artifacts}"
  for _d in "$pdir" "$adir"; do
    case "$_d" in /*) ;; *) _d="$ROOT/${_d#./}" ;; esac
    mkdir -p "$_d"
  done

  # 1) 下载令牌 HMAC 密钥：为空则 download-url 无法签发
  if [ -z "$(env_get HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET)" ]; then
    set_env HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET "$(rand 32 hex)"
    ok "已生成 HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET（制品下载换票密钥）"
  fi

  # 2) 支付配置管理口令：为空则 /market/pay/admin/** 全部 403

  # 源码包没有 .git：按参与编译的源文件内容取哈希
  find backend plugin-platform -type f \( -name '*.java' -o -name 'pom.xml' -o -name '*.yml' -o -name '*.json' \) -print0 2>/dev/null \
    | sort -z | xargs -0 sha256sum 2>/dev/null | sha256sum | cut -c1-12
}

# ---------------------------------------------------------------------
# 1. .env：首次生成 + 随机化口令
# ---------------------------------------------------------------------
rand() {
  local bytes="${1:-16}" mode="${2:-hex}"
  if [ "$mode" = "b64" ]; then
    # Nacos 2.x 的 AUTH_TOKEN 必须是 base64 且解码后 ≥32 字节；换成 hex 会让它
    # 启动即崩，而报错只说 basicAuthenticationFilter 依赖失败，看不出与 token 有关。
    openssl rand -base64 "$bytes" 2>/dev/null | tr -d '\n'
  else
    openssl rand -hex "$bytes" 2>/dev/null \
      || head -c "$(( bytes * 2 ))" /dev/urandom | od -An -tx1 | tr -d ' \n'
  fi
}

ensure_env() {
  log "准备 .env"
  if [ -f .env ]; then
    ok ".env 已存在，沿用（不改动既有口令）"
  else
    [ -f .env.docker.example ] || die "缺少 .env.docker.example，代码包不完整"
    cp .env.docker.example .env
    ok "已从 .env.docker.example 生成 .env"
  fi
  chmod 600 .env

  # 只在值仍是 change-me* 时替换，避免覆盖用户已改过的口令
  local replaced=0
  local pairs=(
    "MYSQL_ROOT_PASSWORD=32:hex"
    "MYSQL_PASSWORD=24:hex"
    "REDIS_PASSWORD=24:hex"
    "NACOS_PASSWORD=24:hex"
    "NACOS_AUTH_PASSWORD=24:hex"
    "NACOS_AUTH_TOKEN=32:b64"
    "NACOS_AUTH_IDENTITY_VALUE=24:hex"
    "XXL_JOB_PASS=24:hex"
    "RESTART_ADMIN_TOKEN=32:hex"
  )
  for pair in "${pairs[@]}"; do
    local key="${pair%%=*}" spec="${pair##*=}" len mode
    len="${spec%%:*}"; mode="${spec#*:}"
    [ "$mode" = "$spec" ] && mode="hex"
    local cur
    cur=$(grep -E "^${key}=" .env | head -1 | cut -d= -f2- || true)
    case "$cur" in
      ""|change-me*|*generate-with-openssl*)
        local val; val=$(rand "$len" "$mode")
        if grep -qE "^${key}=" .env; then
          # 用 | 作分隔符，避免口令里的 / & 被 sed 当语法
          sed -i.bak "s|^${key}=.*|${key}=${val}|" .env && rm -f .env.bak
        else
          printf '%s=%s\n' "$key" "$val" >> .env
        fi
        replaced=$((replaced + 1))
        ;;
    esac
  done
  [ "$replaced" -gt 0 ] && ok "已随机化 ${replaced} 个默认口令" || ok "口令均为自定义值，未改动"

  # 兜底：运行前不允许残留 change-me
  if grep -qE '^(MYSQL_ROOT_PASSWORD|MYSQL_PASSWORD|REDIS_PASSWORD|XXL_JOB_PASS|RESTART_ADMIN_TOKEN)=change-me$' .env; then
    die ".env 里仍有 change-me 口令，拒绝启动。请手工改成强口令后重试"
  fi

  # Nacos 2.x 硬性要求 AUTH_TOKEN 是 base64 且解码后 ≥32 字节。手填一个 hex 串
  # 是最常见的坑，症状是 nacos 启动即崩、报错只说 basicAuthenticationFilter 依赖失败。
  local tok tokbytes fresh
  tok=$(env_get NACOS_AUTH_TOKEN); tok="${tok%\"}"; tok="${tok#\"}"
  tokbytes=$(printf '%s' "$tok" | base64 -d 2>/dev/null | wc -c | tr -d ' ')
  if [ "${tokbytes:-0}" -lt 32 ]; then
    fresh=$(rand 32 b64)
    sed -i.bak "s|^NACOS_AUTH_TOKEN=.*|NACOS_AUTH_TOKEN=${fresh}|" .env && rm -f .env.bak
    ok "NACOS_AUTH_TOKEN 不是合法 base64（解码 ${tokbytes:-0} 字节 < 32），已重新生成"
  fi

  ensure_plugin_delivery

  # Nacos 口令只有一个，但编排里有两个变量名：nacos 容器读 NACOS_AUTH_PASSWORD，
  # 微服务与 nacos-init 读 NACOS_PASSWORD（优先）。两个都随机生成 → 值必然不同 →
  # nacos-init 改不动服务端口令、微服务拉不到配置（症状是启动即崩在配置阶段）。
  local np napp
  np=$(env_get NACOS_PASSWORD); napp=$(env_get NACOS_AUTH_PASSWORD)
  [ -n "$np" ] || { np=$(rand 24 hex); set_env NACOS_PASSWORD "$np"; }
  if [ "$np" != "$napp" ]; then
    set_env NACOS_AUTH_PASSWORD "$np"
    ok "已对齐 NACOS_AUTH_PASSWORD 与 NACOS_PASSWORD（同一个口令）"
  fi
}

set_env() {  # set_env KEY VALUE —— 有则改，无则加
  local key="$1" val="$2"
  if grep -qE "^${key}=" .env; then
    sed -i.bak "s|^${key}=.*|${key}=${val}|" .env && rm -f .env.bak
  else
    printf '%s=%s\n' "$key" "${val}" >> .env
  fi
}

# ---------------------------------------------------------------------
# 插件「创建 → 上架 → 下单 → 支付 → 下载 → 安装」闭环所需的本地凭据
#
# 这些配置在代码里<b>全部 fail-closed</b>（留空 = 拒绝，不静默放行）。空白机上不预置
# 的话，装完能登录、能看市场，但整条交易链路一步都走不通，而报错分散在五个接口：
#   download-token-secret 空 → /artifact/download-url 直接拒绝签发（拿不到制品）
#   sign.public-keys 空      → 未签名制品安装被拒（require-signature 默认 true）
#   sign.private-key-path 空 → 上传时无法签名，发布流程断在第一步
#   pay.admin-token 空       → 支付配置接口一律 403，没人配得了收款渠道
#   pay.enabled=false        → /market/order/create 直接拒绝（下不出单）
#
# 单机试用装在本机生成一套完整凭据；生产应换成自己的密钥，且私钥只留在签名机。
# ---------------------------------------------------------------------
ensure_plugin_delivery() {
  local kid secrets _pub _priv _d

  # 0) 宿主机落盘目录：制品本体 / 已装插件 / 签名私钥必须持久化，
  #    否则容器一重建已上架 jar 全丢。统一收在 data/ 下，不污染源码树。
  local pdir adir
  pdir=$(env_get PLUGINS_DIR); pdir="${pdir:-./data/plugins}"
  adir=$(env_get ARTIFACTS_DIR); adir="${adir:-./data/artifacts}"
  for _d in "$pdir" "$adir"; do
    case "$_d" in /*) ;; *) _d="$ROOT/${_d#./}" ;; esac
    mkdir -p "$_d"
  done

  # 1) 下载令牌 HMAC 密钥：为空则 download-url 无法签发
  if [ -z "$(env_get HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET)" ]; then
    set_env HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET "$(rand 32 hex)"
    ok "已生成 HOU_ARTIFACT_DOWNLOAD_TOKEN_SECRET（制品下载换票密钥）"
  fi

  # 2) 支付配置管理口令：为空则 /market/pay/admin/** 全部 403
  if [ -z "$(env_get HOU_PLUGIN_PAY_ADMIN_TOKEN)" ]; then
    set_env HOU_PLUGIN_PAY_ADMIN_TOKEN "$(rand 24 hex)"
    ok "已生成 HOU_PLUGIN_PAY_ADMIN_TOKEN（支付配置管理口令）"
  fi

  # 2b) 跨宿主安装内部令牌：base 派发 /internal/plugin/install 与各宿主校验共用。
  #     为空 → 市场安装报「base 未配置 houcloud.plugin.internal-job-token，无法派发跨宿主安装」。
  #     必须与所有宿主同值；compose 同时注入 HOU_INTERNAL_JOB_TOKEN 与
  #     HOUCLOUD_PLUGIN_INTERNAL_JOB_TOKEN（后者走 Spring 松散绑定，覆盖无 yml 占位的宿主）。
  if [ -z "$(env_get HOU_INTERNAL_JOB_TOKEN)" ]; then
    set_env HOU_INTERNAL_JOB_TOKEN "$(rand 32 hex)"
    ok "已生成 HOU_INTERNAL_JOB_TOKEN（跨宿主插件安装内部令牌）"
  fi
  if [ -z "$(env_get HOU_PLUGIN_PAY_ADMIN_TOKEN)" ]; then
    set_env HOU_PLUGIN_PAY_ADMIN_TOKEN "$(rand 24 hex)"
    ok "已生成 HOU_PLUGIN_PAY_ADMIN_TOKEN（支付配置管理口令）"
  fi

  # 3) 制品签名密钥（Ed25519）。私钥留宿主机 secrets/，只读挂进 base 供上传时签名；
  #    公钥写进 .env 供所有实例验签。格式与 deploy/scripts/plugin/gen-sign-keys.sh 一致。
  kid=$(env_get HOU_ARTIFACT_SIGN_ACTIVE_KID); kid="${kid:-k2026}"
  secrets=$(env_get PLUGIN_SIGN_SECRETS_DIR); secrets="${secrets:-./secrets}"
  case "$secrets" in /*) ;; *) secrets="$ROOT/${secrets#./}" ;; esac
  mkdir -p "$secrets" && chmod 700 "$secrets"
  _priv="$secrets/artifact-ed25519-${kid}.key"
  _pub="$secrets/artifact-ed25519-${kid}.pub"
  if [ ! -s "$_priv" ] || [ ! -s "$_pub" ]; then
    command -v openssl >/dev/null 2>&1 \
      || die "需要 openssl 生成插件签名密钥（apt install openssl）"
    # Java 侧（Ed25519ArtifactSigner.loadPrivateKey）读整个文件做 Base64 解码，
    # 私钥必须是「裸 Base64(PKCS#8 DER)」——不能带 PEM 头尾（openssl genpkey 默认输出），
    # 与 deploy/scripts/plugin/gen-sign-keys.sh 的产物格式一致。
    # 同一对密钥派生私钥/公钥两份产物（不能 genpkey 两次，那会得到不配对的两把钥匙）。
    _tmp=$(mktemp)
    openssl genpkey -algorithm ED25519 -out "$_tmp" 2>/dev/null \
      || die "生成 Ed25519 私钥失败"
    openssl pkey -in "$_tmp" -outform DER 2>/dev/null | base64 | tr -d '\n' > "$_priv"
    chmod 600 "$_priv"
    # 公钥同样是 DER-Base64（不是 PEM 头尾）
    openssl pkey -in "$_tmp" -pubout -outform DER 2>/dev/null | base64 | tr -d '\n' > "$_pub"
    chmod 644 "$_pub"
    rm -f "$_tmp"
    ok "已生成插件签名密钥对：${_priv}（600，勿入 git / 镜像）"
  fi
  set_env HOU_ARTIFACT_SIGN_ACTIVE_KID "$kid"
  set_env HOU_ARTIFACT_SIGN_PUBLIC_KEYS "${kid}=$(tr -d '\n' < "$_pub")"
  set_env HOU_ARTIFACT_SIGN_PRIVATE_KEY_PATH "/app/secrets/artifact-ed25519-${kid}.key"
  set_env PLUGIN_SIGN_SECRETS_DIR "$secrets"
  if [ -z "$(env_get HOU_PLUGIN_REQUIRE_SIGNATURE)" ]; then
    set_env HOU_PLUGIN_REQUIRE_SIGNATURE true
  fi

  # 4) 购买链路：单机试用默认「开启 + 沙箱 + MOCK 渠道」，否则下单直接被拒。
  #    ⚠️ 沙箱不碰真实资金，任何人都能把订单刷成已支付并领走授权 —— 生产必须关掉。
  if [ -z "$(env_get HOU_PLUGIN_PAY_ENABLED)" ]; then
    set_env HOU_PLUGIN_PAY_ENABLED true
    set_env HOU_PLUGIN_PAY_CHANNEL MOCK
    set_env HOU_PLUGIN_PAY_SANDBOX true
    warn "已开启插件购买（沙箱模式，不扣真实资金）"
  fi
}

env_get() { grep -E "^$1=" .env 2>/dev/null | head -1 | cut -d= -f2- || true; }

# ---------------------------------------------------------------------
# 1b. 外部中间件（custom 模式）：把向导收集的连接信息写进 .env
# ---------------------------------------------------------------------
# 输入一律走环境变量（向导 agent 传入），本阶段是 .env 关于中间件地址的唯一写者：
#   CUSTOM_MYSQL_HOST/PORT/USER/PASSWORD/DATABASE、CUSTOM_REDIS_ADDR/PASSWORD、
#   CUSTOM_NACOS_ADDR/USER/PASSWORD/NAMESPACE、CUSTOM_ROCKETMQ_NAMESERVER
ensure_env_custom() {
  log "写入外部中间件连接信息（custom 模式）"
  [ -n "${CUSTOM_MYSQL_HOST:-}" ] || die "CUSTOM_MYSQL_HOST 未传入（向导配置缺项）"
  [ -n "${CUSTOM_MYSQL_PASSWORD:-}" ] || die "CUSTOM_MYSQL_PASSWORD 未传入"

  set_env NACOS_ADDR        "${CUSTOM_NACOS_ADDR:-127.0.0.1:8848}"
  set_env NACOS_USER         "${CUSTOM_NACOS_USER:-nacos}"
  set_env NACOS_PASSWORD     "${CUSTOM_NACOS_PASSWORD:-}"
  set_env NACOS_NAMESPACE   "${CUSTOM_NACOS_NAMESPACE:-houeoc}"
  set_env REDIS_ADDR        "${CUSTOM_REDIS_ADDR:-127.0.0.1:6379}"
  set_env CUSTOM_REDIS_PASSWORD "${CUSTOM_REDIS_PASSWORD:-}"
  set_env CUSTOM_MYSQL_HOST    "${CUSTOM_MYSQL_HOST}"
  set_env CUSTOM_MYSQL_PORT    "${CUSTOM_MYSQL_PORT:-3306}"
  set_env CUSTOM_MYSQL_USER    "${CUSTOM_MYSQL_USER:-root}"
  set_env CUSTOM_MYSQL_DB      "${CUSTOM_MYSQL_DATABASE:-meoc}"
  set_env CUSTOM_ROCKETMQ_NAMESERVER "${CUSTOM_ROCKETMQ_NAMESERVER:-127.0.0.1:9876}"
  # 渲染链统一用 MYSQL_PASSWORD 填全部 *_DATASOURCE_PASSWORD，这里写客户库口令
  set_env MYSQL_DATABASE  "${CUSTOM_MYSQL_DATABASE:-meoc}"
  set_env MYSQL_PASSWORD "${CUSTOM_MYSQL_PASSWORD}"
  # 外部轨没有 seata-server：不关的话客户端启动即 BeanCreationException
  set_env SEATA_ENABLED "false"
  ok "已写入外部中间件地址（MySQL ${CUSTOM_MYSQL_HOST}:${CUSTOM_MYSQL_PORT:-3306} / Redis ${CUSTOM_REDIS_ADDR:-127.0.0.1:6379} / Nacos ${CUSTOM_NACOS_ADDR:-127.0.0.1:8848}）"
}

# ---------------------------------------------------------------------
# 1c. custom 模式：直连客户库初始化（表结构 + 种子）
# ---------------------------------------------------------------------
# 在 agent 容器内执行（自带 mysql client）；顺序唯一权威仍是 order.txt。
init_custom_db() {
  local host port user pass db
  host=$(env_get CUSTOM_MYSQL_HOST); port=$(env_get CUSTOM_MYSQL_PORT); port="${port:-3306}"
  user=$(env_get CUSTOM_MYSQL_USER); user="${user:-root}"
  pass=$(env_get MYSQL_PASSWORD)
  db=$(env_get MYSQL_DATABASE); db="${db:-meoc}"
  [ -n "$pass" ] || die "MYSQL_PASSWORD 为空，先跑 --stage env-custom"
  command -v mysql >/dev/null 2>&1 || die "缺 mysql 客户端（agent 镜像已内置；宿主机直跑需 apt install default-mysql-client）"

  # 本阶段常在 agent 容器（bridge 网络）内执行：用户填 127.0.0.1 指「宿主机上的库」，
  # 容器内要改走 host.docker.internal（compose 已给 agent 配 host-gateway）。
  # .env 里存的仍是原始值 —— 业务服务是 host 网络，用 127.0.0.1 才是正确地址。
  # 宿主机直跑本脚本时无此覆盖变量，自然用原始值。
  if [ -n "${CUSTOM_DB_HOST_OVERRIDE:-}" ]; then
    log "灌库连接地址覆盖：${host} → ${CUSTOM_DB_HOST_OVERRIDE}（容器内视角）"
    host="$CUSTOM_DB_HOST_OVERRIDE"
  fi

  # 先验证连通与权限：建不了库说明账号权限不足，早报早明白
  log "验证数据库连通性 ${host}:${port}/${db}"
  mysql -h"$host" -P"$port" -u"$user" -p"$pass" -e "CREATE DATABASE IF NOT EXISTS \`$db\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci" \
    || die "连不上或无建库权限：mysql -h$host -P$port -u$user（检查地址/口令/账号权限）"
  ok "数据库连通，库 ${db} 就绪"

  log "灌入表结构与种子（顺序 = order.txt）"
  export MYSQL_HOST="$host" MYSQL_PORT="$port" MYSQL_USER="$user" MYSQL_PASSWORD="$pass" I_ACCEPT_EULA=yes
  bash deploy/scripts/sql/init_all.sh || die "init_all.sh 执行失败（看上方 SQL 错误）"
  ok "数据库初始化完成"
}

# ---------------------------------------------------------------------
# 1d. custom 模式：向外部 Nacos 导入配置（nacos-init 一次性任务）
# ---------------------------------------------------------------------
import_nacos_custom() {
  log "渲染并导入 Nacos 配置（外部地址）"
  # run --no-deps：外部轨绝不能拉起 mysql/nacos 依赖（会去抢宿主机端口）。
  # --build：nacos-init 镜像 tag 固定，改了 deploy/nacos-init/ 不重建不生效。
  compose_custom run --rm --no-deps --build nacos-init \
    || die "nacos-init 导入失败（检查 NACOS_ADDR/口令/命名空间权限）"
  ok "Nacos 配置已导入（39 个 dataId）"
}


# ---------------------------------------------------------------------
# 2. 中间件：起 + 等就绪
# ---------------------------------------------------------------------
# Nacos 内嵌 Derby 的失败模式（2026-10-27 实测踩坑）：
#   首次建库有 30s 登录超时，装机时 MySQL 正在建 700+ 张表（重 I/O），Derby 建库
#   偶发超时 → 留下「有目录、缺 service.properties」的残缺库 → 之后每次 restart 都
#   崩在同一处，安装永久卡死。报错只写 load derby-schema.sql error，看不出真因；
#   而 `docker compose down` 不清数据卷，所以「重装一次」也救不回来。
# 两道防线：
#   1) docker-compose.yml 里 nacos 的 entrypoint 每次启动前清掉残缺目录（自愈）；
#   2) 这里在判定失败后主动清一次并重建容器（容器已在运行时不重跑 entrypoint）。
nacos_clear_derby() {
  local project vol img
  project=$(env_get COMPOSE_PROJECT_NAME); project="${project:-unimall}"
  vol="${project}_nacos-data"
  docker volume inspect "$vol" >/dev/null 2>&1 || return 1
  # 只有「确实残缺」才动数据卷，避免把正常运行的配置库删掉
  compose logs --tail=300 nacos 2>/dev/null \
    | grep -qE "service\.properties|SQLTimeoutException|Login timeout exceeded" || return 1
  img=$(env_get NACOS_IMAGE); img="${img:-nacos/nacos-server:v2.3.2}"
  log "检测到 Nacos 内嵌 Derby 残缺（首次建库登录超时），清理 ${vol}/derby-data 后重启"
  compose rm -sf nacos >/dev/null 2>&1 || true
  docker run --rm -v "${vol}:/d" --entrypoint sh "$img" -c 'rm -rf /d/derby-data' >/dev/null 2>&1 \
    || return 1
  return 0
}

# 非致命版本：失败返回 1，由调用方决定是重试还是退出
wait_healthy_soft() {
  local svc="$1" timeout="${2:-300}" waited=0 state cid code
  log "等待 ${svc} 就绪（最多 ${timeout}s）"
  while [ "$waited" -lt "$timeout" ]; do
    # 必须用 -a：docker compose ps 默认只列「运行中」的容器，一次性任务
    # （nacos-init）正常退出后 ID 就取不到，inspect 拿到空值 → 一直判 missing，
    # 于是一次已经成功的初始化被当成没就绪，白等到超时。
    cid=$(compose ps -aq "$svc" 2>/dev/null | head -1)
    state=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo "missing")
    case "$state" in
      healthy|running) ok "${svc} ${state}"; return 0 ;;
      exited)
        # nacos-init 这类一次性任务正常结束就是 exited 0
        code=$(docker inspect -f '{{.State.ExitCode}}' "$cid" 2>/dev/null || echo 1)
        if [ "$code" = "0" ]; then ok "${svc} 已完成（exit 0）"; return 0; fi
        warn "${svc} 异常退出（exit ${code}）"
        return 1
        ;;
    esac
    sleep 5; waited=$((waited + 5))
  done
  warn "${svc} ${timeout}s 内未就绪"
  return 1
}

wait_healthy() {
  local svc="$1" timeout="${2:-300}" repaired=0
  while :; do
    if wait_healthy_soft "$svc" "$timeout"; then return 0; fi
    # nacos 专有自愈：清掉残缺 Derby 后重建容器，只重试一轮，避免无限循环掩盖真问题
    if [ "$svc" = "nacos" ] && [ "$repaired" -eq 0 ] && nacos_clear_derby; then
      repaired=1
      compose up -d --force-recreate nacos >/dev/null 2>&1 || true
      timeout=300
      continue
    fi
    die "${svc} 未就绪。看日志：docker compose logs --tail=120 ${svc}"
  done
}

# 起容器前校验：解析出来的容器名必须全部属于当前项目。
# 2026-10-27 实测教训：container_name 写死时，compose 并不是「按名字」找容器，
# 而是按 com.docker.compose.project + service 标签找，找到就 recreate——
# 于是一次「只想在旁路装一套验收环境」的操作，把另一套的容器停掉重建了，
# 而日志里只有一行不起眼的 Recreate，看不出越界。
guard_project_isolation() {
  local project names bad="" n count
  project=$(env_get COMPOSE_PROJECT_NAME); project="${project:-unimall}"
  names=$(compose config 2>/dev/null | awk '/container_name:/{print $2}') \
    || die "docker compose config 解析失败，拒绝启动"
  [ -n "$names" ] || die "编排里没有任何 container_name，解析异常，拒绝启动"
  for n in $names; do
    case "$n" in
      "${project}-"*) ;;
      *) bad="$bad $n" ;;
    esac
  done
  if [ -n "$bad" ]; then
    die "以下容器名不属于当前项目 ${project}：${bad}
    继续启动会操作到别的项目的容器。检查 .env 的 COMPOSE_PROJECT_NAME，
    以及 compose 里是否还有写死的 container_name（应统一用 \${COMPOSE_PROJECT_NAME:-...} 前缀）。"
  fi
  count=$(printf '%s\n' "$names" | wc -l | tr -d ' ')
  # 宿主机同名容器检测（2026-10-27 实测事故）：compose create 遇到
  # 容器重名时 Docker 不会报错，而是把既有容器悄悄 rename 成
  # `<hash>_旧名` 再建新的 —— 曾把生产 unimall-bff-web 改名成
  # 05b247d4e853_unimall-bff-web（容器还在跑，但按名字找容器的
  # 发版/监控脚本全部失灵）。这里必须在启动前把这种局面拦下来。
  local existing
  existing=$(docker ps -a --format '{{.Names}}' 2>/dev/null)
  for n in $names; do
    printf '%s\n' "$existing" | grep -qx "$n" || continue
    is_mine_container "$n" && continue
    die "宿主机上已存在不属于本项目 ${project} 的同名容器：${n}
    直接启动会让 Docker 把它强制改名（<hash>_${n}），如果那是生产/别套环境的容器，
    按名字找容器的脚本会当场失灵。二选一：
      1) 本机已装过另一套 → 改 .env 的 COMPOSE_PROJECT_NAME 换个前缀（容器名/网络/卷全部跟随）；
      2) 这是残留 → 先执行 ./install.sh down 清理。"
  done
  ok "容器名全部归属项目 ${project}（${count} 个），且与宿主机既有容器无重名"
}

# MySQL 首次初始化要建 700+ 张表并灌种子，慢盘上 10-15 分钟属正常；
# 这段时间官方 entrypoint 用 --skip-networking，TCP 健康检查必然失败。
# 按普通服务那样等到 300s 就给一句「未就绪」，会让人以为装坏了并反复重装。
wait_mysql_ready() {
  # 超时给 1 小时：这是「建 700+ 张表 + 灌种子」的纯 I/O 活儿，在忙的宿主机上实测
  # 只有 4-25 张表/分钟（同一台 80 核共享机，取决于邻居负载）。1500s 曾在 635 张表
  # 时误判超时 —— 重装只会从头再来一遍，代价比多等 20 分钟大得多。
  local timeout="${1:-3600}" waited=0 state cid last=0 q n
  log "等待 mysql 就绪（首次初始化 700+ 张表，10-40 分钟属正常，每分钟报一次已建表数）"
  while [ "$waited" -lt "$timeout" ]; do
    cid=$(compose ps -aq mysql 2>/dev/null | head -1)
    state=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo missing)
    case "$state" in
      healthy) ok "mysql healthy（耗时 ${waited}s）"; return 0 ;;
      exited)  die "mysql 异常退出。看日志：docker compose logs --tail=80 mysql" ;;
    esac
    if [ $((waited - last)) -ge 60 ]; then
      last=$waited
      # ⚠️ 必须 `|| true`：初始化早期 mysqld 还在建表，docker exec 会失败；
      #    裸命令在 set -e 下会让整个安装脚本静默退出（连错误都不打印）。
      # 报「已建表数」而不是 processlist：初始化期间 processlist 常给出 NULL，
      # 看不出到底是卡住了还是在推进；表数一直涨就说明正常。
      n=$(docker exec "$cid" sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" -N -e \"select count(*) from information_schema.tables where table_schema=database()\" \"\$MYSQL_DATABASE\"" 2>/dev/null | tr -d '\n' || true)
      q=$(docker exec "$cid" sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" -N -e \"select concat(state,' | ',left(replace(info,char(10),' '),50)) from information_schema.processlist where command<>'Sleep' order by time desc limit 1\"" 2>/dev/null | tr -d '\n' || true)
      log "  ${waited}s · 已建 ${n:-?} 张表 · ${q:-初始化中}"
    fi
    sleep 5; waited=$((waited + 5))
  done
  die "mysql ${timeout}s 内未就绪。若日志停在 running /docker-entrypoint-initdb.d/00-init.sql，
    说明初始化还没跑完，别急着重装（重装只会从头再来一遍）。"
}

start_infra() {
  log "启动中间件：${INFRA_SERVICES}"
  # 🔴 顺序很重要：先单独把 Nacos 拉起来，再起 MySQL。
  # Nacos 内嵌 Derby 首次建库有 30s 登录超时，而 MySQL 首次初始化要建 700+ 张表，
  # 是整台机器 I/O 最重的一段。两者同时启动时 Derby 建库偶发超时（实测踩过），
  # 把最容易翻车的一步放到机器最闲的时候做，失败率直接从「偶发」降到「不会」。
  # 🔴 装到一半失败后重跑前，先 ./install.sh down：半启动的容器占着端口，
  #    compose 对 Created 态容器重建时会再撞一遍（实测踩过）。
  compose up -d nacos || die "nacos 启动失败。若是端口冲突（ss 查不到的 IPv6/代理残留也算）：
    改 .env 的 NACOS_HOST_PORT/NACOS_GRPC_PORT/NACOS_GRPC_RAFT_PORT 后重跑；若是本项目残留，先 ./install.sh down。"
  wait_healthy nacos 300
  # nacos 已就绪，nacos-init 可以在 MySQL 初始化期间并行导入 40 个 dataId，不占关键路径。
  # 必须 --build + --force-recreate：
  #   --force-recreate：它是一次性容器，上一轮失败会留下 Exited(1) 的实例，
  #     直接 up 会「已存在即跳过」，安装继续往下走，直到 base 起不来才发现配置没导入。
  #   --build：镜像 tag 固定是 unimall/nacos-init:latest，机器上只要存在旧镜像，
  #     compose 就不会按新 entrypoint 重建 —— 改了 deploy/nacos-init/ 却不生效，
  #     症状是「代码明明改了、行为还是旧的」。镜像很轻，每次重建代价可以忽略。
  compose up -d --build --force-recreate nacos-init || die "nacos-init 启动失败，先清半启动状态再重跑：./install.sh down"
  compose up -d mysql redis rocketmq-namesrv rocketmq-broker || die "中间件启动失败。若是端口冲突：改 .env 的 MYSQL_HOST_PORT/REDIS_HOST_PORT/ROCKETMQ_NAMESRV_PORT；若是本项目残留：先 ./install.sh down"
  wait_mysql_ready 3600
  wait_healthy redis 120
  wait_healthy rocketmq-broker 180
  # nacos-init 导入 40 个 dataId，是 base 能否拿到配置的前提
  wait_healthy nacos-init 420
}

# ---------------------------------------------------------------------
# 3. 应用服务
# ---------------------------------------------------------------------
build_apps() {
  log "构建应用镜像（首次会编 26 个 Maven 模块，约 10-20 分钟）"

  # builder 镜像必须与源码绑定：tag 带源码指纹，指纹变了自然重建。
  # 复用一个「存在但陈旧」的 builder 会把上一版代码烤进 jar，且所有日志都很正常。
  local base_tag fp tag
  base_tag=$(env_get BUILDER_IMAGE); base_tag="${base_tag:-houcloud/builder:latest}"
  fp=$(source_fingerprint)
  tag="${base_tag%%:*}:src-${fp}"
  if docker image inspect "$tag" >/dev/null 2>&1; then
    ok "builder 镜像 ${tag} 已存在（源码指纹一致），复用"
  else
    warn "构建 builder ${tag}（源码指纹 ${fp}，这一步最慢）"
    docker build -f deploy/Dockerfile.builder -t "$tag" . || die "builder 构建失败，见上方 Maven 输出"
  fi
  # 让 compose 用指纹 tag（shell 环境变量优先于 .env）
  export BUILDER_IMAGE="$tag"
  # admin-web 一并构建：安装向导的「完成页 → 启动管理后台」要求它已就绪（up 即秒起）；
  # 有 Docker layer 缓存时增量很快，首次 npm install + vite build 约 5-15 分钟。
  compose build $APP_SERVICES admin-web || die "应用镜像构建失败，先看 docker compose build 的输出"

  # ------------------------------------------------------------ 插件落地
  # 11 个插件是「默认安装」的：jar 由 builder 的插件步骤产出，登记靠各插件的
  # deploy/register.sql。这两步原来整包里都没有 —— register.sql 全仓无人执行、
  # 插件 jar 一个都不产出，于是插件中心永远是空表，文档写的「默认安装」从未生效。
  # 顺序不能反：先铺 jar（宿主插件引擎启动时按目录一次性装载），再写库（菜单/
  # 安装记录），最后才起宿主。
  log "投放插件制品到 ${PLUGINS_DIR:-./data/plugins}"
  compose run --rm --no-deps plugin-seed || die "插件制品投放失败（见上方 [plugin-seed] 输出）"
  log "登记插件（插件中心条目 / 安装记录 / 菜单 / 市场条目）"
  compose run --rm --no-deps plugin-register || die "插件登记失败（见上方 [plugin-register] 错误）"

}

start_apps() {
  log "启动应用服务：${APP_SERVICES}"
  if [ "$INSTALL_MODE" = "custom" ]; then
    # 外部轨：host 网络 + 绝不拉起中间件依赖（详见 compose_custom 注释）
    compose_custom up -d --no-deps $APP_SERVICES || die "应用服务启动失败（host 网络，宿主端口冲突时看容器日志）"
  else
    compose up -d $APP_SERVICES
  fi
  wait_healthy bff-web 480
}

# ---------------------------------------------------------------------
# 4. 验收：不是「容器起来了」，而是「接口通了」
# ---------------------------------------------------------------------
# 种子完整性校验（fail-closed）：曾两次实测「容器全绿但 admin 不在」——
#   ① 20 号迁移 AFTER 引用未来列，source 链中止、种子段全没跑；
#   ② 数据卷是旧残留（上轮 down -v 没删净）→ initdb 被整体跳过、库是空壳。
# 两种都能走到 smoke 且接口验收通过，唯独登录不了。所以「安装完成」的判定
# 必须包含：admin 账号在、核心菜单在 —— 缺一个就是假成功，直接判失败。
seed_check() {
  log "校验数据库种子完整性（admin/菜单）"
  local db="$ROOT" q project admin menus plugins expected
  db=$(env_get MYSQL_DATABASE); db="${db:-meoc}"
  if [ "$INSTALL_MODE" = "custom" ]; then
    local host user pass
    host=$(env_get CUSTOM_MYSQL_HOST); host="${CUSTOM_DB_HOST_OVERRIDE:-$host}"
    user=$(env_get CUSTOM_MYSQL_USER); user="${user:-root}"
    pass=$(env_get MYSQL_PASSWORD)
    admin=$(mysql -h"$host" -P"$(env_get CUSTOM_MYSQL_PORT)" -u"$user" -p"$pass" "$db" \
      -N -e "SELECT COUNT(*) FROM hou_user_info WHERE user_id=1" 2>/dev/null || echo 0)
    menus=$(mysql -h"$host" -P"$(env_get CUSTOM_MYSQL_PORT)" -u"$user" -p"$pass" "$db" \
      -N -e "SELECT COUNT(*) FROM hou_permission_menu" 2>/dev/null || echo 0)
    plugins=$(mysql -h"$host" -P"$(env_get CUSTOM_MYSQL_PORT)" -u"$user" -p"$pass" "$db" \
      -N -e "SELECT COUNT(*) FROM hou_addon_plugin_install WHERE installed=1 AND deleted=0" 2>/dev/null || echo 0)
  else
    project=$(env_get COMPOSE_PROJECT_NAME); project="${project:-unimall}"
    admin=$(docker exec "${project}-mysql" sh -c \
      "mysql -umeoc -p\"\$MYSQL_PASSWORD\" '$db' -N -e \"SELECT COUNT(*) FROM hou_user_info WHERE user_id=1\"" 2>/dev/null | tr -d '[:space:]' || echo 0)
    menus=$(docker exec "${project}-mysql" sh -c \
      "mysql -umeoc -p\"\$MYSQL_PASSWORD\" '$db' -N -e \"SELECT COUNT(*) FROM hou_permission_menu\"" 2>/dev/null | tr -d '[:space:]' || echo 0)
    plugins=$(docker exec "${project}-mysql" sh -c \
      "mysql -umeoc -p\"\$MYSQL_PASSWORD\" '$db' -N -e \"SELECT COUNT(*) FROM hou_addon_plugin_install WHERE installed=1 AND deleted=0\"" 2>/dev/null | tr -d '[:space:]' || echo 0)
  fi
  [ "$admin" = "1" ] || die "种子校验失败：初始管理员(admin, user_id=1) 不在库中。
    这说明初始化 SQL 链没有完整执行（常见原因：数据卷是旧残留，MySQL 跳过了 initdb）。
    处理：bash deploy/install.sh --purge 清掉数据卷后重新安装；向导里则是「重置安装 → PURGE → 重新安装」。"
  case "$menus" in
    ''|*[!0-9]*) die "种子校验失败：菜单表查询异常" ;;
  esac
  [ "$menus" -gt 500 ] || die "种子校验失败：核心菜单仅 ${menus} 条（正常应上千）——种子段未完整执行。同上：purge 后重装。"

  # 插件「默认安装」同样要查库，不能只看容器绿。
  # 期望值不写死：按树上实际有多少个 deploy/register.sql 来算，树变了门槛自动跟着变。
  # 之前这一步完全没有 —— 插件中心是空表也能一路走到「安装完成」，文档却写着
  # 「默认安装 11 个插件」，属于装完了才发现对不上。
  expected=$(ls "$ROOT"/plugins/*/*/deploy/register.sql 2>/dev/null | wc -l | tr -d ' ')
  case "$plugins" in
    ''|*[!0-9]*) die "插件校验失败：安装记录表查询异常" ;;
  esac
  if [ "$expected" -gt 0 ] && [ "$plugins" -lt "$expected" ]; then
    die "插件校验失败：已安装 ${plugins} 个，树上带 ${expected} 个 register.sql。
    说明 plugin-seed / plugin-register 没跑成功（jar 没铺到插件目录，或登记 SQL 失败）。
    单跑这两步：docker compose run --rm --no-deps plugin-seed，再 plugin-register。"
  fi
  ok "admin 账号在位、核心菜单 ${menus} 条、插件已安装 ${plugins}/${expected} 个"
}

smoke() {
  local port; port=$(env_get BFF_PORT); port="${port:-8391}"
  local base="http://127.0.0.1:${port}"
  log "接口验收 ${base}"

  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 "${base}/web/base/index/index" || echo 000)
  [ "$code" = "000" ] && die "bff-web ${port} 端口不通。看 docker compose logs --tail=100 bff-web"
  ok "bff-web 响应 HTTP ${code}"

  # 🔴 先查种子再报完成：容器全绿 ≠ 可登录（见 seed_check 头注释的两起实测事故）
  seed_check

  # 插件中心控制面存在性。真路径是 POST /web/plugin-center/**（base 的
  # PluginCenterController），原先这里写的是 GET /web/plugin/plugin-center/list ——
  # 路径拼错，永远 404，而分支只打 warn，于是这条验收从来没生效过。
  # 未登录被拒（401/403/业务 code）都属正常，只有 404/000 说明模块没装配，必须判失败。
  local pcode
  pcode=$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 -X POST \
    -H 'Content-Type: application/json' -d '{}' "${base}/web/plugin-center/summary" || echo 000)
  case "$pcode" in
    404|000) die "插件中心接口不可达（HTTP ${pcode}）：base 的 plugin-center 模块未装配或路由缺失" ;;
    *) ok "插件中心接口在位（HTTP ${pcode}，未登录被拒属正常）" ;;
  esac
}

summary() {
  local port; port=$(env_get BFF_PORT); port="${port:-8391}"
  cat <<EOF

============================================================
 安装完成

   门户/BFF      http://<本机IP>:${port}
   管理后台      http://<本机IP>:${port}/admin$([ "$WITH_ADMIN" -eq 1 ] && echo '（admin-web 容器）')
   Nacos 控制台  http://<本机IP>:$(env_get NACOS_HOST_PORT || echo 8848)/nacos
   MySQL         127.0.0.1:$(env_get MYSQL_HOST_PORT || echo 3306)  库 $(env_get MYSQL_DATABASE || echo meoc)

   初始管理员    admin / 见 105-tenant-seed.sql 与 99-seed.sql 的种子口令
                 演示租户    demo（演示企业 org_id=1001）

   插件交易凭据点这里看：.env 中的 HOU_PLUGIN_PAY_ADMIN_TOKEN
     取法： grep '^HOU_PLUGIN_PAY_ADMIN_TOKEN=' .env
     用途： 调 /base/plugin-center/market/pay/admin/** 配收款渠道（放在 X-Pay-Admin-Token 头）
EOF
  if [ "$(env_get HOU_PLUGIN_PAY_SANDBOX)" = "true" ]; then
    cat <<EOF

   ⚠️ 插件购买当前是【沙箱模式】(HOU_PLUGIN_PAY_SANDBOX=true)
      不扣真实资金，任何人登录后都能把订单刷成已支付并领走授权。
      正式对外收费前必须改 .env：HOU_PLUGIN_PAY_SANDBOX=false
      并配好 HOU_PLUGIN_PAY_CASHIER_API_KEY（回调验签密钥），然后
        docker compose up -d --force-recreate base
EOF
  fi
  cat <<EOF

   常用命令
     docker compose ps
     docker compose logs -f --tail=100 base
     bash deploy/install.sh --down     # 停服保数据
     bash deploy/install.sh --purge    # 停服删数据卷

   下一步：登录后在「平台 → 插件中心」安装插件，或到
   「平台 → 插件发布」上传自己的 jar 走审核上架。
============================================================
EOF
}

# ---------------------------------------------------------------------
# main
# ---------------------------------------------------------------------
# 安装状态持久化：data/install-state.json 是「是否已安装」的唯一权威。
# 防重复进入安装流程三道闸：
#   1) agent 的 /start 在 installed 态一律拒绝；
#   2) SPA 挂载即调 /status，installed → 直接渲染完成页并锁死重装入口；
#   3) 安装完成后 install-web 容器即停，域名端口交还 admin-web，
#      /install/ 不再由安装向导接管（admin-web 里的同名页会提示已装完）。
mark_state() {  # mark_state <state> <step> [error]
  local f="$ROOT/$STATE_FILE" state="$1" step="$2" err="${3:-}"
  mkdir -p "$(dirname "$f")"
  if command -v python3 >/dev/null 2>&1; then
    python3 -c 'import json,sys,datetime
f,st,step,mode,err=sys.argv[1:6]
d={}
try: d=json.load(open(f,encoding="utf-8"))
except Exception: d={}
d.update(state=st,step=step,mode=mode,error=err)
d["updatedAt"]=datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
json.dump(d,open(f,"w",encoding="utf-8"),ensure_ascii=False,indent=2)' \
      "$f" "$state" "$step" "$INSTALL_MODE" "$err" || true
  else
    printf '{"state":"%s","step":"%s","mode":"%s","error":"%s"}\n' \
      "$state" "$step" "$INSTALL_MODE" "$err" > "$f"
  fi
}

# 向导模式的前置检查（软版）：没有 docker 起不了 agent，硬拦；
# 内存不足只提醒 —— 具体报告由向导页的环境检测步骤展示，别让客户连页面都看不到。
preflight_wizard() {
  command -v docker >/dev/null 2>&1 \
    || die "未安装 docker。Debian/Ubuntu：curl -fsSL https://get.docker.com | sh"
  docker info >/dev/null 2>&1 \
    || die "docker 已安装但连不上 daemon（当前用户不在 docker 组？sudo usermod -aG docker \$USER 后重新登录）"
  compose version >/dev/null 2>&1 || die "docker compose 不可用"
  local mem_kb mem_gb
  mem_kb=$(awk '/MemTotal/{print $2}' /proc/meminfo 2>/dev/null || echo 0)
  mem_gb=$(( mem_kb / 1024 / 1024 ))
  [ "$mem_gb" -lt 8 ] && warn "内存 ${mem_gb}GB 低于最小集建议值 8GB，向导会标红但不阻断"
  ok "docker $(docker version --format '{{.Server.Version}}' 2>/dev/null || echo '?') / compose 可用 / 内存 ${mem_gb}GB"
}

start_wizard() {
  log "准备 .env（向导需要 compose 变量可解析）"
  ensure_env
  log "启动安装向导（install profile：install-web + install-agent）"
  # 🔴 agent 容器内跑 compose 的路径错位坑（见 compose_invoke 注释）：
  #    把宿主仓库绝对路径注入 agent，compose 请求才能在 dockerd 侧解析对挂载源。
  export HOUCLOUD_HOST_PROJECT_DIR="$ROOT"
  # --build：agent/web 镜像 tag 固定 latest，改了 deploy/install-agent/ 不重建不生效
  compose --profile install up -d --build install-web install-agent \
    || die "向导启动失败。常见原因：WEB_PORT 被占 → 改 .env 的 WEB_PORT；agent 构建失败 → 看上方构建日志"
  local port
  port=$(env_get WEB_PORT); port="${port:-8080}"
  cat <<EOF

============================================================
 安装向导已启动

   浏览器访问： http://<本机IP或域名>:${port}/
   （会自动跳转到安装页 /install/）

   ⚠️  安装期间 install-agent 拥有 docker.sock 权限，
       请勿将 ${port} 端口暴露到公网（反代域名仅限受信网络）。

   退出向导： bash deploy/install.sh --down
============================================================
EOF
}

# --stage：单阶段执行（向导 agent 编排的原子单元）。
# 幂等：重跑任一阶段不破坏已完成阶段的状态（.env/数据卷/compose up 全幂等）。
run_one_stage() {
  case "$STAGE_NAME" in
    preflight)      preflight ;;
    env)            ensure_env ;;
    env-custom)     ensure_env; ensure_env_custom ;;
    infra)          preflight; ensure_env; check_ports; guard_project_isolation; start_infra ;;
    build)          build_apps ;;
    apps)           start_apps ;;
    smoke)          smoke ;;
    custom-db)      init_custom_db ;;
    nacos-import)   import_nacos_custom ;;
    *) die "未知阶段：$STAGE_NAME（可用：preflight/env/env-custom/infra/build/apps/smoke/custom-db/nacos-import）" ;;
  esac
  # agent 解析此标记推进 UI；agent 不在时人眼看也无妨
  echo "##STAGE:${STAGE_NAME}:ok"
}

# 运行期目录（data/ 下的插件、制品、密钥）文件属主是容器里的 root，
# 普通用户 rm 会 Permission denied，必须借 root 容器清。
# agent 容器视角路径 → 宿主真实路径：同构挂载后 agent 的 cwd 与宿主路径一致，
# 本函数只剩「历史防御」意义（万一有人用旧配置起 agent），保留无副作用。
_host_abs() {  # $1=容器视角绝对路径 → stdout=宿主视角路径
  if [ -n "${HOUCLOUD_HOST_PROJECT_DIR:-}" ]; then
    case "$1" in
      /project/*) printf '%s/%s\n' "$HOUCLOUD_HOST_PROJECT_DIR" "${1#/project/}"; return ;;
    esac
  fi
  printf '%s\n' "$1"
}

purge_runtime_dirs() {
  local dirs d abs
  dirs="$(env_get PLUGINS_DIR) $(env_get ARTIFACTS_DIR) $(env_get PLUGIN_SIGN_SECRETS_DIR)"
  for d in $dirs; do
    case "$d" in /*) abs="$d" ;; *) abs="$ROOT/${d#./}" ;; esac
    abs="$(_host_abs "$abs")"
    [ -d "$abs" ] || continue
    docker run --rm -v "${abs}:/d" alpine sh -c 'rm -rf /d/* /d/.[!.]* 2>/dev/null || true' >/dev/null 2>&1 || true
    rmdir "$abs" 2>/dev/null || true
    ok "已清理运行期目录 ${d}"
  done
}

case "$ACTION" in
  wizard)
    preflight_wizard
    start_wizard
    ;;
  stage)
    [ -n "$STAGE_NAME" ] || die "--stage 需要阶段名（--help 查看可用阶段）"
    run_one_stage
    ;;
  down)
    log "停止全部服务（保留数据卷）"
    # HOUCLOUD_KEEP_INSTALL_PROFILE=1：向导 agent 调用（reset RESET）——保留 install
    # profile 自身存活，否则页面一点重置就把向导也停了（实测：agent 把自己 down 成 137）。
    # 纯命令行调用不传此变量，语义不变（down 连向导一起停）。
    if [ -n "${HOUCLOUD_KEEP_INSTALL_PROFILE:-}" ]; then
      compose --profile ai --profile commerce down
    else
      compose --profile install --profile ai --profile commerce down
    fi
    ok "已停止"
    ;;
  purge)
    warn "将删除所有数据卷（MySQL/Nacos/Redis 数据不可恢复）"
    read -r -p "确认删除？输入 PURGE 继续：" ans
    [ "$ans" = "PURGE" ] || { echo "已取消"; exit 1; }
    # 同 down：向导 agent 调用时保留 install profile（HOUCLOUD_KEEP_INSTALL_PROFILE）
    if [ -n "${HOUCLOUD_KEEP_INSTALL_PROFILE:-}" ]; then
      compose --profile ai --profile commerce down -v
    else
      compose --profile install --profile ai --profile commerce down -v
    fi
    purge_runtime_dirs
    # 安装状态一并重置：向导回到「未安装」
    rm -f "$ROOT/$STATE_FILE" 2>/dev/null || true
    ok "已删除（install-state 一并重置）"
    ;;
  up)
    # 失败也把终态落盘（--stage 模式由 agent 写；命令行整流程由这里写）
    trap 'mark_state failed "$CURRENT_STEP" "命令行安装中断/失败，重跑 bash deploy/install.sh 继续（幂等）"' ERR
    CURRENT_STEP="preflight"
    preflight
    CURRENT_STEP="env";      ensure_env
    CURRENT_STEP="ports";    check_ports
    CURRENT_STEP="isolation"; guard_project_isolation
    if [ "$INSTALL_MODE" = "custom" ]; then
      CURRENT_STEP="env-custom";   ensure_env_custom
      CURRENT_STEP="custom-db";   init_custom_db
      CURRENT_STEP="nacos-import"; import_nacos_custom
    else
      CURRENT_STEP="infra";       start_infra
    fi
    CURRENT_STEP="build";    build_apps
    CURRENT_STEP="apps";     start_apps
    if [ "$WITH_ADMIN" -eq 1 ]; then
      log "启动管理后台前端 admin-web"
      compose up -d admin-web
      wait_healthy admin-web 240
    fi
    CURRENT_STEP="smoke";    smoke
    trap - ERR
    mark_state installed done
    summary
    ;;
esac
