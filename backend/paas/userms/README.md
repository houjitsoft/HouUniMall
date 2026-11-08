# userms 服务

职责：用户、会员、地址、收藏、积分与用户权益。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/userms/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-userms/`，仅通过本服务 `custom-userms` profile 装配。
- 公开契约：`backend/contracts/userms-api/`。
- 服务发现名称：`userms`；约定端口：`8195`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl userms -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
