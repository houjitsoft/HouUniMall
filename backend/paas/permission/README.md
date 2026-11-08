# permission 服务

职责：平台角色、菜单、权限与授权关系。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/permission/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-permission/`，仅通过本服务 `custom-permission` profile 装配。
- 公开契约：`backend/contracts/permission-api/`。
- 服务发现名称：`permission`；约定端口：`8300`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl permission -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
