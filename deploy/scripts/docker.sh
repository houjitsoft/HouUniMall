#!/usr/bin/env bash
# =====================================================================
# HouCloud Docker 部署一键脚本（双轨入口）
#
#   bash deploy/scripts/docker.sh up        # 轨 B：全容器化（中间件 + 14 个核心服务 + 管理后台）
#   bash deploy/scripts/docker.sh full      # 轨 B 完整版：核心集 + commerce profile 10 个商业服务（共 24 业务服务，需再 +12GB 内存）
#   bash deploy/scripts/docker.sh demo      # 开源 Demo 最小集：中间件 + auth/permission/base/goods/order/payment/bff + admin
#   bash deploy/scripts/docker.sh mw        # 轨 A 加速：只起中间件，服务仍用 jar 跑在宿主机
#   bash deploy/scripts/docker.sh build     # 只构建镜像（先编后端 jar，再打各服务镜像）
#   bash deploy/scripts/docker.sh ps        # 查看容器状态
#   bash deploy/scripts/docker.sh logs mall # 看某个服务日志（省略服务名则看全部）
#   bash deploy/scripts/docker.sh nacos     # 重新渲染并导入 Nacos 配置（改完配置用这个）
#   bash deploy/scripts/docker.sh down      # 停止并移除容器（保留数据卷）
#   bash deploy/scripts/docker.sh down mw   # 停止中间件栈
#   bash deploy/scripts/docker.sh reset     # ⚠ 连数据卷一起删除（数据库会重新初始化）
#   bash deploy/scripts/docker.sh config    # 只做编排文件校验，不启动
#
# 首次使用：cp .env.docker.example .env
# =====================================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$REPO_ROOT"

FULL_FILE="docker-compose.yml"
MW_FILE="docker-compose.middleware.yml"

# 开源 Demo 最小集（用户可见 Quick Start；不含 mall/merchant/oa 等重服务）
DEMO_SERVICES=(
  builder mysql redis nacos rocketmq-namesrv rocketmq-broker nacos-init
  permission auth base goods order payment bff-web admin-web
)

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  DC="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  DC="docker-compose"
else
  echo "!! 未检测到 docker / docker compose，请先安装 Docker（含 Compose 插件）"
  exit 1
fi

# 可选：HOUCLOUD_PROFILES="ai es" bash deploy/scripts/docker.sh up   （启用可选组件）
PROFILES=()
if [ -n "${HOUCLOUD_PROFILES:-}" ]; then
  read -r -a PROFILES <<< "${HOUCLOUD_PROFILES}"
fi
# 可选：MW=1 bash deploy/scripts/docker.sh ps    （查看/操作中间件栈）
FILE="$FULL_FILE"
[ "${MW:-0}" = "1" ] && FILE="$MW_FILE"

require_env() {
  if [ ! -f .env ]; then
    echo "!! 缺少 .env"
    echo "   请先执行：cp .env.docker.example .env    （然后按需修改口令与端口）"
    exit 1
  fi
}

profile_args() {
  local out=""
  # 注意：${PROFILES[@]:-} 不能写成 ${PROFILES[@]}——macOS 自带 bash 3.2 下，
  # 空数组在 `set -u` 里会报 unbound variable 直接中断脚本。
  for p in ${PROFILES[@]:-}; do
    [ -n "$p" ] && out="$out --profile $p"
  done
  echo "$out"
}

compose() { # compose <compose-file> <args...>
  local file="$1"; shift
  # shellcheck disable=SC2086
  $DC -f "$file" $(profile_args) "$@"
}

case "${1:-}" in
  build)
    echo "==> 构建后端构建器镜像（一次性编译全部服务，首次较慢）"
    docker build -f docker/Dockerfile.builder -t "${BUILDER_IMAGE:-houcloud/builder:latest}" .
    echo "==> 构建各服务镜像 + 前端镜像"
    require_env
    compose "$FULL_FILE" build
    echo "==> 完成：docker images | grep houcloud"
    ;;

  full)
    require_env
    echo "==> 启动完整栈：24 个业务服务（14 核心 + commerce 10 商业）"
    echo "    内存需求：核心集 16GB + 商业服务约 12GB（共 ≥28GB）；不足请用 up / demo"
    docker build -f docker/Dockerfile.builder -t "${BUILDER_IMAGE:-houcloud/builder:latest}" .
    PROFILES+=("commerce")
    compose "$FULL_FILE" up -d --build
    cat <<'TIP'

