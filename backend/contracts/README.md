# 服务公开契约

每个业务服务使用 `<service>-api/` 作为公开 DTO、事件和扩展点目录；现存 `plugin-api/` 保持兼容。跨服务只能依赖公开契约或 API，禁止通过该目录转移 Controller、Mapper 和业务实现。

仅有说明的预留目录暂不生成空 Maven 模块；实际迁入契约后再增加 pom.xml 和聚合引用。
