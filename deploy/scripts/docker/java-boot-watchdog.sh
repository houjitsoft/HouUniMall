#!/bin/bash
# =====================================================================
# HouCloud · 业务服务容器启动看门狗（java-boot-watchdog）
#
# 解决的事故（2026-10-15 RH2288）：
#   unimall-userms / unimall-order 在宿主机重启时 Nacos 还没起来，
#   Spring 抢跑注册失败 → "Application run failed" → Tomcat 停止监听，
#   但 JVM 因 RocketMQ 消费者线程（非 daemon）不退出，容器呈
#   `Up 2 days` + 端口 CLOSED 的「假死」态，restart: unless-stopped
#   完全不生效（策略只在容器退出时触发），假死 2 天无人发现。
#
# 本脚本作为容器 entrypoint，把 java 包成子进程并做三件事：
#   阶段 0  等 Nacos 端口可连（TCP），最多 JAVA_BOOT_WAIT_NACOS 秒
#   阶段 1  启动 java，最多 JAVA_BOOT_BOOT_TIMEOUT 秒内必须出现启动成功信号
#   阶段 2  持续守护：端口连续 JAVA_BOOT_STRIKE_MAX 次探测失败 → 杀掉 java 并
#           以非 0 退出，让 Docker 的 restart 策略真正接手
#
# 用法（compose）：
#   entrypoint: ["/bin/bash", "/app/boot/java-boot-watchdog.sh"]
#   volumes:
#     - ./deploy/scripts/docker/java-boot-watchdog.sh:/app/boot/java-boot-watchdog.sh:ro
#   environment:
#     JAVA_BOOT_LOG_NAME: userms      # <服务名>.log，取自 /app/logs/
#     JAVA_BOOT_PORT: "8195"          # 业务端口，没有就留空（只用日志判据）
#
# 全部参数都有默认值，不配任何 JAVA_BOOT_* 也能安全工作（退化为
# 「等 Nacos + 启动即守护」）。
#
# 🔴 日志文件必须存在才会启用日志判据；判据取自 Spring Boot 自身的
#    "Started <X>Application in N seconds" / "Application run failed" 两行，
#    与具体服务无关，故可一套脚本覆盖全部业务服务。
# =====================================================================
set -u

# ---------- 可调参数 ----------
JAVA_BOOT_LOG_DIR="${JAVA_BOOT_LOG_DIR:-/app/logs}"
JAVA_BOOT_LOG_NAME="${JAVA_BOOT_LOG_NAME:-}"
JAVA_BOOT_PORT="${JAVA_BOOT_PORT:-${SERVER_PORT:-}}"
JAVA_BOOT_WAIT_NACOS="${JAVA_BOOT_WAIT_NACOS:-600}"
JAVA_BOOT_BOOT_TIMEOUT="${JAVA_BOOT_BOOT_TIMEOUT:-300}"
JAVA_BOOT_PROBE_INTERVAL="${JAVA_BOOT_PROBE_INTERVAL:-5}"
JAVA_BOOT_STRIKE_MAX="${JAVA_BOOT_STRIKE_MAX:-12}"
JAVA_BOOT_JAR="${JAVA_BOOT_JAR:-/app/app.jar}"

log() { echo "[java-boot] $(date '+%Y-%m-%d %H:%M:%S') $*"; }

# ---------- Nacos 地址拆分（host:port，无端口默认 8848） ----------
NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
nh="${NACOS_ADDR%%:*}"
np="${NACOS_ADDR##*:}"
case "$NACOS_ADDR" in
  *:*) ;;
  *) np=8848 ;;
esac

port_open() {
  [ -n "${1:-}" ] || return 1
  (echo >"/dev/tcp/127.0.0.1/$1") 2>/dev/null
}

LOGFILE="$JAVA_BOOT_LOG_DIR/$JAVA_BOOT_LOG_NAME.log"

log_started() {
  [ -n "$JAVA_BOOT_LOG_NAME" ] || return 1
  [ -f "$LOGFILE" ] || return 1
  grep -qE 'Started .*Application in [0-9]+ seconds' "$LOGFILE" 2>/dev/null
}

log_failed() {
  [ -n "$JAVA_BOOT_LOG_NAME" ] || return 1
  [ -f "$LOGFILE" ] || return 1
  grep -qE 'Application run failed' "$LOGFILE" 2>/dev/null
}

