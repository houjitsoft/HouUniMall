# 第三方组件声明（THIRD-PARTY-NOTICES）

本文件列出本项目**随仓分发（distribute）**的第三方代码及其归属与许可声明，
用于满足 Apache License 2.0 第 4 条及各上游许可证的署名要求。

> 判定原则：**只有在开源仓中实际分发的代码才受许可证约束**。
> Maven / npm 的依赖声明本身不构成分发；`resource/`、`nativeplugins/`、`uni_modules/`
> 下随仓提交的第三方源码或二进制构成分发，必须列明。

完整逐依赖判定见
[public/docs/mall/compliance/依赖许可证审计报告.md](public/docs/mall/compliance/依赖许可证审计报告.md)。

---

## 一、随仓分发的第三方源码

### 1. 人人开源（renren-fast）衍生代码 — ⚠️ 需确认上游许可证

原版权头：`Copyright (c) 2016-2019 人人开源 All rights reserved.`（原作者 Mark / chenshun，`sunlightcs@gmail.com`）

| 文件 |
|------|
| `backend/paas/commons/src/main/java/com/houjiang/commons/util/Constant.java` |
| `backend/paas/commons/src/main/java/com/houjiang/commons/util/querypage/PageUtils.java` |
| `backend/paas/commons/src/main/java/com/houjiang/commons/util/querypage/Query.java` |
| `backend/paas/commons/src/main/java/com/houjiang/commons/xss/SQLFilter.java` |

**状态**：🔴 **待核实**。人人开源（renren-fast）上游公开仓库以 **Apache-2.0** 发布，
与本项目许可证**推定兼容**；但原文件头写的是 "All rights reserved"，属模板惯性写法，
与该仓库 LICENSE 不一致。

**2026-09-23 注释统一处置**：为统一全仓注释风格，上述 4 个文件内的 renren 版权头、
`https://www.renren.io` 链接与原作者邮箱标签已按项目风格统一改写
（版权声明统一为「厚匠科技版权所有」、作者署名统一为 `Amos`），
上游原始版权声明与作者信息**完整保留于本节**，作为 Apache-2.0 §4(b)/(c) NOTICE 层面的署名依据。
逐条改写明细原见 `docs/compliance/外部来源注释清单与改写报告-2026-09-23.md`（该报告已随 2026-10-16 文档清理移除，本节保留结论）。

**上线前动作**：
1. 核实上游 renren-fast 仓库当前 LICENSE，确认确为 Apache-2.0；
2. 上游署名以本节为准（文件内注释已统一为项目风格，本节为权威保留处）；
3. 若上游为 Apache-2.0，则合规；若为其它许可，需按上游要求处置。

### 2. fkgou（www.fkgou.com）来源代码 — 🔴 高风险，上线前必须处置

版权头：`Copyright (C), 2014-2018, :www.fkgou.com`

| 文件 | 用途 |
|------|------|
| `.../commons/util/secverifyapiutil/MessageMD5Util.java` | MD5 工具 |
| `.../commons/util/secverifyapiutil/VerifyConfig.java` | 验证配置 |
| `.../commons/util/Url.java` | URL 工具 |
| `.../commons/config/HouConfig.java` | 配置 |
| `backend/paas/thirdparty/.../service/impl/SendSmsServiceImpl.java` | 短信发送实现 |

**状态**：🔴 **未发现任何开源许可声明**。fkgou 为第三方商业站点，
其代码若无明确授权，**不得随 Apache-2.0 开源仓分发**——这会构成对原权利人的侵权敞口，
且本项目的 Apache-2.0 授权无法合法覆盖这些文件。

**处置方案（任选其一，推荐 A）**：
- **A（推荐）移除并重写**：`MessageMD5Util`（MD5）、`Url`、`VerifyConfig`、`HouConfig`
  均为通用工具类，可用 JDK 标准库或 Hutool 等价实现重写，工作量小；
  `SendSmsServiceImpl` 需按自有接口重新实现短信发送逻辑。
