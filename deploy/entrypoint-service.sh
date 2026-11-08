#!/bin/sh
# =====================================================================
# HouCloud 服务容器统一启动脚本
#
# 关键职责：拉起 Xvfb 虚拟显示后再启动 Java。
#
# 背景：GEO 平台的登录授权用 Playwright 驱动 Chromium。实测微信公众号在无头环境下
# 不下发登录二维码 —— 页面里 img.login__type__container__scan__qrcode 的 src 为空且
# visibility:hidden，直接调 /cgi-bin/scanloginqrcode?action=getqrcode 只会得到
# HTTP 200 + Content-Length: 0（微信把机房无头浏览器判定为自动化环境后主动拒绝发码）。
# 挂上 Xvfb 提供真实显示环境后二维码正常下发，用户用手机扫码即可完成登录并自动获取登录态。
#
# Xvfb 缺失或启动失败时直接继续启动 Java：服务本体不依赖图形环境，
# 仅 GEO 扫码登录/自动发表会回退到无头模式（能力下降但不会导致服务起不来）。
#
# 🔴 Xvfb 必须「脱离会话」启动（setsid + nohup）：
#   早先用 `Xvfb ... &` 启动，随后 `exec java` 替换了 shell，
#   Xvfb 变成孤儿进程被 PID 1 收割 —— 结果是「启动时提示 Xvfb 就绪，
#   但几分钟后进程消失、只剩 /tmp/.X11-unix/X99 这个空 socket」，
#   之后所有 Playwright 调用报「Missing X server or $DISPLAY」，
#   微信公众号自动发表（浏览器点「发表」）随之失效。
#   另：判断是否就绪必须看「进程是否存活」，不能只看 socket 文件是否存在
#   （socket 是残留文件时会被误判为就绪）。
# =====================================================================
set -e

DISPLAY_NUM="${HOU_GEO_HEADLESS_DISPLAY:-:99}"
SCREEN_SIZE="${HOU_GEO_SCREEN_SIZE:-1440x1000x24}"
SOCKET_PATH="/tmp/.X11-unix/X${DISPLAY_NUM#:}"

# Xvfb 进程是否真的在跑（socket 文件存在不代表进程存活）
xvfb_alive() {
  pgrep -f "Xvfb ${DISPLAY_NUM}" >/dev/null 2>&1
}

start_xvfb() {
  # 残留 socket 会让 Xvfb 启动失败（"Server is already active"），先清掉
  rm -f "$SOCKET_PATH" 2>/dev/null || true
  # setsid 让它脱离当前会话/进程组，exec java 后不会被 PID 1 收割
  setsid nohup Xvfb "$DISPLAY_NUM" -screen 0 "$SCREEN_SIZE" -nolisten tcp \
    >/tmp/xvfb.log 2>&1 &
  # 等待 socket 就绪，最多 5 秒
  i=0
  while [ $i -lt 25 ]; do
    [ -e "$SOCKET_PATH" ] && xvfb_alive && break
    i=$((i + 1))
    sleep 0.2
  done
}

if command -v Xvfb >/dev/null 2>&1; then
  # 已有活实例则复用，避免 :99 端口冲突
  if ! xvfb_alive; then
    start_xvfb
  fi
  if xvfb_alive; then
    export DISPLAY="$DISPLAY_NUM"
    echo "[entrypoint] Xvfb 就绪，DISPLAY=$DISPLAY_NUM，GEO 扫码登录与自动发表将有头模式可用"
  else
    echo "[entrypoint] 警告：Xvfb 未就绪，GEO 扫码登录与自动发表将回退无头模式（公众号等平台可能无法下发二维码）"
  fi

  # 🔴 保活：Xvfb 意外退出后自动拉起（否则自动发表会在运行一段时间后失效）
  (
    while sleep 10; do
      if ! xvfb_alive; then
        echo "[entrypoint] 检测到 Xvfb 退出，正在重新拉起"
        start_xvfb
      fi
    done
  ) &
else
  echo "[entrypoint] 警告：镜像内无 Xvfb，GEO 扫码登录与自动发表将回退无头模式"
fi

# ---- Redis 地址拆分 ----
# 服务 bootstrap.yml 的 Redis 兜底段引用 ${REDIS_HOST}/${REDIS_PORT}/${REDIS_PASSWORD}。
# Nacos redis.yaml 正常拉取时远端真值优先（覆盖兜底）；拉取失败时用这里的 env 兜底，
# 避免 ${REDIS_PASSWORD} 占位符解析失败导致启动即崩（2026-10-28 向导装机实锤）。
# REDIS_ADDR 形如 host:port（缺端口按 6379）；已有 REDIS_HOST/REDIS_PORT 时不覆盖。
if [ -n "${REDIS_ADDR:-}" ]; then
  case "$REDIS_ADDR" in
    *:*)
      _rh="${REDIS_ADDR%:*}"
      _rp="${REDIS_ADDR##*:}"
      ;;
    *)
      _rh="$REDIS_ADDR"
      _rp="6379"
      ;;
  esac
  case "$_rp" in
    *[!0-9]*) _rp="6379" ;;  # 端口非纯数字（如 IPv6 场景误拆）时回退默认
  esac
  export REDIS_HOST="${REDIS_HOST:-$_rh}"
  export REDIS_PORT="${REDIS_PORT:-$_rp}"
  echo "[entrypoint] Redis 兜底地址 ${REDIS_HOST}:${REDIS_PORT}（Nacos 正常时以远端配置为准）"
fi

exec java $JAVA_OPTS -jar /app/app.jar
