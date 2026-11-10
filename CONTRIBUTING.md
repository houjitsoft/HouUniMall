# 贡献指南（CONTRIBUTING）

感谢你愿意为 HouUniMall 贡献力量。本文档说明**怎么提、提到哪、什么标准会被合并**。

## 0. 开始前必读

- 提交代码即表示你同意按本项目许可证（**Apache License 2.0**，见 [LICENSE](LICENSE)）分发你的贡献。本组织各开源仓统一采用 Apache-2.0。
- 本项目采用 **DCO（Developer Certificate of Origin）**，见 §4。
- 不要提交任何密钥、口令、证书、内网地址、真实客户数据。CI 会拦截，但请自查。

## 1. 你可以贡献什么

| 类型 | 说明 | 走哪个仓库 |
|------|------|-----------|
| Bug 修复 | 缺陷修复 + 最小复现用例 | 主仓 `HouUniMall` |
| 新功能 | 先发 Issue 讨论，避免与官方规划冲突 | 主仓 |
| 文档 | 文档站 `docs/`、注释、示例 | 主仓 |
| **插件** | 新插件（可作为官方示例或社区插件） | `mall-plugins-official` |
| **SDK 扩展点** | 新增扩展点接口、PluginContext 能力 | `mall-plugin-sdk` |
| 部署资产 | compose、Dockerfile、初始化脚本 | `mall-docker` |
| 二开示例 | `extensions/` 下的覆盖示例 | 主仓 |

## 2. 开发环境

| 组件 | 版本 |
|------|------|
| JDK | 21 |
| Maven | 3.8+ |
| Node.js | 20+（部分老终端需 16，见各应用 README） |
| MySQL | 8.0 |
| Redis | 6+ |
| Nacos | 2.3+ |

```bash
# 后端（以 mall 模块为例）
cd backend/paas
mvn -pl commons -am install -DskipTests     # commons 必须最先编译
mvn -pl mall -am install -DskipTests

# 前端（以 pc 为例）
cd frontend/mall/pc && npm install && npm run dev
```

## 3. 提交规范（Conventional Commits）

```
<type>(<scope>): <subject>
```

`type` 取值：`feat` / `fix` / `docs` / `refactor` / `perf` / `test` / `chore` / `plugin` / `ext`

示例：

```
fix(order): 修复售后单重复退款校验缺失
feat(mall): 插件中心支持按分类筛选
plugin(mall-points): 积分插件支持过期提醒
ext(order): 示例——覆盖运费计算
```

- `scope` 用模块名（mall / goods / order / payment / bff / pc / bcenter ...）
- 一个 PR 只做一件事；重构与功能改动分开提

## 4. DCO 签名（必需）

每次提交信息末尾附加：

```
Signed-off-by: 你的名字 <你的邮箱>
```

或直接用 `git commit -s`。含义是你声明：你有权提交这段代码，并同意按本项目许可证分发。

CI 会校验所有提交的 DCO；缺失时 PR 会失败，补签方式：

```bash
git rebase --signoff origin/main
git push --force-with-lease
```

## 5. 代码约定

### 后端

| 项 | 约定 |
|----|------|
| 官方代码包 | `com.houjiang.{module}.**` |
| **二开代码包** | `com.houjiang.custom.{module}.**`（**禁止**使用官方包名） |
| **插件代码包** | `com.houjiang.plugin.{code}.**` |
| 官方表前缀 | `hou_` |
| 二开新增表 | `hou_ext_` |
| 插件自有表 | `hou_plugin_{code}_` |
| 新增官方 Service | 显式暴露接口，供二开通过 `@ConditionalOnMissingBean` 覆盖 |
| 事务/幂等 | 涉及资金与库存的写操作必须有幂等键 |

### 前端

- Vue 3 组合式 API；新管理端统一 Element Plus，PC 商城统一 Nuxt UI + Tailwind
- 颜色/间距/字号使用 Design Token，禁止硬编码色值（见 PC 商城 UI 设计规范）
- 接口统一走 `src/api/**`，禁止在组件内直接写 axios

### 数据库

- 建表脚本同时提交到 `sql/` 与对应模块，字段名与实体保持一致
- 变更脚本必须可重复执行（`CREATE TABLE IF NOT EXISTS` / `INSERT IGNORE`）
- 禁止在 SQL 中提交任何真实业务数据

## 6. PR 流程

1. Fork → 建分支 `feat/xxx` 或 `fix/xxx`（从 `develop` 切出）
2. 提交（带 DCO 签名）→ 自测 → 推送
3. 提 PR 到 `develop`，填写 PR 模板，**关联 Issue**
4. CI 全绿（构建 / 单元测试 / 许可证 / gitleaks / DCO）→ 至少 1 名维护者 Review
5. 合并后由维护者统一 cherry-pick 到内部仓库（社区贡献不直接进内部主线）

### Review 关注点

- 是否引入破坏性变更？涉及公开接口/表结构必须有迁移说明
- 是否有硬编码配置、密钥、内网地址？
- 是否新增依赖？新增依赖必须说明许可证
- 是否需要同步更新文档与 CHANGELOG？

## 7. 测试要求

- Bug 修复：附最小复现步骤或单元测试
- 新功能：核心路径必须有单元测试
- 涉及接口的改动：附实际调用结果（`code` 与关键字段）
- 涉及页面的改动：附真实页面截图（**不得包含账号、Token、手机号等敏感信息**）

## 8. 不被接受的内容

- 包含真实密钥、证书、内网 IP、客户数据的提交
- GPL/AGPL 等与 Apache-2.0 不兼容的依赖
- 第三方商业 SDK（如商米、极光、云信 SDK），需改为「文档说明 + 使用方自行引入」
- 大规模格式化改动（会淹没 diff，请先开 Issue 讨论）
- 与 Roadmap 严重冲突且未提前沟通的大功能

## 9. 行为准则

参与本项目即视为同意 [行为准则](./CODE_OF_CONDUCT.md)。
