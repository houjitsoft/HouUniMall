> 目录基线：`custom-<service>` 共 24 个模块，只能装入同名服务；当前权威架构见根目录 `系统架构.md`。文中的历史测试数量不代表本次迁移验收。

# 二开（extensions）开发指南

本目录存放**源码级二开模块**：在 Core 之上实现 Core 暴露的扩展点契约，**以源码编译进宿主进程**（与商业插件运行时安装是两种不同形态）。二开是站点级构建期定制；需要租户级动态开关时必须交付为签名运行期插件。

> 原则：**不改 Core 源码、不 fork 官方实现**。二开只通过**扩展点契约**这一条缝接入；CI 用契约测试门禁保证不破坏官方契约。

## 宿主装配（当前实现）

每个 `custom-*` 模块都提供一个 Spring `AutoConfiguration.imports`，由 `@Bean` 显式暴露 **Custom* 实现**。`Default*` 只用于契约测试对照，不注册为生产 Bean。Mall 只有在 `backend/paas/mall/pom.xml` 选择对应 profile（如 `custom-goods` 或 `custom-all`）时才依赖这些 JAR；默认官方构建不会加载二开。

宿主启动完成后，`plugin-engine` 的 `ClasspathExtensionRegistrar` 扫描这些 Bean 的 `@HouExtension`，把它们注册到与 PF4J 插件共用的 `ExtensionRegistry`。注册阶段生成排序后的不可变快照，业务请求只读快照。二开不进入 `jar/plugins`，不经过插件市场，也不通过 `@Primary`、同名 Bean 或 classpath 顺序覆盖官方实现。

运行门禁：

```bash
mvn -f backend/extensions/pom.xml clean install
# 架构门禁（跨平台等价，断言逐条对应、退出码一致）：
bash deploy/scripts/verify-extensions.sh                       # macOS / Linux
powershell -File deploy/scripts/verify-extensions.ps1          # Windows（PowerShell）
mvn -f backend/paas/pom.xml -pl mall -am test -P custom-all
```

> 跨平台（R4.4 **已落地**）：`deploy/scripts/verify-extensions.sh` 与 `deploy/scripts/verify-extensions.ps1` 断言逐条对应、退出码语义一致（有错误=1）。
> macOS 实测：`bash deploy/scripts/verify-extensions.sh` → `Build-time extension architecture checks passed (32 annotated source file(s)).`（EXIT=0）。

完整开发、升级、回滚和真实页面验收流程见 [`public/docs/二开/开发手册.md`](../../public/docs/二开/开发手册.md) 与 [`public/docs/二开/操作手册.md`](../../public/docs/二开/操作手册.md)。

**升级前差异报告（用户可见产物）**：

```bash
python deploy/scripts/upgrade-diff-report.py
python deploy/scripts/upgrade-diff-report.py --base origin/master --service goods --out D:/tmp/upgrade-diff.md
```

对照官方基线列出 `custom-<service>` / 前端 `src/extensions/<service>` / `plugins/<service>` 的改动，交给负责人确认后再升 Core。

## 二开与插件的关系

两者共用**同一套扩展点机制**，区别只在交付与装载形态：

| 维度 | 二开（extensions） | 商业插件（plugins） |
|---|---|---|
| 源码位置 | `backend/extensions/{module}/` | `plugins/{hostService}/{code}/` |
| 交付形态 | 源码，随宿主一起编译 | JAR 制品（`jar/plugins/{hostService}/{code}/{version}/`） |
| 装载时机 | 构建期进 classpath，随宿主启动 | 运行时安装/启停/卸载 |
| 接缝 | `plugin-api` 扩展点 + `@HouExtension` | `plugin-api` 扩展点 + `@HouExtension` |
| 归属标记 | `@HouExtension(owner = "custom")` | `plugin.yaml owner=official/thirdparty` |
| 可否独立售卖 | 否，属于交付给单个客户的定制 | 是 |

