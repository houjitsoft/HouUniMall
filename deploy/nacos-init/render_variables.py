#!/usr/bin/env python3
"""在容器里生成 Nacos 配置变量表 variables.env。

设计目标：Docker 轨不需要人肉填 67 个变量——凡是与「本机中间件」有关的变量，
按 compose 服务名给出默认值；第三方业务凭据（支付密钥、短信 AK 等）保持留空，
需要用到时再自己在 variables.env 里补。

优先级（高 -> 低）：
  1. 容器同名环境变量（compose 里加 `environment: - MALL_DATASOURCE_PASSWORD=xxx` 即可覆盖）
  2. Docker 轨内置默认值（见 DEFAULTS / 规则）
  3. variables.example 里的原值（多数为空）

用法：
  python3 render_variables.py [variables.example 路径] [输出路径]
"""
import os
import pathlib
import secrets
import sys

HERE = pathlib.Path(__file__).parent
SRC = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path("/work/variables.example")
DST = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else pathlib.Path("/work/variables.env")

# ---------------------------------------------------------------- 环境变量
MYSQL_PASSWORD = os.environ.get("MYSQL_PASSWORD", "houcloud")
REDIS_PASSWORD = os.environ.get("REDIS_PASSWORD", "")
NACOS_PASSWORD = os.environ.get("NACOS_PASSWORD", "nacos")
ES_PASSWORD = os.environ.get("ES_PASSWORD", "")
WEB_PORT = os.environ.get("WEB_PORT", "8080")

MYSQL_HOST = os.environ.get("RENDER_MYSQL_HOST", "mysql")
MYSQL_PORT = os.environ.get("RENDER_MYSQL_PORT", "3306")
MYSQL_USER = os.environ.get("RENDER_MYSQL_USER", "meoc")
MYSQL_DB = os.environ.get("RENDER_MYSQL_DB", "meoc")
REDIS_ADDR = os.environ.get("RENDER_REDIS_ADDR", "redis:6379")
# redis.yaml 的 host/port 是两个字段，host 不能塞复合值（带端口的 host 会被 Lettuce
# 当主机名解析，2026-10-28 实锤 UnknownHostException）；从 REDIS_ADDR 拆出两段。
REDIS_HOST, _, _REDIS_PORT_TAIL = REDIS_ADDR.rpartition(":")
if not REDIS_HOST or not _REDIS_PORT_TAIL.isdigit():
    REDIS_HOST = REDIS_ADDR
    REDIS_PORT = "6379"
else:
    REDIS_PORT = _REDIS_PORT_TAIL
NACOS_SERVER_ADDR = os.environ.get("RENDER_NACOS_SERVER_ADDR", "nacos:8848")
ROCKETMQ_NAMESERVER = os.environ.get("RENDER_ROCKETMQ_NAMESERVER", "rocketmq-namesrv:9876")
ROCKETMQ_ACCESS_KEY = os.environ.get("RENDER_ROCKETMQ_ACCESS_KEY", "")
ROCKETMQ_SECRET_KEY = os.environ.get("RENDER_ROCKETMQ_SECRET_KEY", "")
BFF_ADDR = os.environ.get("RENDER_BFF_ADDR", "bff-web:8391")
ES_ADDR = os.environ.get("RENDER_ES_ADDR", "elasticsearch:9200")
SEATA_SERVER_ADDR = os.environ.get("RENDER_SEATA_SERVER_ADDR", "seata:8091")
THIRD_ADDR = os.environ.get("RENDER_THIRD_ADDR", "http://thirdparty:8194")

MYSQL_PARAMS = os.environ.get(
    "MYSQL_PARAMS",
    "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
    "&useSSL=false&allowPublicKeyRetrieval=true",
)

# ---------------------------------------------------------------- 沿用既有值
# 幂等：variables.env 由本脚本产出并持久保存，重跑 nacos-init 时先读上次的值，
# 让「每安装实例唯一」的标识（如 HOUCLOUD_PLATFORM_SITE_CODE）不会每次重装都换新码。
PREV = {}
if DST.exists():
    for _line in DST.read_text(encoding="utf-8").splitlines():
        _s = _line.strip()
        if _s and not _s.startswith("#") and "=" in _s:
            _k, _v = _s.split("=", 1)
            PREV[_k.strip()] = _v.strip()

