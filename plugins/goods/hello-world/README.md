# Hello World：真实插件目录

位置为 `plugins/goods/hello-world/`，遵循 `plugins/{hostService}/{code}/`，宿主为 goods。此目录只包含通用示例，不含商业插件源码。

- `plugin.yaml`：code=hello-world、version=1.0.0、runtime=embedded、hostService=goods。
- `HelloPlugin`：PF4J 入口，POM 的 MANIFEST 指向它。
- `HelloWorldController`：POST `/hello` 返回 `{"message":"Hello World","mode":"plugin"}`。
- `HelloWorldExtension`：向商品扩展上下文写入 `hello-world.plugin=Hello World`；不修改价格、不写数据库。
- `src/test/`：校验问候返回值及原价格不变。

以下命令均在仓库根目录运行。先确保父 POM、SDK 和 API 已安装到自己的 Maven 本地仓库，再构建：

```sh
mvn -f backend/paas/pom.xml -pl goods -am -DskipTests install
mvn -f plugins/goods/hello-world/pom.xml clean test package
```

产物：`plugins/goods/hello-world/target/hello-world-1.0.0.jar`。包内应含 `plugin.yaml` 和 MANIFEST 的 `Plugin-Id`、`Plugin-Class`、`Plugin-Version`。SDK、API、PF4J 为 provided，不重复打进插件包。

开发环境的 goods 宿主需要开启 `houcloud.plugin.boot-enabled=true`，配置 `houcloud.plugin.plugins-dir`。经安装与验签流程落盘位置为 `{pluginsRoot}/hello-world/1.0.0/hello-world-1.0.0.jar`；开发直接放包同样受宿主配置、验签和授权约束，复制文件不等于已安装。

本示例 code 已登记在 `architecture/services.json`，BFF 将 `/web/plugin/hello-world/hello` 转给 goods。安装启用并分配 `hello-world:hello` 权限后，可用自己的 BFF 登录态与租户头调用：

```sh
curl -X POST 'http://127.0.0.1:8391/web/plugin/hello-world/hello' \
  -H "Authorization: Bearer $TOKEN" -H 'tenant-id: 1'
```

默认不会自动安装或分配权限；本例也不包含支付、市场上架、签名私钥和前端商品页面。单元测试/打包成功不表示完整客户站点安装验收已通过。后端二开对照见 [custom-goods](../../../backend/extensions/custom-goods/README.md)。
