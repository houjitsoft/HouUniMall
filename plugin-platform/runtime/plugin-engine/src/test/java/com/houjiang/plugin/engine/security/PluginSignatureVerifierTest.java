package com.houjiang.plugin.engine.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 插件签名验签（fail-closed）。
 *
 * <p>历史上本组件恒返回 {@code true}，导致「配了验签开关但没有拦截力」。
 * 这组用例把「必须拦住」的场景钉死：签名缺失、被篡改、公钥缺失、算法不配。
 *
 * @author HouCloud
 */
class PluginSignatureVerifierTest {

    @TempDir
    Path tempDir;

    private KeyPair keyPair;
    private String publicKeyPem;
    private String privateKeyPem;
    private PluginSignatureVerifier verifier;

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";
        privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";
        verifier = new PluginSignatureVerifier();
    }

    @Test
    void 签名匹配且未被篡改时通过() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        writeSignature(jar, verifier.sign(jar, keyPair.getPrivate()));
        assertTrue(verifier.verify(jar, publicKeyPem));
    }

    @Test
    void 内容被篡改时拒绝() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        Path signatureFile = writeSignature(jar, verifier.sign(jar, keyPair.getPrivate()));
        Files.writeString(jar, "tampered bytes", StandardCharsets.UTF_8);
        assertFalse(verifier.verify(jar, publicKeyPem, signatureFile));
    }

    @Test
    void 缺少签名文件时拒绝() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        assertFalse(verifier.verify(jar, publicKeyPem));
    }

    @Test
    void 公钥缺失时拒绝而不是放行() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        writeSignature(jar, verifier.sign(jar, keyPair.getPrivate()));
        assertFalse(verifier.verify(jar, null));
        assertFalse(verifier.verify(jar, "  "));
    }

    @Test
    void 公钥被换成另一对的公钥时拒绝() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        writeSignature(jar, verifier.sign(jar, keyPair.getPrivate()));
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String otherPublic = Base64.getEncoder().encodeToString(generator.generateKeyPair().getPublic().getEncoded());
        assertFalse(verifier.verify(jar, otherPublic));
    }

    @Test
    void 签名文件不是合法Base64时拒绝() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        Files.writeString(jar.resolveSibling("erp-1.0.0.jar.sig"), "!!!not-base64!!!", StandardCharsets.UTF_8);
        assertFalse(verifier.verify(jar, publicKeyPem));
    }

    @Test
    void 安装包不存在时拒绝() {
        assertFalse(verifier.verify(tempDir.resolve("missing.jar"), publicKeyPem));
    }

    @Test
    void 裸Base64公钥与PEM公钥等价() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        writeSignature(jar, verifier.sign(jar, keyPair.getPrivate()));
        String bare = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        assertTrue(verifier.verify(jar, bare));
    }

    @Test
    void 公钥文本非法时抛参数异常() {
        assertThrows(IllegalArgumentException.class, () -> PluginSignatureVerifier.loadPublicKey("not-a-key"));
        assertThrows(IllegalArgumentException.class, () -> PluginSignatureVerifier.loadPublicKey(""));
        assertThrows(IllegalArgumentException.class, () -> PluginSignatureVerifier.loadPublicKey(null));
    }

    @Test
    void 私钥往返解析可用() {
        PrivateKey restored = PluginSignatureVerifier.loadPrivateKey(privateKeyPem);
        PublicKey publicKey = PluginSignatureVerifier.loadPublicKey(publicKeyPem);
        assertTrue(restored instanceof PrivateKey);
        assertTrue(publicKey instanceof PublicKey);
    }

    @Test
    void 不同算法签名与验签一致() throws Exception {
        PluginSignatureVerifier sha512 = new PluginSignatureVerifier("SHA512withRSA");
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        byte[] signature = sha512.sign(jar, keyPair.getPrivate());
        assertTrue(sha512.verify(jar, PluginSignatureVerifier.loadPublicKey(publicKeyPem), signature));
        // 换算法解读同一份签名必然失败——防止「算法被悄悄改了还能验过」
        assertFalse(verifier.verify(jar, PluginSignatureVerifier.loadPublicKey(publicKeyPem), signature));
    }

    @Test
    void 摘要工具可用于区分内容变化() throws Exception {
        Path jar = writeJar("erp-1.0.0.jar", "plugin bytes");
        String before = PluginSignatureVerifier.sha256(jar);
        Files.writeString(jar, "changed", StandardCharsets.UTF_8);
        assertNotEquals(before, PluginSignatureVerifier.sha256(jar));
    }

    @Test
    void 空算法回落到默认算法() {
        assertEqualsDefaultAlgorithm(new PluginSignatureVerifier(null));
        assertEqualsDefaultAlgorithm(new PluginSignatureVerifier("   "));
    }

    private static void assertEqualsDefaultAlgorithm(PluginSignatureVerifier verifier) {
        org.junit.jupiter.api.Assertions.assertEquals(PluginSignatureVerifier.DEFAULT_ALGORITHM, verifier.algorithm());
    }

    private Path writeJar(String name, String content) throws IOException {
        Path jar = tempDir.resolve(name);
        Files.writeString(jar, content, StandardCharsets.UTF_8);
        return jar;
    }

    private Path writeSignature(Path jar, byte[] signature) throws IOException {
        Path sig = PluginSignatureVerifier.defaultSignaturePath(jar);
        Files.writeString(sig, PluginSignatureVerifier.toPem(signature), StandardCharsets.UTF_8);
        return sig;
    }
}
