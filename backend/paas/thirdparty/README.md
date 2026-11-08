# thirdparty 服务

职责：短信、对象存储、外部平台、AI 等第三方连接能力。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/thirdparty/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-thirdparty/`，仅通过本服务 `custom-thirdparty` profile 装配。
- 公开契约：`backend/contracts/thirdparty-api/`。
- 服务发现名称：`thirdparty`；约定端口：`8194`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl thirdparty -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
