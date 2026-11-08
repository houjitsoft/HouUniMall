# logging 服务

职责：操作日志、审计日志、日志查询与采集接入。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/logging/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-logging/`，仅通过本服务 `custom-logging` profile 装配。
- 公开契约：`backend/contracts/logging-api/`。
- 服务发现名称：`logging`；约定端口：`8099`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl logging -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
