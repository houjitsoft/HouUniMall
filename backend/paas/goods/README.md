# goods 服务

职责：商品、SKU、分类、销售库存与库存预占。

- 官方后端：`src/main/java/`、`src/main/resources/`。
- 插件：仓库根目录 `plugins/goods/<pluginCode>/`。
- 站点二开：`backend/extensions/custom-goods/`，仅通过本服务 `custom-goods` profile 装配。
- 公开契约：`backend/contracts/goods-api/`。
- 服务发现名称：`goods`；约定端口：`8208`。

从仓库根目录构建：`mvn -f backend/paas/pom.xml -pl goods -am package`。
源码迁移不代表部署切换完成；以根目录《系统架构.md》的验收记录为准。
