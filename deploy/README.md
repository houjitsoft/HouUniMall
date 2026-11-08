# 部署与安装

本目录是「拿到代码就能装起来」的唯一入口，部署相关的一切都在这里：
安装脚本、镜像材料（Dockerfile）、初始化资产（MySQL 种子、Nacos 模板）、部署与验收工具。
编排真相始终是仓库根的 `docker-compose.yml`，本目录不复制第二份 YAML。

## 目录结构

```
deploy/
├── install.sh               一键装机入口（本页第二节的主角）
├── verify-plugin-chain.sh   插件全链路验收（12 步接口自动化）
├── Dockerfile.builder       后端编译镜像（一次性编译全部 jar，各服务镜像从它取 jar）
├── Dockerfile.service       Java 服务通用镜像
├── Dockerfile.frontend      管理后台镜像（镜像内构建）
├── Dockerfile.nacos-init    Nacos 初始化镜像（一次性任务）
├── build-jars.sh            jar 编译编排（含架构门禁调用）
├── entrypoint-service.sh    Java 服务容器入口
├── mysql-init/              MySQL 首启初始化（00-init.sql 由 render 脚本生成）
├── nacos-init/              Nacos 首启初始化（模板渲染 + 39 dataId 导入）
├── nginx/                   admin-web 的 Nginx 配置
├── rocketmq/                broker 双轨配置（容器/宿主两种 brokerIP1）
└── scripts/
    ├── docker.sh            Docker 双轨入口（up/demo/full/mw/build/reset 等子命令）
    ├── sql/                 SQL 种子源（order.txt → render → mysql-init）
    ├── nacos-templates/     Nacos 配置模板（渲染后导入）
    ├── architecture/        架构门禁（services.json 校验，构建期强制）
    └── verify-extensions / new-extension / upgrade-diff-report  二开辅助工具
```

## 〇、编排文件怎么选

| 文件 | 用途 |
|---|---|
| `docker-compose.yml` | 唯一权威编排，本页所有命令都走它 |
| `docker-compose.hostnet.yml` | 中间件已在宿主机时**叠加**它（host 网络），必须配 `--no-deps` |
| `docker-compose.middleware.yml` | 只起中间件（MySQL / Redis / Nacos / RocketMQ）的独立网络形态，服务 jar 跑在宿主机 |
| `docker-compose.commercial.yml` | 商业服务制品镜像版（购买镜像后用，与 `--profile commerce` 二选一） |

模式 A（全栈）/ 模式 B（外部中间件）的完整对照、编排校验方法、四层启动验证与
故障排查（Nacos 鉴权伪装成 Redis 配置缺失等高迷惑性案例），见
[public/docs/docker/操作手册.md](../public/docs/docker/操作手册.md)。

> 开源包含 14 个业务服务模块 —— `auth` `permission` `userms` `base` `goods` `order`
> `payment` `mall` `merchant` `logging` `job` `thirdparty` `tool` `aiagent` —— 外加
> `commons` 公共库与 `bff-web` 网关。`cms` / `crm` / `erp` / `finance` / `geo` / `im` /
> `mes` / `oa` / `orderfood` / `pm` / `rider` / `supplier` / `webpos` 等商业域**未开源**，
> `compose up` 到它们会在 build 阶段失败，属预期。

## 一、前置条件

| 项 | 要求 | 说明 |
|---|---|---|
| 操作系统 | 任意 Linux（内核 4.x+） | Debian 12 / Ubuntu 22.04 / Rocky 9 已验证 |
| Docker | Engine 24+，含 `docker compose` v2 | `curl -fsSL https://get.docker.com \| sh` |
| 内存 | 最小集 ≥ 8GB，全量 24 服务 ≥ 28GB | 每个 JVM 默认 `SERVICE_MEM_LIMIT=1200m` |
| CPU | 最小集 ≥ 4 核 | Maven 构建阶段吃 CPU |
| 磁盘 | ≥ 30GB 可用 | 镜像 + MySQL 数据 + Nacos 数据 |
| 网络 | 能拉 Docker Hub 与 Maven Central | 内网可配 `BUILDER_IMAGE` 指向私有仓库 |

