# job 服务

职责：调度定义、执行触发、重试与运行记录。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/job/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-job/`，仅通过本服务 `custom-job` profile 装配。
- 公开契约：`backend/contracts/job-api/`。
- 服务发现名称：`job`；约定端口：`8313`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl job -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