==> 已发起完整栈启动。接下来：
    1) 观察服务状态：  bash deploy/scripts/docker.sh ps
    2) 浏览器打开：    http://localhost:${WEB_PORT:-8080}   （默认 8080）
       初始账号：admin / Admin@123456（首次登录后立即改密）
TIP
    ;;

  up)
    require_env
    echo "==> 启动全栈（首次会自动编译后端并初始化数据库 / Nacos 配置，约 10-20 分钟）"
    docker build -f docker/Dockerfile.builder -t "${BUILDER_IMAGE:-houcloud/builder:latest}" .
    compose "$FULL_FILE" up -d --build
    cat <<'TIP'

==> 已发起启动。接下来：
    1) 观察数据库初始化：docker compose logs -f mysql      （出现 "ready for start up" 即完成）
    2) 观察配置导入：    docker compose logs -f nacos-init  （出现 "配置初始化完成"）
    3) 观察服务状态：    bash deploy/scripts/docker.sh ps
    4) 浏览器打开：      http://localhost:${WEB_PORT:-8080}   （默认 8080）
       初始账号：admin / Admin@123456（首次登录后立即改密）
TIP
    ;;

  demo)
    require_env
    echo "==> 启动开源 Demo 最小集（auth/permission/base/goods/order/payment/bff + admin）"
    echo "    内存建议 ≥8GB；完整 14 服务请用：bash deploy/scripts/docker.sh up"
    docker build -f docker/Dockerfile.builder -t "${BUILDER_IMAGE:-houcloud/builder:latest}" .
    # shellcheck disable=SC2086
    compose "$FULL_FILE" up -d --build "${DEMO_SERVICES[@]}"
    cat <<'TIP'

==> Demo 已发起。接下来：
    1) bash deploy/scripts/docker.sh ps
    2) 打开 http://localhost:8080  （账号 admin / Admin@123456）
    3) 需要商户端 / 全量服务时再执行：bash deploy/scripts/docker.sh up
TIP
    ;;

  mw)
    require_env
    echo "==> 只启动中间件（MySQL / Redis / Nacos / RocketMQ + 自动初始化库与配置）"
    compose "$MW_FILE" up -d --build
    cat <<'TIP'

==> 中间件已启动（端口都映射到 127.0.0.1）。接下来按《安装手册》在宿主机跑 jar：
    export NACOS_ADDR=127.0.0.1:8848
    export NACOS_NAMESPACE=houeoc
    export NACOS_PASSWORD=<.env 里 NACOS_AUTH_PASSWORD 的值>
    java -jar backend/paas/permission/target/permission.jar
TIP
    ;;

  ps)
    require_env
    compose "$FILE" ps
    ;;

  logs)
    require_env
    if [ -n "${2:-}" ]; then
      compose "$FILE" logs -f --tail=200 "$2"
    else
      compose "$FILE" logs -f --tail=200
    fi
    ;;

  nacos)
    require_env
    echo "==> 重新渲染变量表并导入 Nacos 配置"
    # 必须用 -e 传进容器：前缀写法（FORCE_RENDER=true compose ...）只作用于 compose
    # 进程本身，容器里拿不到，会走「variables.env 已存在就保留」分支导致重新渲染无效。
    compose "$FILE" run --rm -e FORCE_RENDER=true nacos-init
    ;;

  down)
    require_env
    compose "$FILE" down
    ;;

  reset)
    echo "⚠️  这会删除数据库 / Redis / Nacos / RocketMQ 的全部数据卷，下次启动会重新初始化。"
    read -r -p "   确认请输入 yes： " ans
    [ "$ans" = "yes" ] || { echo "已取消"; exit 1; }
    compose "$FILE" down -v
    echo "==> 已清理。重新启动：bash deploy/scripts/docker.sh up"
    ;;

  config)
    require_env
    compose "$FULL_FILE" config >/dev/null && echo "==> $FULL_FILE 校验通过"
    compose "$MW_FILE" config >/dev/null && echo "==> $MW_FILE 校验通过"
    compose "$FULL_FILE" --profile commerce config >/dev/null && echo "==> $FULL_FILE (commerce profile) 校验通过"
    ;;

  *)
    sed -n '2,25p' "$0"
    exit 1
    ;;
esac
