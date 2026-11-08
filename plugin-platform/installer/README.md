# UniMall 独立服务安装代理

安装代理运行在客户部署服务器上，由 base 插件中心完成购买、维护期和站点授权后调用。当前实现交付的是单独的 Apache-2.0 源码归档与 `docker save` 镜像归档，通过 `docker image load` 导入镜像，再用签名清单中的不可变镜像 ID 启动 `unimall-<service>`。本实现没有接入私有 Registry 拉取或多副本滚动发布。

仅支持商业独立服务 `crm`、`cms`、`erp`、`finance`、`geo`、`im`、`logging`、`mes`、`oa`、`orderfood`、`pm`、`rider`、`supplier`、`webpos`。签名清单的 `pluginCode` 必须等于 `hostService`，`runtime` 必须是 `standalone`。

- 源码保存在 `sources/<service>/<version>/source.tar.gz`，仅供客户留存与二次开发，不解压到 `plugins/`，不挂载或加载到运行容器。
- 运行归档保存在 `runtime/<service>/<version>/image.tar`。下载逐字节校验签名清单的大小和 SHA-256；导入后还要核对 Docker image ID。
- 官方镜像必须定义有效 `HEALTHCHECK`。候选容器先禁用 Nacos 注册，通过健康检查后才切换正式容器。正式容器健康检查与部署方提供的注册/关键 API 检查全部通过，任务才进入 `SUCCESS`。
- 升级在候选容器启动、旧容器停止前执行备份/兼容性预检。切换失败时按旧容器不可变 ID 恢复原名和运行状态，并复查旧版本健康与注册；无法完整恢复时保留 `FAILED` 和人工恢复提示。
- 代理只操作标记为对应 `unimall.service` 的现有容器；清理新容器还必须匹配当前 `unimall.task`。旧容器、已取得的源码和镜像归档不会在成功后自动删除。
- 每个代理进程顺序执行任务。同服务、同版本和同摘要的执行中请求返回原任务；同服务的不同版本/摘要会被拒绝，其他服务拥有独立任务记录。完成后的重新提交会创建新任务。

## 部署配置

安装代理为独立 Maven 模块 `com.houjiang:unimall-installer:1.1.0`，需要 Java 21 和 Docker，构建另需 Maven。签名验证使用 JDK 内置 Ed25519，不依赖 Python 或 OpenSSL 可执行程序；继续使用现有 SPKI PEM 公钥。它不是业务服务，也不是 PF4J 插件。配置、环境变量文件、检查脚本和私钥/令牌由部署方管理；源码包不包含客户配置或安装代理令牌。每个站点只运行一个代理进程，绑定本机或受保护的内部接口。

```json
{
  "instanceId": "org-1",
  "catalogOrigin": "http://base.internal:8080",
  "bind": "127.0.0.1",
  "port": 8420,
  "tokenEnv": "UNIMALL_INSTALLER_TOKEN",
  "root": "/var/lib/unimall-installer",
  "publicKeys": {
    "release-2026": "/etc/unimall/release-2026.pub"
  },
  "services": {
    "erp": {
      "envFile": "/etc/unimall/erp.env",
      "network": "unimall",
      "healthTimeoutSeconds": 180,
      "registrationCheck": ["/usr/local/bin/check-erp-registration"],
      "upgradePreflight": ["/usr/local/bin/backup-and-preflight-erp"]
    }
  }
}
```

`catalogOrigin` 必须指向可访问 base 内部 `/base/plugin-center/artifact/raw` 接口的受信任 origin（协议、主机和可选端口），不能配置为公开浏览器 `/web/` 路径。交付票据只允许相对 `/base/plugin-center/artifact/raw?token=...` URL；代理拒绝外部绝对 URL 和下载重定向。跨主机时由部署方配置 TLS 或受保护的内部网络。

`registrationCheck` 与 `upgradePreflight` 均为必填的非空字符串参数数组，直接执行，不经 shell。请求不能传入脚本、SQL、环境文件或 Docker 参数。

- `registrationCheck` 执行时追加 `<service> <version>`；必须核对预期版本的 Nacos 注册、关键 API 与依赖服务，退出码 0 才表示通过。回滚恢复后也用旧版本调用它。
- `upgradePreflight` 仅在存在旧服务容器时执行，追加 `<targetVersion>`。必须完成数据库备份/快照、二开差异检查、版本化迁移评估，并确认候选镜像启动后数据库仍兼容旧镜像。不可逆或不兼容迁移必须返回非零并转入人工备份恢复/向前修复流程；代理不会自动执行数据库降级，也无法检查脚本是否真正完成了这些工作。
- 候选容器与正式容器使用同一环境文件、网络与数据依赖。禁用 Nacos 注册不能阻止候选容器内的定时任务或启动迁移；发布镜像和预检脚本必须保证候选启动安全。