因为是同一套接缝，二开与插件共享能力模型；差别在交付和生命周期。二开默认面向站点级定制，不能用 Maven profile 做租户级动态开关。

## 目录结构

```
backend/extensions/
├── pom.xml                        # 聚合构建：统一编译全部二开模块并跑契约测试
├── README.md
├── custom-goods/                  # 商品域二开示例
│   ├── pom.xml
│   └── src/
│       ├── main/java/.../ext/goods/
│       │   ├── DefaultPriceCalcExtension.java     # 官方默认（对照组）
│       │   ├── CustomPriceCalcExtension.java      # 二开：满额打折
│       │   ├── DefaultStockChangeExtension.java   # 官方默认（对照组）
│       │   └── CustomStockChangeExtension.java    # 二开：出库后同步外部仓储
│       └── test/java/.../ext/goods/
│           ├── {Xxx}ExtensionContractTest.java    # 契约测试抽象基类（每个扩展点一个）
│           ├── Default{Xxx}ExtensionTest.java
│           └── Custom{Xxx}ExtensionTest.java
├── custom-order/                  # 订单域二开示例（同构）
│   └── ... OrderCreateBefore / OrderCreateAfter / FreightCalc
├── custom-payment/                # 支付域二开示例（同构）
│   └── ... PayCallback / PromotionPaySettle
├── custom-auth/                   # 认证域二开示例
│   └── ... LoginAfter
├── custom-userms/                   # 用户域二开示例
│   └── ... RedpacketQuery
├── custom-base/                   # 平台域二开示例
│   └── ... DashboardWidget
└── custom-permission/             # 权限域二开示例
    └── ... MenuContribution
```

七个模块按**业务域**划分，与 `backend/paas` 的模块划分对齐，便于定位该去哪个模块改。

**命名约定**：一个扩展点对应三组文件——官方默认实现 `Default{Xxx}Extension`、
二开实现 `Custom{Xxx}Extension`、契约测试基类 `{Xxx}ExtensionContractTest`。
`Default{Xxx}Extension` 只作为测试对照实现，不能在自动配置中声明为 Bean；官方宿主实现由 Core 自己提供。官方对照实现与二开实现**各继承契约基类一次**，跑同一组断言。

## 唯一的接入方式：实现扩展点契约

Core 在 `backend/contracts/plugin-api` 暴露扩展点。二开实现它并用 `@HouExtension` 声明：

```java
@HouExtension(point = PriceCalcExtension.class, code = "custom-goods", priority = 100, owner = "custom",
        mode = ExtensionMode.REPLACE,
        description = "满 200 打 9 折")
public class CustomPriceCalcExtension implements PriceCalcExtension {
    @Override
    public void execute(Context ctx) {
        if (ctx == null || ctx.finalPrice == null) {
            return;
        }
        if (ctx.finalPrice.compareTo(new BigDecimal("200")) >= 0) {
            ctx.finalPrice = ctx.finalPrice.multiply(new BigDecimal("0.9"));
        }
    }
}
```

**策略约定**：默认 `APPEND`，适合通知、审计和同步；确需接管官方实现时显式声明 `mode = ExtensionMode.REPLACE`。只有 `owner=custom` 可以替换，同一扩展点只能有一个 replacement；第三方插件默认只能追加。官方实现通常为 `priority = 0`，二开钩子取 `100`（数值越小越先执行）。同 priority 的两个 custom 实现仍在注册阶段直接失败。

### 绝对禁止的覆盖方式

以下做法已被《UniMall Core 目录架构最终版》§五 明令禁止，**本目录不允许出现**：

```text
@Primary 抢占官方 Bean        # 禁止
依赖 classpath 顺序覆盖        # 禁止
抢占 URL 路由 / Controller     # 禁止
依赖 Core 实现或运行时实现包   # 禁止：只依赖 plugin-api、plugin-sdk 和允许的基础库
```

