# payment 服务

职责：收款、支付渠道、退款、支付回调和支付流水。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/payment/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-payment/`，仅通过本服务 `custom-payment` profile 装配。
- 公开契约：`backend/contracts/payment-api/`。
- 服务发现名称：`payment`；约定端口：`8207`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl payment -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