设置至少 32 字符的令牌后启动：

```sh
java -jar plugin-platform/installer/target/unimall-installer.jar --config /etc/unimall/installer.json
```

## 任务接口

所有接口必须携带 `Authorization: Bearer <token>` 和与配置 `instanceId` 完全匹配的 `X-UniMall-Instance`，两者均须通过鉴权。`POST /tasks` 必须提供唯一且有效的 `Content-Length`，请求体最多 128 KiB（131072 字节），不接受 `Transfer-Encoding`。

| 接口 | 返回 |
|---|---|
| `POST /tasks` | 提交已授权交付 bundle，返回任务对象 |
| `GET /tasks/<uuid>` | 一个持久化任务对象 |
| `GET /tasks` | 最近 100 个任务对象 |

任务对象字段如下；POST 与 GET 使用相同格式：

| 字段 | 类型与含义 |
|---|---|
| `taskId` | UUID 字符串 |
| `pluginCode`, `version` | 已验证签名清单的产品与版本 |
| `imageId` | `sha256:` 前缀的 Docker image ID |
| `runtimeSha256` | Docker 镜像归档 SHA-256，64 位小写十六进制 |
| `sourceSha256` | 源码归档 SHA-256，64 位小写十六进制 |
| `state` | `PENDING`、`RUNNING`、`SUCCESS`、`FAILED` 或 `ROLLED_BACK` |
| `step` | 最近阶段，如 `QUEUED`、`DOWNLOAD`、`VERIFY`、`BACKUP`、`START`、`ACTIVATE`、`HEALTH`、`FAILED`、`INTERRUPTED` |
| `result` | 字符串；失败原因，或成功时包含容器与摘要的 JSON 字符串 |
| `created` | UTC Unix 秒时间戳 |

摘要字段在提交时从验证通过的签名清单持久化，失败任务也保留目标身份。只有 `SUCCESS` 表示下载摘要、导入镜像、健康与注册检查均已通过；`ROLLED_BACK` 表示目标版本失败、旧服务已恢复，不能据此登记目标版本安装成功。base 应核对任务归属以及上述五个身份字段后登记成功。

继续使用配置 `root` 下的 `tasks.sqlite`，兼容旧 Python 代理任务表；启动时自动补齐 `image_id`、`runtime_sha256`、`source_sha256` 列。旧记录的摘要为 `null`，不能当作已验证的安装回执。代理重启会把 `PENDING`、`RUNNING` 任务标记 `FAILED / INTERRUPTED`，不自动重新执行；部署方应先检查候选、正式和历史容器，再决定恢复或重试。

## 从旧 Python 代理迁移

1. 先停止旧代理进程，并关闭其自动重启入口，确认不再写入任务库。
2. 在旧进程停止后备份整个 `root`（至少完整备份 `tasks.sqlite` 及存在的 SQLite WAL/SHM 文件），保留源码、运行归档和现场容器状态。
3. 使用原配置、原 `root`、原令牌环境变量和公钥启动 Java 代理；不要删除任务库，也不要另建 root 冒充迁移。首次启动会执行上述补列及中断标记。
4. 通过任务查询和宿主现场核对迁移结果；中断任务须人工确认容器状态后再恢复或重试。

**旧 Python 与 Java 代理不可双开，即使监听不同端口也不能共用同一个 root。** 自动补列不代替备份，不保证中断部署已自动回滚；涉及数据库不兼容迁移仍须执行既有人工恢复流程。

## 构建与验证

在仓库根目录使用 Java 21 和 Maven 执行：

```sh
mvn -f plugin-platform/installer/pom.xml clean verify
```

产物为 `plugin-platform/installer/target/unimall-installer.jar`，包含运行依赖，可用上面的 `java -jar` 命令启动。模块独立构建，不要求先构建业务服务或 PF4J 插件。Java 测试应验证 JDK Ed25519、真实临时文件与 SQLite、摘要/签名、任务去重与隔离、旧表迁移、HTTP 鉴权及请求上限，并使用模拟 Docker/下载验证安装与回滚边界；具体覆盖以当前测试源码和 Maven 报告为准，不等于真实 Docker、数据库迁移或 Nacos 联调验收。