## 二、安装（最小可用集）

最小集 = 6 个中间件 + 5 个应用服务。包含登录、权限、用户、插件中心、插件市场、
插件授权、插件制品分发、插件发布审核，**但不含**商品/订单/支付等业务服务。

### 方式一：界面化安装向导（推荐）

```bash
bash deploy/install.sh --wizard
```

向导会起两个安装期容器（`install-web` + `install-agent`，install profile），浏览器访问
`http://<本机IP>:$WEB_PORT/` 自动跳转 `/install/`：

1. **环境检测**：Docker/compose 版本、内存/CPU/磁盘、端口冲突（致命项红标，不通过不能继续）
2. **选择模式**：Docker 默认安装（自动创建 MySQL/Redis/Nacos/RocketMQ 容器并全自动初始化）
   或 自定义安装（填写外部 MySQL/Redis/Nacos 连接信息，逐项「测试连接」通过后部署）
3. **安装进度**：阶段条 + 实时日志 + 数据库建表数；失败可**从失败阶段重试**（各阶段幂等）
4. **完成页**：Docker 模式展示各容器服务的访问地址（口令留在服务器 `.env`，不回显）；
   点「启动管理后台」后向导自动退出、端口交还 admin-web

状态持久化 `data/install-state.json`（已安装/安装中/失败/未安装），已安装后向导锁死重装入口；
`RESET`（停服务保数据）/`PURGE`（删数据卷）两个全词确认码才可重置。

### 方式二：命令行

```bash
git clone <仓库地址> houcloud && cd houcloud
bash deploy/install.sh
```

脚本会依次做完这 7 件事，任一步失败立即退出并给出下一步该看什么：

1. 校验 docker / compose / 内存 / 端口占用
2. 生成 `.env`：从 `.env.docker.example` 复制，并把全部 `change-me` 替换为随机口令
   （已存在 `.env` 时不覆盖，只在值仍是 `change-me*` 时替换）
3. 起中间件：`mysql` `redis` `nacos` `rocketmq-namesrv` `rocketmq-broker`，逐个等 healthy
4. 等 `nacos-init` 一次性任务跑完（它把 39 个 dataId 导入 Nacos，是 base 能否拿到配置的前提）
5. 构建并启动应用：`permission` `auth` `userms` `goods` `order` `payment` `base` `mall` `bff-web`
   （`mall` 是商城域 8 个插件的宿主，必须起，否则插件中心里那批插件装载不上）
6. 投放并登记插件：`plugin-seed` 把镜像里的插件 jar 铺到宿主插件卷，`plugin-register`
   逐个执行各插件的 `deploy/register.sql`（插件中心条目 / 安装记录 / 菜单 / 市场 / 角色授权，全幂等）。
   宿主启动前必须跑完 —— 插件引擎是启动时一次性扫描插件目录，晚了只能重启补
7. 接口验收：不是看容器状态，而是打 `bff-web` 的 HTTP 端口并请求插件中心接口

首次执行约 40–70 分钟：Maven 构建 29 个模块约 15 分钟，8 个服务镜像约 10 分钟，
**MySQL 建 700+ 张表是最慢的单步**（机械盘/网络盘上可能到 40 分钟以上；编排里已关 binlog
并把 redo 改成每秒刷，见 `docker-compose.yml` 的 mysql `command`）。

### 带管理后台前端

```bash
bash deploy/install.sh --with-admin
```

多起一个 `admin-web`（Nginx 托管前端产物），访问 `http://<IP>:8391/admin`。

### 起完整业务（含商品/订单/支付/商业插件）

```bash
docker compose up -d --profile commerce
```

## 二点五、协议回传（默认开启，可关）

站点用户在注册 / 版本更新补签时签署的协议凭据，会由站点后端自动回传到 Unimall 官方平台（`https://unimall.houjit.com`），平台运营方在后台「系统设置 → 协议同意记录 → 站点回传记录」页审计。**这是默认行为，装完即生效，不需要任何操作。**

