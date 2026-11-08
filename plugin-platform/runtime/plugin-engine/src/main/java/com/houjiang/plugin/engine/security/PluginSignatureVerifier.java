package com.houjiang.plugin.engine.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 插件包签名校验（方案 6.1 / License 协议）。
 *
 * <p>私钥由市场/签发方持有，公钥内置于宿主（配置项 {@code houcloud.plugin.signature.public-key}
 * 或多公钥 {@code houcloud.plugin.signature.trusted-keys}）。安装与升级的 VERIFY 步调用本类，
 * 证明「安装包来源可信」——这与 {@code PluginChecksumVerifier} 的「传输一致」是两个层次：
 * 摘要防损坏，签名防伪造。
 *
 * <p><b>fail-closed 口径</b>：以下任一情况一律返回 {@code false}，绝不因为「没法验」就放行：
 * <ul>
 *   <li>公钥缺失或无法解析</li>
 *   <li>签名文件缺失或内容为空</li>
 *   <li>签名与内容不匹配、算法不受支持</li>
 *   <li>安装包不存在 / 不可读 / 读取中断</li>
 * </ul>
 * 历史版本恒返回 {@code true}，导致「配了验签开关但没有任何拦截力」，本轮重写。
 *
 * <p><b>签名文件约定</b>：默认与安装包同名、追加 {@value #SIGNATURE_SUFFIX} 后缀
 * （{@code erp-1.0.0.jar} → {@code erp-1.0.0.jar.sig}），内容为 Base64 编码的签名字节，
 * 允许 PEM 包裹（{@code -----BEGIN SIGNATURE-----}），允许空白与换行。
 *
 * <p><b>流式摘要</b>：按 8KB 缓冲分块更新，不把整个 jar 读进内存——制品上限默认 64MB，
 * 全量读入会让堆占用随并发安装线性抬升。
 *
 * @author HouCloud
 */
public class PluginSignatureVerifier {

    /** 默认签名算法。JDK 标准名称，与 {@link Signature#getInstance(String)} 一致。 */
    public static final String DEFAULT_ALGORITHM = "SHA256withRSA";

    /** 签名文件后缀。 */
    public static final String SIGNATURE_SUFFIX = ".sig";

    private static final int BUFFER_SIZE = 8192;

    private static final String PEM_PUBLIC_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PEM_PUBLIC_FOOTER = "-----END PUBLIC KEY-----";
    private static final String PEM_SIGNATURE_HEADER = "-----BEGIN SIGNATURE-----";
    private static final String PEM_SIGNATURE_FOOTER = "-----END SIGNATURE-----";

    private final String algorithm;

    /** 无参构造：使用默认算法，公钥由调用方每次传入。 */
    public PluginSignatureVerifier() {
        this(DEFAULT_ALGORITHM);
    }

    /**
     * @param algorithm JDK 标准签名算法名（如 {@code SHA256withRSA}、{@code SHA256withECDSA}）；
     *                  空值回落到 {@link #DEFAULT_ALGORITHM}
     */
    public PluginSignatureVerifier(String algorithm) {
        this.algorithm = (algorithm == null || algorithm.isBlank())
                ? DEFAULT_ALGORITHM
                : algorithm.trim();
    }

    /** @return 当前使用的签名算法名 */
    public String algorithm() {
        return algorithm;
    }

    /**
     * 校验安装包签名（签名文件按默认约定定位）。
     *
     * @param jar       安装包路径，必须存在且可读
     * @param publicKey 可信公钥（PEM 文本或 Base64 的 X.509 编码），为空直接失败
     * @return 验签通过返回 {@code true}；任何异常/缺失一律 {@code false}
     */
    public boolean verify(Path jar, String publicKey) {
        return verify(jar, publicKey, null);
    }

    /**
     * 校验安装包签名。
     *
     * @param jar           安装包路径，必须存在且可读
     * @param publicKey     可信公钥（PEM 文本或 Base64 的 X.509 编码），为空直接失败
     * @param signatureFile 签名文件路径；为 {@code null} 时按 {@value #SIGNATURE_SUFFIX} 约定推导
     * @return 验签通过返回 {@code true}；任何异常/缺失一律 {@code false}
     */
    public boolean verify(Path jar, String publicKey, Path signatureFile) {
        if (jar == null) {
            return false;
        }
        if (publicKey == null || publicKey.isBlank()) {
            return false;
        }
        try {
            PublicKey key = loadPublicKey(publicKey);
            Path sigPath = signatureFile != null ? signatureFile : defaultSignaturePath(jar);
            byte[] signature = readSignature(sigPath);
            return verify(jar, key, signature);
        } catch (RuntimeException | IOException | GeneralSecurityException e) {
            return false;
        }
    }

    /**
     * 校验安装包签名（公钥与签名字节已解析）。
     *
     * @throws IllegalArgumentException 入参非法
     * @throws IOException              安装包不可读或读取中断
     * @throws GeneralSecurityException 算法不可用
     */
    public boolean verify(Path jar, PublicKey publicKey, byte[] signature)
            throws IOException, GeneralSecurityException {
        if (jar == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("可信公钥不能为空");
        }
        if (signature == null || signature.length == 0) {
            throw new IllegalArgumentException("签名字节不能为空");
        }
        if (!Files.isRegularFile(jar)) {
            throw new IllegalArgumentException("安装包不存在或不是普通文件：" + jar);
        }
        Signature verifier = Signature.getInstance(algorithm);
        verifier.initVerify(publicKey);
        try (InputStream in = Files.newInputStream(jar)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                verifier.update(buffer, 0, read);
            }
        }
        return verifier.verify(signature);
    }

    /**
     * 校验字节内容签名（内存中制品，如已下载但尚未落盘）。
     */
    public boolean verify(byte[] content, PublicKey publicKey, byte[] signature)
            throws GeneralSecurityException {
        if (content == null) {
            throw new IllegalArgumentException("安装包内容不能为空");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("可信公钥不能为空");
        }
        if (signature == null || signature.length == 0) {
            throw new IllegalArgumentException("签名字节不能为空");
        }
        Signature verifier = Signature.getInstance(algorithm);
        verifier.initVerify(publicKey);
        verifier.update(content);
        return verifier.verify(signature);
    }

    /**
     * 解析公钥：同时接受 PEM 包裹文本与裸 Base64（X.509 {@code SubjectPublicKeyInfo}）。
     *
     * @throws IllegalArgumentException 文本为空、格式非法或算法不支持
     */
    public static PublicKey loadPublicKey(String pemOrBase64) {
        if (pemOrBase64 == null || pemOrBase64.isBlank()) {
            throw new IllegalArgumentException("公钥内容不能为空");
        }
        String body = pemOrBase64
                .replace(PEM_PUBLIC_HEADER, "")
                .replace(PEM_PUBLIC_FOOTER, "")
                .replaceAll("\\s", "");
        if (body.isEmpty()) {
            throw new IllegalArgumentException("公钥内容为空（仅有 PEM 头尾）");
        }
        byte[] encoded;
        try {
            encoded = Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("公钥不是合法 Base64 文本", e);
        }
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException rsaFailed) {
            // RSA 失败再试 EC：签发方可能用 ECDSA，不能因为猜错算法就误判为「公钥非法」
            try {
                return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
            } catch (GeneralSecurityException ecFailed) {
                throw new IllegalArgumentException("公钥解析失败（RSA/EC 均不匹配）", ecFailed);
            }
        }
    }

    /**
     * 解析私钥（PEM 或裸 Base64 的 PKCS#8 编码），供签发侧与测试使用。
     */
    public static PrivateKey loadPrivateKey(String pemOrBase64) {
        if (pemOrBase64 == null || pemOrBase64.isBlank()) {
            throw new IllegalArgumentException("私钥内容不能为空");
        }
        String body = pemOrBase64
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded;
        try {
            encoded = Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("私钥不是合法 Base64 文本", e);
        }
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("私钥解析失败（仅支持 PKCS#8 未加密私钥）", e);
        }
    }

    /**
     * 读取签名文件内容，支持 PEM 包裹与裸 Base64。
     *
     * @throws IOException              文件不存在或不可读
     * @throws IllegalArgumentException 内容为空或不是合法 Base64
     */
    public static byte[] readSignature(Path signatureFile) throws IOException {
        if (signatureFile == null) {
            throw new IllegalArgumentException("签名文件路径不能为空");
        }
        if (!Files.isRegularFile(signatureFile)) {
            throw new IllegalArgumentException("签名文件不存在：" + signatureFile);
        }
        String text = Files.readString(signatureFile);
        String body = text
                .replace(PEM_SIGNATURE_HEADER, "")
                .replace(PEM_SIGNATURE_FOOTER, "")
                .replaceAll("\\s", "");
        if (body.isEmpty()) {
            throw new IllegalArgumentException("签名文件内容为空：" + signatureFile);
        }
        try {
            return Base64.getDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("签名文件不是合法 Base64 文本：" + signatureFile, e);
        }
    }

    /** 默认签名文件路径：安装包文件名 + {@value #SIGNATURE_SUFFIX}。 */
    public static Path defaultSignaturePath(Path jar) {
        if (jar == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        return jar.resolveSibling(jar.getFileName().toString() + SIGNATURE_SUFFIX);
    }

    /**
     * 对安装包内容生成签名（签发侧工具，测试与运营上架时使用）。
     *
     * @return 签名字节
     */
    public byte[] sign(Path jar, PrivateKey privateKey) throws IOException, GeneralSecurityException {
        if (jar == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("签发私钥不能为空");
        }
        if (!Files.isRegularFile(jar)) {
            throw new IllegalArgumentException("安装包不存在或不是普通文件：" + jar);
        }
        Signature signer = Signature.getInstance(algorithm);
        signer.initSign(privateKey);
        try (InputStream in = Files.newInputStream(jar)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                signer.update(buffer, 0, read);
            }
        }
        return signer.sign();
    }

    /** 签名字节的 PEM 文本表示（便于落盘为 {@code *.sig} 文件）。 */
    public static String toPem(byte[] signature) {
        if (signature == null || signature.length == 0) {
            throw new IllegalArgumentException("签名字节不能为空");
        }
        return PEM_SIGNATURE_HEADER + "\n" + Base64.getEncoder().encodeToString(signature) + "\n"
                + PEM_SIGNATURE_FOOTER + "\n";
    }

    /**
     * 内容摘要（诊断用，便于把「验签失败」与「内容被改」区分开）。
     *
     * @return 小写十六进制 SHA-256
     */
    public static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest()).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JVM 不支持 SHA-256", e);
        }
    }
}
