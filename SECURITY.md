# 安全策略

## 支持版本

| 版本 | 支持状态 |
|---|---|
| master 分支最新 | ✅ 支持 |
| 历史快照 | ❌ 请升级到最新 |

## 报告漏洞

**请勿在公开 Issue、讨论区或 PR 中提交漏洞细节。**

请通过 GitHub 仓库的 **Security Advisories（Report a vulnerability）** 私密提交，内容包括：

- 问题类型（越权 / 注入 / 签名绕过 / 信息泄露等）
- 复现步骤与影响面（涉及的服务、接口路径、前置条件）
- 版本与部署形态（host 网络 / bridge 网络、是否启用制品强制验签）
- 如有 PoC 请附上（ privately ）

我们会在收到报告后尽快确认，修复将先合入 master，再发布安全公告。

## 安全基线（部署方必读）

- 装机脚本生成的随机口令请自行妥善保管；`.env` 不要提交进任何仓库
- 生产部署请开启制品强制验签：`HOU_PLUGIN_REQUIRE_SIGNATURE=true` 并注入 `HOU_ARTIFACT_SIGN_PUBLIC_KEYS`
- 插件制品校验：上传侧 Ed25519 签名 + 安装侧 SHA-256 复核，任何一侧失败都拒绝安装（fail-closed）
- `deploy/` 目录中的脚本；执行前请通读内容，尤其是涉及 Docker 与文件写入的部分