- 配置项：`variables.env` 的 `HOUCLOUD_PLATFORM_*` 五个变量（`nacos-init` 首次渲染时自动填官方地址/令牌，并为本安装实例生成唯一 `HOUCLOUD_PLATFORM_SITE_CODE`；重跑 `nacos-init` 沿用同一码，不会漂移）。
- 改站点显示名：把 `HOUCLOUD_PLATFORM_SITE_NAME` 设成你的站点名（如 `张三的商城`），重跑 `docker compose run --rm nacos-init` 并重启 bff-web。
- **关闭回传**：`HOUCLOUD_PLATFORM_REPORT_ENABLED=false`（或清空 `HOUCLOUD_PLATFORM_URL`），重跑 nacos-init 后重启 bff-web。
- 回传失败不影响业务：后端异步静默跳过，只记日志。
- **老安装激活回传**（装于本功能合入之前的站点）：确认 bff-web 镜像里有回传类（`docker compose exec bff-web sh -c 'unzip -l /app/app.jar | grep -c PlatformConsentReporter'`，输出 ≥1 才具备能力，旧包需升级）；然后在 Nacos 控制台的 `bff-web.yaml` 末尾追加：

```yaml
houcloud:
  platform:
    report-enabled: true
    url: https://unimall.houjit.com
    site-code: unimall-site-<自定义唯一码>
    site-name: <站点名>
    token: 76d51cbee840f49f5155616170048054830dcbf263af5979f1cfcdd2ceb902bb
```

 保存后重启 bff-web 容器即可；验证见下节第 6 步。

## 二点六、数据库初始化（SQL 在哪、怎么导、能否手动导）

**建库 SQL 全部随仓库分发，不需要任何外部下载**：`deploy/scripts/sql/init/`（含 `plugins/` 子目录，共 48 个文件，2.7MB 明文 SQL——建库、全量建表、菜单/角色/管理员种子、插件表）。

执行顺序的**唯一权威清单**是同目录的 `order.txt`（`[schema]` 建表段 → `[data]` 种子段）。两条执行链都读它，禁止各自维护顺序：

| 轨道 | 谁执行 | 何时执行 |
|---|---|---|
| Docker 默认安装 | `render-docker-init.sh` 把清单渲染成 `deploy/mysql-init/00-init.sql`，MySQL 容器首次启动时由 `/docker-entrypoint-initdb.d` 自动执行 | 仅空库首启 |
| 自定义安装（外部 MySQL） | install.sh 调 `deploy/scripts/sql/init_all.sh` 按 `order.txt` 逐个执行 | 安装阶段 |

**想自己审一遍 SQL 或手动导入**——完全可以，都是普通明文文件，clone 下来就能看：

```bash
# 按 order.txt 顺序全自动执行（install.sh 内部就是这么调的）
bash deploy/scripts/sql/init_all.sh

# 或手动逐个执行（顺序照抄 order.txt）
mysql -h<host> -uroot -p < deploy/scripts/sql/init/00-create-database.sql
mysql -h<host> -uroot -p <随 00 号建库脚本指定的库名> < deploy/scripts/sql/init/10-meoc-schema.sql
# ……其余按 order.txt 依次
```

改 SQL 顺序/新增脚本 = 改 `order.txt` + 重跑 `bash deploy/scripts/sql/render-docker-init.sh` + 连同生成物一起提交（`--check` 可校验清单与生成物没漂移）。

**注意**：init 链只在**空库首次安装**时执行一次；对已初始化的库重跑整条链会因表已存在报错（部分脚本带 `information_schema` 幂等守卫，但不承诺全部）。已装好的库做版本升级时，增量 SQL 以该版本的发布说明为准，**不要重跑整条 init 链**。

## 三、中间件已在宿主机时

如果 MySQL / Redis / Nacos 已经装在宿主机上（不走容器），叠 host 网络编排：

