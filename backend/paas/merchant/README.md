# merchant 服务

职责：商户、店铺、入驻、店铺配置与商户业务关系。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/merchant/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-merchant/`，仅通过本服务 `custom-merchant` profile 装配。
- 公开契约：`backend/contracts/merchant-api/`。
- 服务发现名称：`merchant`；约定端口：`8210`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl merchant -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
