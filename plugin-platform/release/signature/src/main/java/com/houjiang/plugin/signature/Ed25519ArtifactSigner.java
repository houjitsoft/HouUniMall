package com.houjiang.plugin.signature;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 Ed25519 的制品签名器（JDK 21 原生实现，<b>不依赖 BouncyCastle</b>）。
 *
 * <p>设计要点：
 * <ol>
 *   <li>算法与密钥体系：Ed25519（RFC 8032），JDK 15+ 原生支持
 *       {@code KeyFactory.getInstance("Ed25519")} / {@code Signature.getInstance("Ed25519")}，
 *       JDK 21 与 JDK 23（当前构建 JDK）均可用，无需任何三方加密库。</li>
 *   <li>私钥懒加载：<b>私钥文件缺失时不允许让宿主启动失败</b>。当前 mall 已在生产运行，
 *       新增签名能力必须做到「配置未就绪 = 功能不可用，但不影响既有业务」，
 *       因此私钥在第一次 sign 时才加载，失败抛 {@link ArtifactSignatureException}。</li>
 *   <li>密钥轮换：所有方法带 kid（key id），制品元信息记录 kid，
 *       验签时按 kid 取公钥；新增一对密钥只需在配置里加一个 kid，老制品不受影响。</li>
 * </ol>
 *
 * <p>配置：
 * <pre>
 * houcloud:
 *   artifact:
 *     sign:
 *       enabled: true
 *       private-key-path: file:D:/houcloud-secrets/artifact-ed25519-{kid}.key   # 支持 {kid} 占位
 *       public-keys: k2025=&lt;base64&gt;,k2026=&lt;base64&gt;                              # kid=base64(X.509 公钥)
 * </pre>
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
public class Ed25519ArtifactSigner implements ArtifactSigner {

    /**
     * 签名算法名（JDK 原生）。
     */
    private static final String ALGORITHM = "Ed25519";

    /**
     * 私钥路径模板中的 kid 占位符。
     */
    private static final String KID_PLACEHOLDER = "{kid}";

    /**
     * Spring Resource 前缀，本类直接按文件处理，需剥离。
     */
    private static final String FILE_PREFIX = "file:";

    /**
     * 私钥文件路径模板，允许包含 {@code {kid}} 占位符。
     */
    private final String privateKeyPathTemplate;

    /**
     * kid -&gt; Base64(X.509 编码公钥)，不可变快照。
     */
    private final Map<String, String> publicKeys;

    /**
     * 已加载的私钥缓存（懒加载）。
     */
    private final Map<String, PrivateKey> privateKeyCache = new ConcurrentHashMap<>();

    /**
     * 已解析的公钥缓存（懒解析）。
     */
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();