```bash
cp .env.docker.example .env
# 关键：把地址改成宿主机端口，而不是容器服务名
sed -i 's/^NACOS_ADDR=.*/NACOS_ADDR=127.0.0.1:8848/' .env
docker compose -f docker-compose.yml -f docker-compose.hostnet.yml up -d --no-deps base bff-web
```

`--no-deps` 是必须的：不加会去拉容器版中间件，与宿主机端口撞车。

## 四、装完怎么验

容器 `Up` 不等于装好了。按下面顺序验，每步失败都能定位到具体环节。

```bash
# 1. 库表齐不齐（应有 700+ 张，含 hou_market_plugin_* 与 hou_plugin_*）
docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e \
  "select count(*) from information_schema.tables where table_schema=\"meoc\";"'

# 2. 菜单种子在不在（应 1300+ 条，含「插件中心/插件发布/发布审核/开发者/结算」）
docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e \
  "select count(*) from meoc.hou_permission_menu;"'

# 3. Nacos 配置导入完没（应 39 个 dataId）
curl -s "http://127.0.0.1:8848/nacos/v1/cs/configs?search=accurate&dataId=&group=&pageNo=1&pageSize=1&tenant=houeoc" | head -c 200

# 4. BFF 通不通
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8391/web/base/index/index

# 5. 插件中心接口（未登录返回 401/403 属正常，说明路由和鉴权都在）
curl -s http://127.0.0.1:8391/web/plugin/plugin-center/list | head -c 300

# 6. 协议回传配置在不在（应输出 true 和你的唯一 site-code，如 unimall-site-xxxx）
grep -E "HOUCLOUD_PLATFORM_(REPORT_ENABLED|SITE_CODE)=" variables.env
```

### 协议回传怎么算通

用户在站点注册 / 补签协议勾选同意后，去 **Unimall 官方平台**后台（unimall.houjit.com）「系统设置 → 协议同意记录 → 站点回传记录」页，能看到本站点（site-code）对应的签署记录，即为端到端打通。回传是异步的，最迟十几秒内可达。

### 插件全链路验收

登录管理后台后按这条线走一遍，五步都通才算「插件中心可用」：

| 步骤 | 入口 | 预期 |
|---|---|---|
| 1 创建插件 | 平台 → 插件发布 → 上传 jar | 生成草稿版本，返回校验结果 |
| 2 提交审核 | 同一页面「提交审核」 | 状态变「待审核」 |
| 3 审核通过 | 平台 → 发布审核 | 状态变「已上架」 |
| 4 购买 | 平台 → 插件市场 → 详情 → 结算 | 生成订单，支付后自动发授权 |
| 5 下载并安装 | 插件中心 → 安装 | 换票下载制品 → 验签 → 落库 → 装载 |

不想点页面的话，`deploy/verify-plugin-chain.sh` 会按这五步把接口全打一遍
（上传 → 审核 → 下单 → 支付 → 发证 → 换票 → 回源比对 sha256 → 安装）：

```bash
BFF=http://127.0.0.1:8391 bash deploy/verify-plugin-chain.sh
```

第 5 步失败时按这个顺序查，对应关系是固定的：

```
制品 404          → architecture/services.json 里缺该插件条目（BFF 路由靠它）
下载 401/403      → 授权不是 VALID 或来自沙箱（沙箱授权不得下载生产制品）
下载 410          → 下载票据过期或超次数，重新换票
安装 423          → 安装回执/授权记录没写成功，查 hou_service_installation 与 hou_market_plugin_license
签名校验失败       → 缺 houcloud.artifact.sign.public-keys；install.sh 已自动生成，
                    手工换密钥用 deploy/scripts/plugin/gen-sign-keys.sh
下单被拒          → HOU_PLUGIN_PAY_ENABLED=false（默认关），见下一节
```

### 买不了插件：支付默认是关的

`HOU_PLUGIN_PAY_ENABLED` 默认 `false`（代码里是 fail-closed），空白机装完只能逛市场、下不了单。
`deploy/install.sh` 会把单机试用装成**沙箱模式**（`SANDBOX=true` + `CHANNEL=MOCK`），
下单后调一次模拟付款即可拿授权：

