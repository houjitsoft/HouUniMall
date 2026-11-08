#!/usr/bin/env python3
# =====================================================================
# HouCloud 界面化安装向导 · 安装代理（install-agent）
#
# 职责：把 deploy/install.sh --stage <name> 的阶段化安装内核包装成 HTTP API，
#       供 public/install/ 下的向导页面（经 install-web nginx 反代）调用。
#
# 设计约束：
#   1. 纯 Python 标准库（镜像零 pip 依赖，apk 只装 python3）。
#   2. .env 的唯一写者是 install.sh（--stage env / env-custom）；
#      本进程只读 .env（组装配 info），不直接改配置 —— 两个写者必漂移。
#   3. install-state.json 的唯一写者是本进程：install.sh --stage 模式只输出
#      ##STAGE:<name>:ok 标记，命令行整流程才会自己写终态（那时本进程不在）。
#   4. 防重复安装三道闸：state=installed 时 /start、/retry 一律 409；
#      SPA 挂载即查 /status 渲染终态页；安装完成 install-web 即停、端口交还 admin-web。
#   5. agent 只监听容器网络（不映射宿主端口），公网暴露面 = install-web 的 WEB_PORT，
#      文档已注明安装期间不要把该端口开放公网（本进程持有 docker.sock）。
#
# 安全模型：本进程能执行 docker 命令（挂载 docker.sock）+ 读写仓库（挂载 /project）。
#   它只编排 install.sh 的白名单阶段；reset 必须输入全词 RESET/PURGE；
#   installed 态拒绝一切变更类接口。
# =====================================================================
import json
import os
import pathlib
import re
import shlex
import socket
import subprocess
import threading
import time
import datetime
import urllib.request
import urllib.parse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PROJECT = pathlib.Path(os.environ.get("INSTALL_PROJECT_DIR", "/project"))
STATE_REL = os.environ.get("HOUCLOUD_INSTALL_STATE", "data/install-state.json")
STATE_FILE = PROJECT / STATE_REL
PORT = int(os.environ.get("INSTALL_AGENT_PORT", "8370"))

# agent 跑在 bridge 网络：用户填 127.0.0.1 指「宿主机上的中间件」，
# 容器内的 127.0.0.1 是 agent 自己 → 连不上。
# compose 已给 agent 配 extra_hosts host.docker.internal:host-gateway，
# 本函数仅在「agent 发起的临时连接」（连通测试 / 灌库）里重写；
# .env 永远存原始值（业务服务是 host 网络，用 127.0.0.1 才是对的）。
SELF_HOSTS = {"127.0.0.1", "localhost", "0.0.0.0"}

def rewrite_loopback(addr):
    """host:port 或纯 host → 容器可达形式（仅 agent 侧连接用）。"""
    s = str(addr).strip()
    host, sep, rest = s.partition(":")
    if host in SELF_HOSTS:
        return "host.docker.internal" + (":" + rest if sep else "")
    return s

# 阶段序列：docker 模式起容器化中间件；custom 模式直连外部中间件。
STAGES_DOCKER = ["env", "infra", "build", "apps", "smoke"]
STAGES_CUSTOM = ["env-custom", "custom-db", "nacos-import", "build", "apps", "smoke"]

STAGE_TITLES = {
    "env": "生成配置与随机口令",
    "env-custom": "写入外部中间件连接信息",
    "infra": "启动中间件容器（MySQL/Redis/Nacos/RocketMQ）",
    "custom-db": "初始化外部数据库（表结构 + 种子）",
    "nacos-import": "渲染并导入 Nacos 配置",
    "build": "构建应用镜像（首次 10-20 分钟）",
    "apps": "启动应用服务",
    "smoke": "接口验收",
}

_log_lock = threading.Lock()
_log_ring = []          # [(ts, line)] 环形日志，progress 接口读尾部
LOG_RING_MAX = 600

_install_thread = None
_install_lock = threading.Lock()   # 串行化 start/retry/finish/reset
_thread_gen = 0                     # 安装线程代号，防止旧线程结果覆盖新状态


def log(line):
    with _log_lock:
        _log_ring.append((time.strftime("%H:%M:%S"), str(line)))
        if len(_log_ring) > LOG_RING_MAX:
            del _log_ring[: len(_log_ring) - LOG_RING_MAX]


def ring_tail(n=80):
    with _log_lock:
        return [{"ts": t, "line": l} for t, l in _log_ring[-n:]]


