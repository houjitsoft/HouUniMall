package com.houjiang.plugin.signature;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * Ed25519 密钥对生成工具（运维用，<b>不是 Spring Bean</b>）。
 *
 * <p>用途：初始化部署或密钥轮换时生成一对 Ed25519 密钥。私钥以 Base64(PKCS#8) 落盘，
 * 公钥以 Base64(X.509) 打印出来填进 {@code houcloud.artifact.sign.public-keys}。
 *
 * <p>使用方式：
 * <pre>
 *   # 仅打印
 *   java -cp plugin-signature.jar com.houjiang.plugin.signature.Ed25519KeyGen
 *
 *   # 打印并落盘私钥（目录必须已存在，私钥权限需自行收紧为 600）
 *   java -cp plugin-signature.jar com.houjiang.plugin.signature.Ed25519KeyGen /houcloud/secrets k2025
 * </pre>
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
public final class Ed25519KeyGen {

    /**
     * 私有构造，工具类不允许实例化。
     */
    private Ed25519KeyGen() {
    }

    /**
     * 生成一对 Ed25519 密钥。
     *
     * @return Ed25519 密钥对
     * @throws IllegalStateException 当前 JDK 不支持 Ed25519 时抛出（JDK 15 以下）
     */
    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
            // Ed25519 密钥长度固定（256 bit），SunEC 无需显式 initialize，强传 keysize 反而可能抛不支持
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("当前 JDK 不支持 Ed25519，需 JDK 15 及以上", e);
        }
    }

    /**
     * 入口。
     *
     * @param args 可选：args[0]=私钥输出目录，args[1]=kid（默认 k1）
     */
    public static void main(String[] args) throws Exception {
        String kid = args.length >= 2 ? args[1] : "k1";
        KeyPair keyPair = generate();
        String privateB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicB64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        if (args.length >= 1 && !args[0].isBlank()) {
            Path dir = Path.of(args[0]);
            Files.createDirectories(dir);
            Path privateFile = dir.resolve("artifact-ed25519-" + kid + ".key");
            Path publicFile = dir.resolve("artifact-ed25519-" + kid + ".pub");
            Files.writeString(privateFile, privateB64, StandardCharsets.UTF_8);
            Files.writeString(publicFile, publicB64, StandardCharsets.UTF_8);
            System.out.println("私钥文件: " + privateFile.toAbsolutePath());
            System.out.println("公钥文件: " + publicFile.toAbsolutePath());
        }

        System.out.println("---------- kid = " + kid + " ----------");
        if (args.length >= 1 && !args[0].isBlank()) {
            System.out.println("privateKeyPath: "
                    + Path.of(args[0]).resolve("artifact-ed25519-" + kid + ".key").toAbsolutePath());
        }
        System.out.println("private(base64): " + privateB64);
        System.out.println("public (base64): " + publicB64);
        System.out.println("----------------------------------------");
        System.out.println("配置片段：");
        System.out.println("houcloud.artifact.sign.public-keys: " + kid + "=" + publicB64);
    }
}
