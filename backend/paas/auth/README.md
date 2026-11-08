# auth 服务

职责：登录、认证、令牌、身份提供方接入。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/auth/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-auth/`，仅通过本服务 `custom-auth` profile 装配。
- 公开契约：`backend/contracts/auth-api/`。
- 服务发现名称：`auth`；约定端口：`8392`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl auth -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