```bash
# 1) 下单（先登录拿 JWT，请求要带 tenant-id: 1 和 Authorization）
curl -s -X POST http://127.0.0.1:8391/web/base/plugin-center/market/order/create \
  -H 'Content-Type: application/json' -H 'tenant-id: 1' -H "Authorization: $JWT" \
  -d '{"pluginCode":"geo"}'
# 2) 模拟付款（仅沙箱生效）
curl -s -X POST http://127.0.0.1:8391/web/base/plugin-center/market/pay/sandbox/confirm \
  -H 'Content-Type: application/json' -H 'tenant-id: 1' -H "Authorization: $JWT" \
  -d '{"orderNo":"<上一步返回的 orderNo>","result":"SUCCESS"}'
```

🔴 **正式收费前必须**：`.env` 改 `HOU_PLUGIN_PAY_SANDBOX=false`，配好
`HOU_PLUGIN_PAY_CASHIER_API_KEY`（收银台回调验签密钥）与 `HOU_BASE_PUBLIC_BASE_URL`
（拼回调地址），再 `docker compose up -d --force-recreate base`。
沙箱开着等于「任何登录用户都能把订单刷成已支付并领走授权」。

改支付配置要带管理口令（`/market/pay/admin/**` 的准入凭据）：

```bash
grep '^HOU_PLUGIN_PAY_ADMIN_TOKEN=' .env      # install.sh 已生成
curl -s -X POST http://127.0.0.1:8391/web/base/plugin-center/market/pay/admin/view \
  -H 'Content-Type: application/json' -H 'tenant-id: 1' -H "Authorization: $JWT" \
  -H "X-Pay-Admin-Token: $TOKEN" -d '{}'
```

## 五、口令与密钥

- 全部口令在 `.env`，权限 `600`，脚本不会把它们打到终端
- Nacos 要求 `NACOS_AUTH_TOKEN` 是 **base64 编码且解码后 ≥32 字节**，脚本已按此生成
- 插件签名密钥 `deploy/install.sh` 首次运行会自动生成到 `data/secrets/`：
  - `artifact-ed25519-k2026.key` 私钥（`600`），只读挂进 base 供上传时签名
  - `.env` 里自动写 `HOU_ARTIFACT_SIGN_PUBLIC_KEYS=k2026=<base64>` 供各实例验签
  - 生产环境应改在签名机上签名、本机只留公钥：私钥留在应用服务器上，等于把
    「以官方名义发布任意插件」的能力放进了容器
  - 要换密钥：`bash deploy/scripts/plugin/gen-sign-keys.sh ./data/secrets k2027`，
    再把新的 `kid=<base64>` 写进 `.env` 的 `HOU_ARTIFACT_SIGN_PUBLIC_KEYS`
- 运行期数据都在 `data/` 下（`data/plugins` 已装插件、`data/artifacts` 制品本体、
  `data/secrets` 密钥）。这些文件由容器以 root 写入，普通用户删不掉，
  `bash deploy/install.sh --purge` 会借 root 容器清

## 六、排错

| 现象 | 原因 | 处理 |
|---|---|---|
| `nacos-init` 失败 | Nacos 口令与 `.env` 不一致 | 看 `docker compose logs nacos-init`，脚本会把服务端口令改成 `.env` 值 |
| base 启动即退 | 库表不全（SQL 初始化没跑） | `docker compose logs mysql \| grep -i error`，确认 `deploy/mysql-init/00-init.sql` 被 source |
| base 报 20010 | 请求缺 `tenant-id` | BFF 需要 `tenant-id: 1` 头，前端已带；手工调接口要自己加 |
| 插件 404 Unknown plugin | `architecture/services.json` 缺条目 | 新增插件必须在清单里登记，否则 BFF 找不到归属服务 |
| 端口被占 | 本机已有同类服务 | 改 `.env` 的 `*_HOST_PORT`，或改用外部中间件模式 |

日志一律先看 BFF 再看具体服务：请求从 `bff-web` 进，路由错了在 BFF 就会断。