# ===================== 阶段 0：等 Nacos =====================
log "waiting nacos ${nh}:${np} (max ${JAVA_BOOT_WAIT_NACOS}s)"
t0=$(date +%s)
while true; do
  if (echo >"/dev/tcp/$nh/$np") 2>/dev/null; then
    break
  fi
  el=$(($(date +%s) - t0))
  if [ "$el" -ge "$JAVA_BOOT_WAIT_NACOS" ]; then
    log "FATAL nacos ${nh}:${np} unreachable for ${el}s -> exit 1"
    exit 1
  fi
  log "nacos not reachable yet (${el}s/${JAVA_BOOT_WAIT_NACOS}s)"
  sleep 3
done
log "nacos reachable in $(($(date +%s) - t0))s"

# ===================== 阶段 1：启动 java =====================
# 先清掉上一轮残留的失败标记，避免误判（日志是追加写，容器重建时挂载盘会留下旧文件）
if [ -n "$JAVA_BOOT_LOG_NAME" ] && [ -f "$LOGFILE" ]; then
  log "log file $LOGFILE exists (append mode), using grep markers"
fi

log "starting java: java $JAVA_OPTS -jar $JAVA_BOOT_JAR"
# shellcheck disable=SC2086
java $JAVA_OPTS -jar "$JAVA_BOOT_JAR" &
JPID=$!
log "java pid=$JPID"

terminate() {
  log "signal received -> terminating java (pid=$JPID)"
  kill -TERM "$JPID" 2>/dev/null
  wait "$JPID" 2>/dev/null
  exit 0
}
trap terminate TERM INT

t1=$(date +%s)
booted=0
while kill -0 "$JPID" 2>/dev/null; do
  if log_failed; then
    log "FATAL spring reported 'Application run failed' -> kill java and exit 1"
    kill -9 "$JPID" 2>/dev/null
    wait "$JPID" 2>/dev/null
    exit 1
  fi
  if log_started || { [ -n "$JAVA_BOOT_PORT" ] && port_open "$JAVA_BOOT_PORT"; }; then
    booted=1
    log "startup signal seen after $(($(date +%s) - t1))s (port=${JAVA_BOOT_PORT:-none})"
    break
  fi
  el=$(($(date +%s) - t1))
  if [ "$el" -ge "$JAVA_BOOT_BOOT_TIMEOUT" ]; then
    log "FATAL no startup signal within ${el}s -> kill java and exit 1"
    kill -9 "$JPID" 2>/dev/null
    wait "$JPID" 2>/dev/null
    exit 1
  fi
  sleep "$JAVA_BOOT_PROBE_INTERVAL"
done

if [ "$booted" -ne 1 ]; then
  wait "$JPID"
  rc=$?
  log "java exited before any startup signal rc=$rc -> exit $rc"
  exit "$rc"
fi

# ===================== 阶段 2：持续守护 =====================
# 没配 JAVA_BOOT_PORT 时不做端口探测（否则 strike 会无限累加导致误杀），
# 只保留「java 存活 + Application run failed」两项判据。
if [ -z "$JAVA_BOOT_PORT" ]; then
  log "watchdog (log-only): no JAVA_BOOT_PORT, only monitoring java liveness"
  wait "$JPID"
  rc=$?
  log "java exited rc=$rc -> exit $rc"
  exit "$rc"
fi

log "watchdog active: probing port $JAVA_BOOT_PORT every ${JAVA_BOOT_PROBE_INTERVAL}s, ${JAVA_BOOT_STRIKE_MAX} consecutive fails = suicide"
strike=0
while kill -0 "$JPID" 2>/dev/null; do
  if log_failed; then
    log "FATAL 'Application run failed' appeared at runtime -> kill java and exit 1"
    kill -9 "$JPID" 2>/dev/null
    wait "$JPID" 2>/dev/null
    exit 1
  fi
  if port_open "$JAVA_BOOT_PORT"; then
    strike=0
  else
    strike=$((strike + 1))
    if [ "$strike" -ge "$JAVA_BOOT_STRIKE_MAX" ]; then
      log "FATAL port $JAVA_BOOT_PORT closed for $((strike * JAVA_BOOT_PROBE_INTERVAL))s -> kill java and exit 1"
      kill -9 "$JPID" 2>/dev/null
      wait "$JPID" 2>/dev/null
      exit 1
    fi
    log "port $JAVA_BOOT_PORT not listening (strike $strike/$JAVA_BOOT_STRIKE_MAX)"
  fi
  sleep "$JAVA_BOOT_PROBE_INTERVAL"
done

wait "$JPID"
rc=$?
log "java exited rc=$rc -> exit $rc"
exit "$rc"