- **B 取得书面授权**：联系 fkgou 取得书面授权，并保留其版权与许可声明。
- **C 隔离为可选依赖**：移出主仓，改为文档说明「使用方自行引入」。

### 3. 快递鸟（kdniao）电子面单接口封装 — ⚠️ 待确认授权

原文件头：`@copyright: 深圳市快金数据技术服务有限公司`、`@技术QQ群: 340378554`、
`@see: http://kdniao.com/api-eorder`

| 文件 | 用途 |
|------|------|
| `backend/paas/commons/src/main/java/com/houjiang/commons/util/KdGoldAPIUtils.java` | 快递鸟电子面单 API 封装 |

**状态**：🔴 **未发现开源许可声明**。该文件为快递鸟官方示例代码的改写封装，
其版权归深圳市快金数据技术服务有限公司所有。若官方示例未附带开源许可，
随 Apache-2.0 仓分发存在侵权敞口。

**2026-09-24 注释统一处置**：文件内的外部版权归属行与外部社群联系方式已按项目风格统一
（版权声明统一为「厚匠科技版权所有」、移除外部 QQ 群号），
上游原始版权信息**保留于本节**作为署名依据；接口文档链接 `@see: http://kdniao.com/api-eorder`
属必要说明，保留未动。

**处置方案（推荐 A）**：
- **A（推荐）**：核实快递鸟官方示例的授权条款；若无可分发许可，按 API 文档自行重写封装并去除衍生痕迹。
- **B**：保留调用关系但将该文件移出主仓，改为文档说明「使用方自行引入」。

### 4. 其他随仓第三方

| 组件 | 许可证 | 位置 | 状态 |
|------|--------|------|------|
| uview-plus（vendored） | MIT | `frontend/*/src/uni_modules/uview-plus` | ✅ 已含 LICENSE |
| uni-ui / uni_modules 组件 | Apache-2.0 + DCloud 附加条款 | 各前端 `uni_modules/` | ✅ 保留其版权头 |
| DCloud uni-app 框架 | Apache-2.0 主体 | npm 依赖 + 模板 | ✅ 保留 DCloud 声明 |

---

## 二、仅依赖、不构成分发的组件（仅供参考）

| 组件 | 许可证 | 备注 |
|------|--------|------|
| MySQL Connector/J 8.0.33 | GPLv2 + Universal FOSS Exception | 运行时驱动；FOSS Exception 明确允许 |
| Elasticsearch Java Client 7.9.0 | Apache-2.0 | 仅客户端；ES 服务端 7.11+ 为 SSPL，由使用方自备 |
| 阿里云 OSS / 腾讯云 COS / SMS SDK | Apache-2.0 | 官方 SDK |
| 网易云信 nim-web-sdk-ng | ISC | npm 依赖；生产 IM 服务需自行申请账号 |
| Spring Boot / Spring Cloud / MyBatis-Plus 等 | Apache-2.0 | Maven 依赖 |

---

## 三、已知不在仓内（历史风险已消除）

审计报告早期识别的 4 个阻断项，在 unimall 抽取时**未纳入本仓**，
因此当前不构成分发风险（代码引用除外，见风险清单）：

- TinyMCE（GPLv2+ 或商业授权）
- 商米 SUNMI-Fruit 插件（第三方商业 SDK）
- 称重 uniplugin_weight（第三方商业 SDK）
- 极光 JG-JCore / JG-JPush（第三方商业 SDK）

上述组件的**代码引用**（如 `sunmiFruit.js`、`JPushSender.java`）仍保留在仓内，
处置方式应为「缺失时降级 / 使用方自行引入」，不得随仓分发其二进制。

---

## 四、维护说明

完整清单应在 CI 接入许可检查后自动生成并回填（见审计报告 P4 阶段）：

- 后端：`license-maven-plugin` → `THIRD-PARTY.txt`
- 前端：`license-checker --production --summary`

出现 `GPL | AGPL | SSPL | EUPL | CC-BY-NC/SA` 或 `UNKNOWN` 时 CI 应直接失败。
