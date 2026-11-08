/**
 * 插件制品签名模块（{@code plugin-signature}）。
 *
 * <p>职责：为插件制品提供「平台签发、客户端可验」的 Ed25519 签名能力，是分发链路防伪的最后一道防线。
 *
 * <p>依赖方向：本模块位于分发链路最底层，<b>只依赖 JDK 与 spring-context</b>，
 * 不依赖 plugin-artifact / plugin-market / commons，任何上层模块都可单向依赖它。
 *
 * <p>配置前缀统一为 {@code houcloud.artifact.sign.*}。
 *
 * <h2>密钥分发与轮换规范（运维必读）</h2>
 * <ol>
 *   <li><b>kid 命名</b>：{@code k{yyyy}}，如 {@code k2026}。同一年只维护一对，
 *       制品元信息里记 kid，验签时按 kid 取公钥。</li>
 *   <li><b>生成</b>：{@code java -cp plugin-signature.jar com.houjiang.plugin.signature.Ed25519KeyGen &lt;输出目录&gt; k2026}
 *       生成私钥 {@code artifact-ed25519-k2026.key}（Base64 PKCS#8）与
 *       公钥 {@code artifact-ed25519-k2026.pub}（Base64 X.509）。</li>
 *   <li><b>私钥落盘</b>：Windows 生产机 {@code D:/houcloud-secrets/artifact-ed25519-{kid}.key}，
 *       文件权限收紧为「仅运行账号可读」；<b>私钥绝不进代码库、绝不进 Nacos</b>。</li>
 *   <li><b>公钥配置</b>：Nacos {@code mall.yaml} 扁平串
 *       {@code houcloud.artifact.sign.public-keys: k2026=<base64>,k2027=<base64>}
 *       （写成 {@code kid=base64} 逗号分隔，而不是 YAML 嵌套 Map，便于直接放进 properties 配置）。</li>
 *   <li><b>生效密钥</b>：{@code houcloud.artifact.sign.active-kid=k2026}，新制品用它签名。</li>
 *   <li><b>轮换</b>：新增一对密钥 → 公钥表里加一个 kid → 把 {@code active-kid} 切到新 kid。
 *       <b>老制品的 kid 不变，所以老制品仍能继续验签</b>，不需要重签；
 *       只有需要「让某批老制品失效」时才走 {@code resign} 接口（M2）。</li>
 *   <li><b>私钥缺失不启动失败</b>：私钥是懒加载的，未配置只是「不能签名」，
 *       不会让已在线上跑的 mall 起不来。需要关掉签名时把
 *       {@code houcloud.artifact.sign.enabled} 配成 false。</li>
 * </ol>
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
package com.houjiang.plugin.signature;