> 历史包袱：早期方案曾用 `@Primary`、同名 Bean 和 `goods-api` 抢占官方实现，现已删除。当前接入只允许扩展点、稳定 code 和显式 APPEND/REPLACE。

## 扩展点覆盖情况

`plugin-api` 现有 **17 个**扩展点，其中 **16 个**已提供二开示例，1 个（`PromotionCalcExtension`）刻意不提供，理由见下方说明。

| 扩展点 | 语义 | 二开示例 |
|---|---|---|
| `goods/PriceCalcExtension` | 商品计价（写回最终价） | ✅ `custom-goods` |
| `goods/StockChangeExtension` | 库存变动后钩子（after，只读） | ✅ `custom-goods` |
| `goods/StockLockExtension` | 库存预占/释放前校验（before，可 abort） | ✅ `custom-goods` |
| `order/OrderCreateBeforeExtension` | 下单前校验（before，可 abort） | ✅ `custom-order` |
| `order/OrderCreateAfterExtension` | 下单后通知/同步（after，可补写 params） | ✅ `custom-order` |
| `order/FreightCalcExtension` | 运费计算（after，写回运费） | ✅ `custom-order` |
| `order/CartCalcExtension` | 购物车金额计算（after，写回小额） | ✅ `custom-order` |
| `order/CartValidateExtension` | 购物车校验（before，可 abort） | ✅ `custom-order` |
| `order/AfterSaleCreateExtension` | 售后单创建前校验（before，可 abort） | ✅ `custom-order` |
| `payment/PayCallbackExtension` | 支付回调后处理 | ✅ `custom-payment` |
| `payment/PromotionPaySettleExtension` | 促销支付落库（要求幂等） | ✅ `custom-payment` |
| `payment/RefundCreateExtension` | 退款单创建前校验（before，可 abort） | ✅ `custom-payment` |
| `auth/LoginAfterExtension` | 登录后处理（可写 attrs） | ✅ `custom-auth` |
| `user/RedpacketQueryExtension` | 红包查询（**只读，不得 abort**） | ✅ `custom-userms` |
| `mall/DashboardWidgetExtension` | 仪表盘挂件（贡献数据，需幂等） | ✅ `custom-base` |
| `permission/MenuContributionExtension` | 菜单贡献（贡献数据，需幂等） | ✅ `custom-permission` |
| `order/PromotionCalcExtension` | 促销算价 | ➖ 由活动插件提供，二开不直接实现（见下） |

> **为什么 `PromotionCalcExtension` 不提供二开示例**：促销算价由一个个活动插件（秒杀 / 限时折扣 / 满减 / 优惠券）各自实现。
> 二开若要加自定义优惠，应实现的是**业务侧自己的促销插件**，而不是在 `extensions/` 里再写一个算价实现——否则会与活动插件的优惠叠加计算，导致金额重复抵扣。
> 迁移期该扩展点由 order 的 `LegacyActivityPromotionCalcExtension` 接管（见《UniMall Core 目录架构最终版》§八）。

## 契约测试门禁（架构决策 D5）

每个扩展点在 `src/test` 提供**抽象基类**，断言该契约必须满足的不变量。按语义分两类：

- **只读类**（`before*` / 查询）：不篡改入参、空上下文不抛异常、幂等、`@HouExtension` 声明正确；
- **写回类**（`after*` 写回结果字段）：结果非负且不越界（如优惠不超过原价、运费非负）、不篡改官方写入的**只读**字段、幂等。

```java
abstract class PriceCalcExtensionContractTest {
    protected abstract PriceCalcExtension extension();

    @Test void 最终价非负() {
        var ctx = ctxOf("300");
        extension().execute(ctx);
        assertTrue(ctx.finalPrice.signum() >= 0);
    }
}
```

二开实现改坏契约时 CI 立即红灯——这就是「先契约、后实现」的护栏。

## 构建

