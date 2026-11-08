# Hello World：standalone 插件

位置为 `plugins/goods/hello-world-standalone/`，遵循 `plugins/{hostService}/{code}/`。`runtime: standalone` 表示该插件能力由已有 goods 服务承载，不是独立 PF4J jar，也不是新建一个 Spring Boot 服务。

本目录只放插件身份清单。standalone 的业务实现由宿主服务管理；本例控制器位于 `backend/paas/goods/src/main/java/com/houjiang/goods/plugin/standalone/HelloWorldStandaloneController.java`，显式提供 `POST /plugin/hello-world-standalone/hello`，只返回问候文本，不读写业务数据。

## 构建与测试

在仓库根目录运行：

```sh
mvn -f backend/paas/pom.xml -pl goods -am -DskipTests package
mvn -f backend/paas/goods/pom.xml -Dtest=HelloWorldStandaloneControllerTest test
```

本例 `plugin.yaml` 不会被打成插件 jar。standalone 代码与 goods 服务一起编译、部署；插件清单注册、市场发布和生产授权是独立流程，不由本例自动执行。

## 调用

在本地 goods 服务可访问且具备自己的认证上下文时：

```sh
curl -X POST 'http://127.0.0.1:8208/plugin/hello-world-standalone/hello' \
  -H 'Content-Type: application/json' \
  -d '{}'
```

预期返回：

```json
{"message":"Hello World","mode":"standalone"}
```

此 endpoint 仅用于展示路由与服务承载方式；它不代表已接入正式插件市场、权限菜单或生产环境。与 embedded 示例的区别见 [`../hello-world/README.md`](../hello-world/README.md)。
