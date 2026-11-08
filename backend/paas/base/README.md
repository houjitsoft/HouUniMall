# base 服务

职责：平台组织、字典、基础配置、插件目录与安装编排。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/base/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-base/`，仅通过本服务 `custom-base` profile 装配。
- 公开契约：`backend/contracts/base-api/`。
- 服务发现名称：`base`；约定端口：`8214`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl base -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