# ---------------------------------------------------------------- state io
def read_state():
    try:
        return json.loads(STATE_FILE.read_text(encoding="utf-8"))
    except Exception:
        return {}


def write_state(update):
    d = read_state()
    d.update(update)
    d["updatedAt"] = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
    STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
    tmp = STATE_FILE.with_suffix(".tmp")
    tmp.write_text(json.dumps(d, ensure_ascii=False, indent=2), encoding="utf-8")
    tmp.replace(STATE_FILE)


def env_get(key, default=""):
    """读 PROJECT/.env（只读；.env 的写者是 install.sh）。"""
    try:
        for line in (PROJECT / ".env").read_text(encoding="utf-8").splitlines():
            if line.startswith(key + "="):
                return line.split("=", 1)[1].strip()
    except Exception:
        pass
    return default


# ---------------------------------------------------------------- preflight
def sh(args, timeout=15, env=None, cwd=None):
    p = subprocess.run(
        args, capture_output=True, text=True, timeout=timeout,
        env={**os.environ, **(env or {})}, cwd=cwd or str(PROJECT),
    )
    return p.returncode, (p.stdout or "") + (p.stderr or "")


def host_ip():
    """bridge 网络下宿主机地址 = 默认网关（探测宿主端口用）。"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return None


def preflight_report():
    items = []

    def add(name, ok, detail="", fatal=True):
        items.append({"name": name, "ok": bool(ok), "detail": detail, "fatal": fatal})

    code, out = sh(["docker", "version", "--format", "{{.Server.Version}}"])
    add("Docker Engine 24+", code == 0, out.strip()[:120])
    code, out = sh(["docker", "compose", "version", "--short"])
    add("docker compose v2", code == 0, out.strip()[:60])
    if items[-1]["ok"] is False:
        code, out = sh(["docker-compose", "--version"])
        add("docker-compose v1（降级可用）", code == 0, out.strip()[:60])

    try:
        mem_kb = int(next(l.split()[1] for l in open("/proc/meminfo") if l.startswith("MemTotal")))
        mem_gb = mem_kb // 1024 // 1024
    except Exception:
        mem_gb = 0
    add("内存 ≥ 8GB（最小集建议）", mem_gb >= 8, f"当前 {mem_gb}GB", fatal=False)

    try:
        cpu = sum(1 for _ in open("/proc/cpuinfo") if _.startswith("processor"))
    except Exception:
        cpu = 0
    add("CPU ≥ 4 核", cpu >= 4, f"当前 {cpu} 核", fatal=False)

    try:
        st = os.statvfs(str(PROJECT))
        free_gb = st.f_bavail * st.f_frsize / 1024 ** 3
    except Exception:
        free_gb = 0
    add("磁盘剩余 ≥ 30GB", free_gb >= 30, f"当前 {free_gb:.0f}GB", fatal=False)

    # 端口：只拦「本项目之外的容器」占用；宿主进程占用留给 compose up 兜底报错。
    project = env_get("COMPOSE_PROJECT_NAME", "unimall")
    ports = {k: env_get(k) for k in [
        "WEB_PORT", "BFF_PORT", "MYSQL_HOST_PORT", "REDIS_HOST_PORT",
        "NACOS_HOST_PORT", "NACOS_GRPC_PORT", "NACOS_GRPC_RAFT_PORT",
        "ROCKETMQ_NAMESRV_PORT"] if env_get(k)}
    busy_foreign = []
    code, out = sh(["docker", "ps", "--format", "{{.Names}}\t{{.Ports}}"])
    for ln in out.splitlines():
        parts = ln.split("\t")
        if len(parts) != 2:
            continue
        name, pstr = parts
        for pkey, pval in ports.items():
            if re.search(rf":{pval}->", pstr) and not name.startswith(project + "-"):
                busy_foreign.append(f"{pkey}({pval}) 被容器 {name} 占用")
    add("端口无外部容器冲突", not busy_foreign, "; ".join(busy_foreign) or
        "涉及端口 " + ", ".join(sorted(ports.values())), fatal=False)

    return {"items": items, "pass": all(i["ok"] or not i["fatal"] for i in items)}


# ---------------------------------------------------------------- custom 连接测试
def test_connection(cfg):
    res = {}

    m = cfg.get("mysql", {})
    if m.get("host"):
        # 连通测试从 agent 容器发起：127.0.0.1 重写为 host.docker.internal
        host = rewrite_loopback(m["host"])
        env = {"MYSQL_PWD": m.get("password", "")}
        code, out = sh(
            ["mysql", "-h" + host, "-P" + str(m.get("port", 3306)),
             "-u" + str(m.get("user", "root")), "-N", "-B", "-e",
             "SELECT VERSION();SHOW DATABASES LIKE '" + str(m.get("database", "meoc")) + "'"],
            timeout=10, env=env)
        okk = code == 0 and bool(out.strip())
        db_exists = okk and len(out.strip().splitlines()) >= 2
        res["mysql"] = {"ok": okk,
                        "detail": ("已连通，库已存在（将跳过建库）" if db_exists else "已连通，库不存在（安装时自动创建）") if okk else out.strip()[-200:]}
    else:
        res["mysql"] = {"ok": False, "detail": "未填写"}

    r = cfg.get("redis", {})
    if r.get("addr"):
        env = {"REDISCLI_AUTH": r.get("password", "")}
        raddr = rewrite_loopback(r["addr"])
        code, out = sh(["redis-cli", "-h" + raddr.split(":")[0],
                        "-p" + (raddr.split(":")[1] if ":" in raddr else "6379"),
                        "PING"], timeout=8, env=env)
        res["redis"] = {"ok": "PONG" in out, "detail": "PONG" if "PONG" in out else out.strip()[-160:]}
    else:
        res["redis"] = {"ok": False, "detail": "未填写"}

    n = cfg.get("nacos", {})
    if n.get("addr"):
        try:
            data = urllib.parse.urlencode({
                "username": n.get("user", "nacos"), "password": n.get("password", "")}).encode()
            resp = urllib.request.urlopen(
                "http://" + rewrite_loopback(n["addr"]).rstrip("/") + "/nacos/v1/auth/login", data=data, timeout=8)
            body = json.loads(resp.read().decode())
            okk = "accessToken" in body
            res["nacos"] = {"ok": okk, "detail": "登录成功" if okk else "无 token 返回"}
        except Exception as e:
            res["nacos"] = {"ok": False, "detail": str(e)[:160]}
    else:
        res["nacos"] = {"ok": False, "detail": "未填写"}

    q = cfg.get("rocketmq", {})
    if q.get("addr"):
        host, _, port = rewrite_loopback(q["addr"]).partition(":")
        try:
            s = socket.create_connection((host, int(port or 9876)), timeout=5)
            s.close()
            res["rocketmq"] = {"ok": True, "detail": "NameServer TCP 可达"}
        except Exception as e:
            res["rocketmq"] = {"ok": False, "detail": str(e)[:160]}
    else:
        res["rocketmq"] = {"ok": False, "detail": "未填写（最小集可不填）"}

    return res


# ---------------------------------------------------------------- 安装编排
def custom_env_from_config(cfg):
    """向导表单 → install.sh --stage env-custom 的环境变量（CUSTOM_* 协议）。"""
    m = cfg.get("mysql", {}); r = cfg.get("redis", {}); n = cfg.get("nacos", {}); q = cfg.get("rocketmq", {})
    env = {}
    if m.get("host"):
        env["CUSTOM_MYSQL_HOST"] = str(m["host"])
        env["CUSTOM_MYSQL_PORT"] = str(m.get("port", 3306))
        env["CUSTOM_MYSQL_USER"] = str(m.get("user", "root"))
        env["CUSTOM_MYSQL_PASSWORD"] = str(m.get("password", ""))
        env["CUSTOM_MYSQL_DATABASE"] = str(m.get("database", "meoc"))
        # 灌库（custom-db 阶段）也在 agent 容器内连库：127.0.0.1 要重写，
        # 但 install.sh 写进 .env 的是原始值（业务服务 host 网络用原始值才对）
        env["CUSTOM_DB_HOST_OVERRIDE"] = rewrite_loopback(m["host"])
    if r.get("addr"):
        env["CUSTOM_REDIS_ADDR"] = str(r["addr"])
        env["CUSTOM_REDIS_PASSWORD"] = str(r.get("password", ""))
    if n.get("addr"):
        env["CUSTOM_NACOS_ADDR"] = str(n["addr"])
        env["CUSTOM_NACOS_USER"] = str(n.get("user", "nacos"))
        env["CUSTOM_NACOS_PASSWORD"] = str(n.get("password", ""))
        env["CUSTOM_NACOS_NAMESPACE"] = str(n.get("namespace", "houeoc"))
    if q.get("addr"):
        env["CUSTOM_ROCKETMQ_NAMESERVER"] = str(q["addr"])
    return env


def run_install(mode, extra_env, start_from=None):
    """后台线程：按序列跑 install.sh --stage。start_from 指定从失败阶段续跑。"""
    stages = STAGES_DOCKER if mode == "docker" else STAGES_CUSTOM
    if start_from and start_from in stages:
        stages = stages[stages.index(start_from):]

    gen = _thread_gen
    base_env = {**os.environ, "HOUCLOUD_INSTALL_MODE": mode}
    base_env.update(extra_env)

    try:
        for st in stages:
            if st != "preflight":
                write_state({"state": "installing", "mode": mode, "step": st,
                             "error": "", "stepTitle": STAGE_TITLES.get(st, st)})
            log(f"== 开始阶段 {st}（{STAGE_TITLES.get(st, st)}） ==")
            p = subprocess.Popen(
                ["bash", "deploy/install.sh", "--stage", st],
                cwd=str(PROJECT), env=base_env,
                stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)
            stage_log = []
            for line in p.stdout:
                line = line.rstrip()
                log(line)
                stage_log.append(line)
            rc = p.wait()

            if rc != 0 or f"##STAGE:{st}:ok" not in "\n".join(stage_log):
                err = "\n".join(stage_log[-15:])[-1200:]
                write_state({"state": "failed", "mode": mode, "step": st, "error": err})
                log(f"== 阶段 {st} 失败（exit {rc}）。可从本阶段重试，无需从头安装 ==")
                return

        # 全部阶段成功：组装完成信息（读 .env，不回显口令）
        write_state({"state": "installed", "mode": mode, "step": "done",
                     "error": "", "completedAt": datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                     "info": summary_info(mode)})
        log("== 安装完成 ==")
    except Exception as e:
        write_state({"state": "failed", "mode": mode, "error": f"agent 内部错误：{e}"})
        log(f"== agent 内部错误：{e} ==")
    finally:
        global _install_thread
        if gen == _thread_gen:
            _install_thread = None


def summary_info(mode):
    """完成页信息：Docker 模式展示各容器服务访问信息；custom 模式确认连接信息。"""
    project = env_get("COMPOSE_PROJECT_NAME", "unimall")
    web_port = env_get("WEB_PORT", "8080")
    info = {
        "project": project,
        "adminUrl": f"http://<你的域名或IP>:{web_port}/",
        "adminAccount": "admin（初始口令见 deploy/scripts/sql/init/99-seed.sql，首登必改）",
        "sandbox": env_get("HOU_PLUGIN_PAY_SANDBOX", ""),
        "payAdminTokenHint": "grep '^HOU_PLUGIN_PAY_ADMIN_TOKEN=' .env（服务器上执行）",
        "nacosPasswordHint": "grep '^NACOS_PASSWORD=' .env（服务器上执行）",
    }
    if mode == "docker":
        info["services"] = [
            {"name": "MySQL 8.0", "addr": f"127.0.0.1:{env_get('MYSQL_HOST_PORT', '3306')}",
             "note": f"库 {env_get('MYSQL_DATABASE', 'meoc')}；仅宿主机可连（口令见 .env）", "container": f"{project}-mysql"},
            {"name": "Redis 7.2", "addr": f"127.0.0.1:{env_get('REDIS_HOST_PORT', '6379')}",
             "note": "仅宿主机可连", "container": f"{project}-redis"},
            {"name": "Nacos 控制台", "addr": f"http://127.0.0.1:{env_get('NACOS_HOST_PORT', '8848')}/nacos",
             "note": "账号 nacos；口令见 .env 的 NACOS_PASSWORD", "container": f"{project}-nacos"},
            {"name": "RocketMQ", "addr": f"NameServer 127.0.0.1:{env_get('ROCKETMQ_NAMESRV_PORT', '9876')}",
             "note": "Broker 10911", "container": f"{project}-rocketmq-broker"},
            {"name": "BFF 网关", "addr": f"http://127.0.0.1:{env_get('BFF_PORT', '8391')}",
             "note": "统一 API 入口 /web/**", "container": f"{project}-bff-web"},
            {"name": "管理后台", "addr": f"http://<你的域名或IP>:{web_port}/",
             "note": "admin-web 容器（点击下方按钮后启动）", "container": f"{project}-admin-web"},
        ]
    else:
        info["services"] = [
            {"name": "外部 MySQL", "addr": f"{env_get('CUSTOM_MYSQL_HOST', '?')}:{env_get('CUSTOM_MYSQL_PORT', '3306')}",
             "note": f"库 {env_get('MYSQL_DATABASE', 'meoc')} · 账号 {env_get('CUSTOM_MYSQL_USER', 'root')}", "container": ""},
            {"name": "外部 Redis", "addr": env_get("REDIS_ADDR", "?"), "note": "", "container": ""},
            {"name": "外部 Nacos", "addr": env_get("NACOS_ADDR", "?"),
             "note": f"命名空间 {env_get('NACOS_NAMESPACE', 'houeoc')}", "container": ""},
            {"name": "BFF 网关", "addr": f"http://127.0.0.1:{env_get('BFF_PORT', '8391')}",
             "note": "host 网络，宿主机端口直通", "container": ""},
            {"name": "管理后台", "addr": f"http://<你的域名或IP>:{web_port}/",
             "note": "admin-web 容器（点击下方按钮后启动）", "container": f"{project}-admin-web"},
        ]
    return info


def mysql_tables():
    """Docker 模式安装中：报 initdb 进度（表数），供前端打点。"""
    project = env_get("COMPOSE_PROJECT_NAME", "unimall")
    env = {"MYSQL_PWD": env_get("MYSQL_PASSWORD", "")}
    code, out = sh(["docker", "exec", f"{project}-mysql", "mysql",
                    "-umeoc", "-N", "-B", "-e",
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()",
                    env_get("MYSQL_DATABASE", "meoc")], timeout=8, env=env)
    return out.strip() if code == 0 and out.strip().isdigit() else None


# ---------------------------------------------------------------- finish / reset
def finish_sequence():
    """停安装向导、端口交还 admin-web。先回 202 再执行，agent 最后自我退出。"""
    time.sleep(1.5)  # 让 202 先送达浏览器
    project = env_get("COMPOSE_PROJECT_NAME", "unimall")
    for args in (
        ["docker", "compose", "-f", "docker-compose.yml", "--profile", "install", "stop", "install-web"],
        ["docker", "compose", "-f", "docker-compose.yml", "up", "-d", "--no-deps", "admin-web"],
        # 等 admin-web 接住端口再撤 agent；失败也不回头（浏览器已拿到 202 与指引）
        ["docker", "inspect", "-f", "{{.State.Health.Status}}", f"{project}-admin-web"],
    ):
        try:
            sh(args, timeout=120)
        except Exception as e:
            log(f"finish 步骤异常：{e}")
    log("== 向导已退出：install-web 已停，admin-web 已接管 WEB_PORT ==")
    time.sleep(1)
    # agent 自退不能用 os.kill(getpid, SIGTERM)：本进程是容器 PID 1，内核对
    # 未注册 handler 的 PID 1 屏蔽默认信号动作（2026-10-28 实测进程不死）。
    # 正确姿势：docker compose stop 自己 —— 守护进程从外部发信号（10s 后
    # SIGKILL 兜底），且容器转入 stopped 状态后 restart: unless-stopped 不会再拉起。
    try:
        sh(["docker", "compose", "-f", "docker-compose.yml", "--profile", "install",
            "stop", "install-agent"], timeout=90)
    except Exception as e:  # 信号到达时本进程随时会被杀，异常无需处理
        print(f"self-stop: {e}")


def reset_sequence(confirm):
    """RESET=停服务保数据卷；PURGE=删数据卷 + 运行期目录 + 状态。"""
    action = "--down" if confirm == "RESET" else "--purge"
    # HOUCLOUD_KEEP_INSTALL_PROFILE=1：install.sh 的 down/purge 保留 install profile
    # 自身（向导页面还活着才能继续操作；纯命令行调用无此变量，语义不变）。
    env = {**os.environ, "HOUCLOUD_KEEP_INSTALL_PROFILE": "1"}
    p = subprocess.run(["bash", "deploy/install.sh", action],
                       cwd=str(PROJECT), input="PURGE\n",
                       capture_output=True, text=True, timeout=900, env=env)
    log(p.stdout[-1500:] if p.stdout else "")
    if p.returncode != 0:
        return False, (p.stderr or p.stdout or "")[-400:]
    try:
        STATE_FILE.unlink()
    except FileNotFoundError:
        pass
    return True, "已重置（可重新安装）" if confirm == "RESET" else "已清空全部数据（可重新安装）"


# ---------------------------------------------------------------- HTTP
class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"
    server_version = "HouCloudInstallAgent/1.0"

    # -- helpers -------------------------------------------------
    def _json(self, obj, code=200):
        body = json.dumps(obj, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _body(self):
        n = int(self.headers.get("Content-Length") or 0)
        if n <= 0:
            return {}
        try:
            return json.loads(self.rfile.read(n).decode() or "{}")
        except Exception:
            return {}

    def log_message(self, fmt, *args):  # 安装日志走业务日志，不刷 stderr
        pass

    # -- routes --------------------------------------------------
    def do_GET(self):
        path = self.path.split("?")[0]
        if path == "/install/api/status":
            st = read_state()
            self._json({"state": st.get("state", "none"),
                        "mode": st.get("mode", ""), "step": st.get("step", ""),
                        "error": st.get("error", ""), "info": st.get("info", {}),
                        "stepTitle": st.get("stepTitle", ""),
                        "agentBusy": _install_thread is not None})
        elif path == "/install/api/progress":
            st = read_state()
            self._json({"state": st.get("state", "none"), "mode": st.get("mode", ""),
                        "step": st.get("step", ""), "stepTitle": st.get("stepTitle", ""),
                        "error": st.get("error", ""), "logs": ring_tail(90),
                        "mysqlTables": mysql_tables() if st.get("state") == "installing" and st.get("mode") == "docker" else None})
        elif path == "/install/api/agent-ping":
            self._json({"ok": True, "version": "1.0"})
        else:
            self._json({"error": "not found"}, 404)

    def do_POST(self):
        global _install_thread, _thread_gen
        path = self.path.split("?")[0]
        body = self._body()
        st = read_state()

        if path == "/install/api/preflight":
            self._json(preflight_report())
            return

        if path == "/install/api/test-connection":
            self._json(test_connection(body.get("config", body)))
            return

        if path == "/install/api/start" or path == "/install/api/retry":
            with _install_lock:
                if st.get("state") == "installed":
                    self._json({"error": "已安装完成，拒绝重复安装。如需重装请先调用 reset"}, 409)
                    return
                if _install_thread is not None:
                    self._json({"error": "已有安装任务进行中"}, 409)
                    return
                mode = (body.get("mode") or st.get("mode") or "docker").lower()
                if mode not in ("docker", "custom"):
                    self._json({"error": "mode 必须是 docker 或 custom"}, 400)
                    return
                if mode == "custom" and path.endswith("/start"):
                    need = custom_env_from_config(body.get("config", {}))
                    if not need.get("CUSTOM_MYSQL_HOST"):
                        self._json({"error": "custom 模式必须填写 MySQL 连接信息"}, 400)
                        return
                else:
                    need = custom_env_from_config(body.get("config", {}))

                start_from = body.get("fromStep") if path.endswith("/retry") else None
                _thread_gen += 1
                _install_thread = threading.Thread(
                    target=run_install, args=(mode, need, start_from), daemon=True)
                _install_thread.start()
            self._json({"accepted": True, "mode": mode})
            return

        if path == "/install/api/reset":
            confirm = str(body.get("confirm", "")).upper()
            if confirm not in ("RESET", "PURGE"):
                self._json({"error": "confirm 必须是 RESET（停服务保数据）或 PURGE（删全部数据）"}, 400)
                return
            with _install_lock:
                if _install_thread is not None:
                    self._json({"error": "安装进行中不允许重置，请等它结束或重启容器"}, 409)
                    return
                okk, msg = reset_sequence(confirm)
            self._json({"ok": okk, "message": msg}, 200 if okk else 500)
            return

        if path == "/install/api/finish":
            if st.get("state") != "installed":
                self._json({"error": "尚未安装完成"}, 409)
                return
            threading.Thread(target=finish_sequence, daemon=True).start()
            self._json({"accepted": True,
                        "message": "正在停用安装向导并启动管理后台，约 30 秒后访问 http://<你的域名>/"}, 202)
            return

        self._json({"error": "not found"}, 404)


def main():
    log(f"install-agent 启动（project={PROJECT}，state={STATE_FILE}）")
    server = ThreadingHTTPServer(("0.0.0.0", PORT), Handler)
    server.serve_forever()


if __name__ == "__main__":
    main()
