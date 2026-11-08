package com.houjiang.plugin.signature;

/**
 * 插件制品签名器。
 *
 * <p>制品签名是「防伪」的最后一道防线，与「下载链接签名（HMAC-SHA256，短时效、防盗链）」
 * 职责不同：制品签名随制品长期存在，客户端安装前必须校验，用于证明该 jar 确实由本平台签发、
 * 且没有被替换内容或张冠李戴（把 A 版本的签名贴到 B 版本上）。
 *
 * <p>签名的输入不是 jar 字节本身，而是 {@link ArtifactSignaturePayload} 生成的规范化串，
 * 其中同时绑定了 sha256 / pluginCode / version / md5 / size 五个维度。
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
public interface ArtifactSigner {

    /**
     * 使用指定 kid 对应的私钥对 payload 签名。
     *
     * @param kid     密钥 ID，用于密钥轮换（同时写入制品元信息，验签时按 kid 取公钥）
     * @param payload 规范化签名串（见 {@link ArtifactSignaturePayload}）的 UTF-8 字节
     * @return Base64（标准编码）签名值
     * @throws ArtifactSignatureException kid 无对应私钥、私钥文件缺失或签名算法不可用时抛出
     */
    String sign(String kid, byte[] payload);

    /**
     * 使用指定 kid 对应的公钥验签。
     *
     * @param kid           密钥 ID
     * @param payload       规范化签名串的 UTF-8 字节，必须与签名时完全一致
     * @param signatureB64  Base64 签名值
     * @return true 表示验签通过；kid 未知、签名格式非法、内容被篡改均返回 false（不抛异常，
     *         便于上层统一映射为错误码 3011 SIGNATURE_INVALID）
     */
    boolean verify(String kid, byte[] payload, String signatureB64);
}