二开模块由 `backend/extensions/pom.xml` 聚合，**不进入** `backend/paas` 主反应堆（避免影响 Core 构建）。聚合 pom 统一管理 Java 21 编译配置、slf4j 与 JUnit 依赖，子模块只需声明 `plugin-api` + `plugin-sdk`。

```bash
# 二开模块依赖 plugin-api / plugin-sdk，首次需先安装
cd d:/wwwroot/houcloud/plugin-platform
mvn -o -q -DskipTests -pl sdk/plugin-sdk,../backend/contracts/plugin-api install

# 一次构建并跑完所有二开模块的契约测试
cd d:/wwwroot/houcloud/backend/extensions
mvn -o -DforkCount=0 clean test

# 只跑单个模块
mvn -o -DforkCount=0 -pl custom-goods clean test
```

> 上面用 Windows 绝对路径示例；macOS/Linux 换成仓库根下的相对路径即可，例如
> `mvn -f backend/extensions/pom.xml -DforkCount=0 clean test`。

当前状态：7 个模块共 **245 条**契约测试全绿（2026-09-15 本地实测）—— `custom-auth` 16、`custom-goods` 45、`custom-order` 90、`custom-payment` 47、`custom-userms` 17、`custom-base` 15、`custom-permission` 15。

## 新增一个二开模块的步骤

1. 确认要改的行为**已有对应扩展点**；没有则先在 `plugin-api` 提议新增（不要绕过）。
2. 建 `backend/extensions/{module}/`，pom 继承 `houcloud-extensions`，依赖 `plugin-api` + `plugin-sdk`（均 `provided`）。
3. 同时在聚合 `pom.xml` 的 `<modules>` 中登记。
4. 写二开实现，默认 `APPEND`；确需完全接管时显式使用 `REPLACE`。官方默认实现只作契约测试对照，不放进自动配置。
5. 写契约测试抽象基类，并让官方对照实现和二开实现各通过一次。
6. 为二开类声明稳定小写 `code`，确认未使用 `@Primary`、未依赖 Core/运行时实现包、未改 Core 源码。

步骤 2–5 的骨架可用脚手架一次生成（跨平台等价，断言/产物一致）：

```bash
# macOS / Linux（R4.4 已落地）
bash deploy/scripts/new-extension.sh --module order --point CartCalc \
     --package com.houjiang.plugin.api.extension.order [--site acme]

# Windows
powershell -File deploy/scripts/new-extension.ps1 -Module order -Point CartCalc -Package com.houjiang.plugin.api.extension.order
```

## 交付描述与来源校验

每个 `custom-*` 模块必须携带 `src/main/resources/META-INF/houcloud-extension.properties`：

```properties
code=custom-goods
version=1.0.0
scope=site
hostVersion=1.x
contracts=plugin-api,plugin-sdk
```

它是站点级二开制品的来源描述，不是插件市场的商品定价，也不是租户授权开关。构建门禁会校验 `code`、语义化版本、`scope=site` 和稳定契约依赖；发布时应把它与宿主版本、启用 profile、JAR 摘要和 SBOM 一起归档。

运行期 PF4J 插件的 `plugin.yaml owner` 才是运行期归属来源。引擎不会接受运行期插件通过 `@HouExtension(owner="custom")` 伪装成构建期二开；需要租户级动态差异时，应交付为经过签名、授权和健康检查的运行期插件。

## 前端二开边界

平台管理后台的产品页面仍由 `frontend/admin` 构建；二开不得直接复制、覆盖宿主 `src/views` 或抢占官方路由。站点级前端定制应作为宿主构建输入单独登记并生成静态制品，租户级或可启停的页面必须实现插件 ESM 契约（`menus/routes/setup/teardown`），由插件中心按 `frontendUrl` 动态加载。两者都只能通过 BFF 的 `/web/plugin/{code}/**` 调用插件服务，不能让浏览器直连 PaaS 或插件服务地址。