    /**
     * 构造签名器。
     *
     * @param privateKeyPathTemplate 私钥文件路径模板，可含 {@code {kid}}；为空表示不支持签名（仅验签）
     * @param publicKeys             kid -&gt; Base64(X.509 公钥) 映射，为空表示不支持验签
     */
    public Ed25519ArtifactSigner(String privateKeyPathTemplate, Map<String, String> publicKeys) {
        this.privateKeyPathTemplate = privateKeyPathTemplate == null ? "" : privateKeyPathTemplate.trim();
        this.publicKeys = publicKeys == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(publicKeys));
    }

    @Override
    public String sign(String kid, byte[] payload) {
        Objects.requireNonNull(kid, "kid 不能为空");
        Objects.requireNonNull(payload, "待签名内容不能为空");
        PrivateKey privateKey = privateKeyCache.computeIfAbsent(kid, this::loadPrivateKey);
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(payload);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new ArtifactSignatureException("Ed25519 签名失败，kid=" + kid, e);
        }
    }

    @Override
    public boolean verify(String kid, byte[] payload, String signatureB64) {
        if (kid == null || payload == null || signatureB64 == null || signatureB64.isEmpty()) {
            return false;
        }
        PublicKey publicKey = resolvePublicKey(kid);
        if (publicKey == null) {
            // 未知 kid：按验签失败处理（上层映射错误码 3011 SIGNATURE_INVALID），不抛异常
            return false;
        }
        byte[] signatureBytes;
        try {
            // MimeDecoder：兼容运维拷贝公钥/签名时混入的换行与空格
            signatureBytes = Base64.getMimeDecoder().decode(signatureB64);
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(payload);
            return signature.verify(signatureBytes);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    /**
     * 判断指定 kid 是否配置了公钥（用于发布前自检，避免签出无法验签的制品）。
     *
     * @param kid 密钥 ID
     * @return true 表示该 kid 可验签
     */
    public boolean supportsVerify(String kid) {
        return kid != null && publicKeys.containsKey(kid);
    }

    /**
     * 返回已配置的全部 kid（按配置顺序）。
     *
     * @return 不可变 kid 集合
     */
    public java.util.Set<String> availableKids() {
        return publicKeys.keySet();
    }

    /**
     * 懒加载指定 kid 的私钥。
     *
     * @param kid 密钥 ID
     * @return 私钥
     * @throws ArtifactSignatureException 路径未配置、文件不存在或内容不是合法 PKCS#8 私钥时抛出
     */
    private PrivateKey loadPrivateKey(String kid) {
        if (privateKeyPathTemplate.isEmpty()) {
            throw new ArtifactSignatureException(
                    "未配置 houcloud.artifact.sign.private-key-path，无法对制品签名（kid=" + kid + "）");
        }
        String rawPath = privateKeyPathTemplate.replace(KID_PLACEHOLDER, kid);
        if (rawPath.startsWith(FILE_PREFIX)) {
            rawPath = rawPath.substring(FILE_PREFIX.length());
        }
        Path path = Path.of(rawPath);
        if (!Files.isRegularFile(path)) {
            throw new ArtifactSignatureException(
                    "制品签名私钥文件不存在：path=" + path + "，请检查 houcloud.artifact.sign.private-key-path（kid=" + kid + "）");
        }
        byte[] keyBytes;
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            // 容错 PEM 头尾（openssl genpkey 默认输出）：只取 -----BEGIN/END----- 之间的 Base64 载荷。
            // 规范格式仍是「裸 Base64(PKCS#8 DER)」（见 scripts/plugin/gen-sign-keys.sh）；
            // 这里只是不让一次格式手误把整个发布链路炸断。
            if (content.startsWith("-----")) {
                StringBuilder payload = new StringBuilder();
                for (String line : content.split("\\R")) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("-----")) {
                        continue;
                    }
                    payload.append(trimmed);
                }
                content = payload.toString();
            }
            keyBytes = Base64.getMimeDecoder().decode(content);
        } catch (Exception e) {
            throw new ArtifactSignatureException(
                    "读取制品签名私钥失败，文件须为 Base64(PKCS#8) 文本：path=" + path, e);
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (GeneralSecurityException e) {
            throw new ArtifactSignatureException(
                    "解析制品签名私钥失败，内容不是合法 Ed25519 PKCS#8 私钥：path=" + path, e);
        }
    }

    /**
     * 解析（并缓存）指定 kid 的公钥，未配置返回 null。
     *
     * @param kid 密钥 ID
     * @return 公钥，未配置时为 null
     */
    private PublicKey resolvePublicKey(String kid) {
        return publicKeyCache.computeIfAbsent(kid, k -> {
            String base64 = publicKeys.get(k);
            if (base64 == null || base64.trim().isEmpty()) {
                return null;
            }
            try {
                byte[] keyBytes = Base64.getMimeDecoder().decode(base64.trim());
                KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
                return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
            } catch (IllegalArgumentException | GeneralSecurityException e) {
                // 公钥配置错误：记录后按未知 kid 处理，避免单次配置错误拖垮整个验签链路
                return null;
            }
        });
    }
}
