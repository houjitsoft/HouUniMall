# order 服务

职责：购物车、订单、履约编排、运费、售后、促销结算。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/order/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-order/`，仅通过本服务 `custom-order` profile 装配。
- 公开契约：`backend/contracts/order-api/`。
- 服务发现名称：`order`；约定端口：`8209`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl order -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