# ---------------------------------------------------------------- 内置默认值
DEFAULTS = {
    # 基础中间件
    "MYSQL_HOST": MYSQL_HOST,
    "MYSQL_PORT": MYSQL_PORT,
    "MYSQL_USER": MYSQL_USER,
    "MYSQL_DB": MYSQL_DB,
    "MYSQL_PARAMS": MYSQL_PARAMS,
    "REDIS_ADDR": REDIS_ADDR,
    "REDIS_HOST": REDIS_HOST,
    "REDIS_PORT": REDIS_PORT,
    "REDIS_PASSWORD": REDIS_PASSWORD,
    "NACOS_SERVER_ADDR": NACOS_SERVER_ADDR,
    "ROCKETMQ_NAMESERVER": ROCKETMQ_NAMESERVER,
    "ROCKETMQ_ACCESS_KEY": ROCKETMQ_ACCESS_KEY,
    "ROCKETMQ_SECRET_KEY": ROCKETMQ_SECRET_KEY,
    "AUTH_JWT_SECRET": secrets.token_hex(24),
    # 服务间内部调用令牌：每次初始化随机生成，写进 mutual.yaml 后被所有服务共享。
    # 显式在 .env 里设 PLUGIN_INTERNAL_JOB_TOKEN 可覆盖（多环境统一时用）。
    "PLUGIN_INTERNAL_JOB_TOKEN": os.environ.get("PLUGIN_INTERNAL_JOB_TOKEN") or secrets.token_hex(24),
    # mall → merchant 平台财务接口的签名密钥：两侧（mall 签名 / merchant 校验）读
    # mutual.yaml 里同一个键，源码 fail-closed，缺了就起不来。每次安装随机生成，
    # 写进 mutual.yaml 后全服务共享同一把。
    "MALL_PLATFORM_FINANCE_SERVICE_SECRET": os.environ.get("MALL_PLATFORM_FINANCE_SERVICE_SECRET")
        or secrets.token_hex(24),
    # 平台协议回传（开源版默认接入官方平台）：站点用户签署协议后，站点后端自动把
    # 凭据回传到平台汇总，平台后台「协议同意记录 → 站点回传记录」页可查。
    # 不想接入：置 HOUCLOUD_PLATFORM_REPORT_ENABLED=false 或清空 URL 即可（静默跳过）。
    # site-code：环境变量 > 上次渲染值 > 首次随机生成（每安装实例唯一）。
    "HOUCLOUD_PLATFORM_REPORT_ENABLED": os.environ.get("HOUCLOUD_PLATFORM_REPORT_ENABLED") or "true",
    "HOUCLOUD_PLATFORM_URL": os.environ.get("HOUCLOUD_PLATFORM_URL") or "https://unimall.houjit.com",
    "HOUCLOUD_PLATFORM_TOKEN": os.environ.get("HOUCLOUD_PLATFORM_TOKEN")
        or "76d51cbee840f49f5155616170048054830dcbf263af5979f1cfcdd2ceb902bb",
    "HOUCLOUD_PLATFORM_SITE_CODE": (os.environ.get("HOUCLOUD_PLATFORM_SITE_CODE")
                                    or PREV.get("HOUCLOUD_PLATFORM_SITE_CODE")
                                    or ("unimall-site-" + secrets.token_hex(8))),
    "HOUCLOUD_PLATFORM_SITE_NAME": os.environ.get("HOUCLOUD_PLATFORM_SITE_NAME") or "",
    # 可选中间件
    "ES_ADDR": ES_ADDR,
    "ES_ELASTICSEARCH_PASSWORD": ES_PASSWORD,
    "SEATA_SERVER_ADDR": SEATA_SERVER_ADDR,
    "BFF_ADDR": BFF_ADDR,
    # 服务间调用地址
    "THIRD_ADDR": THIRD_ADDR,
    "BFF_FEIGN_PM_ADDR": "",
    "BFF_PM_ADDR": "",
    # 门户域名：容器外用 http://localhost:<WEB_PORT> 访问
    "MERCHANT_DOMAIN": f"http://localhost:{WEB_PORT}",
    # Nacos / Seata 口令
    "SEATA_REGISTRY_NACOS_PASSWORD": NACOS_PASSWORD,
    "SEATA_PASSWORD": MYSQL_PASSWORD,
    "SEATA_PASSWORD_2": MYSQL_PASSWORD,
}

# 规则：所有 *_DATASOURCE_PASSWORD 都是连 meoc 库的口令 -> 统一用 MYSQL_PASSWORD
def rule(key: str):
    if key.endswith("_DATASOURCE_PASSWORD"):
        return MYSQL_PASSWORD
    return None


def main():
    if not SRC.exists():
        print(f"!! 找不到变量模板 {SRC}")
        sys.exit(1)

    out_lines = [
        "# 由 deploy/nacos-init/render_variables.py 自动生成（Docker 轨）",
        "# 需要修改第三方凭据时，直接改本文件后重新执行：",
        "#   docker compose run --rm nacos-init",
        "# 注意：本文件已在 .gitignore 中，不会被提交。",
        "",
    ]
    filled = 0
    for line in SRC.read_text(encoding="utf-8").splitlines():
        s = line.strip()
        if not s or s.startswith("#") or "=" not in s:
            continue
        key = s.split("=", 1)[0].strip()
        # 1) 同名环境变量最高优先级
        val = os.environ.get(key)
        # 2) 持久卷里的上次值（含人工改过的第三方凭据、实例唯一标识 site-code）。
        #    必须排在默认值之前：否则每次重跑都会把人工填的 AK/密钥冲掉；
        #    也必须排在 variables.example 原值之前：模板里新加的键在 PREV 里没有，
        #    会自然走默认值，于是「新键补齐、旧值不动」两件事同时成立。
        if val is None or val == "":
            val = PREV.get(key)
        if val is None:
            val = DEFAULTS.get(key)
        if val is None:
            val = rule(key)
        if val is None:
            val = s.split("=", 1)[1].strip()
        else:
            filled += 1
        out_lines.append(f"{key}={val}")

    DST.write_text("\n".join(out_lines) + "\n", encoding="utf-8")
    print(f"==> 已生成 {DST}（自动填充 {filled} 个变量，其余保持留空）")


if __name__ == "__main__":
    main()
