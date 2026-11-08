package com.houjiang.plugin.artifact.delivery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.mall.service.market.OfficialMaintenancePolicy;
import com.houjiang.plugin.artifact.authorization.PluginDownloadAuthorizationService;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.plugin.signature.ArtifactSigner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Official immutable service releases. Source archives never enter the runtime plugin engine. */
@Service
public class CommercialReleaseService {
    public static final Set<String> PAID_SERVICES = Set.of("crm", "cms", "erp", "finance", "geo", "im", "logging",
            "mes", "oa", "orderfood", "pm", "rider", "supplier", "webpos");
    private final JdbcTemplate jdbc;
    private final PluginLicenseService licenses;
    private final PluginDownloadAuthorizationService authorization;
    private final ArtifactSigner signer;
    private final ArtifactStorage storage;
    private final PluginArtifactMapper artifacts;
    private final MarketPluginVersionMapper versions;
    private final ObjectMapper json = new ObjectMapper();

    public CommercialReleaseService(JdbcTemplate jdbc, PluginLicenseService licenses,
            PluginDownloadAuthorizationService authorization, ArtifactSigner signer, ArtifactStorage storage,
            PluginArtifactMapper artifacts, MarketPluginVersionMapper versions) {
        this.jdbc = jdbc; this.licenses = licenses; this.authorization = authorization;
        this.signer = signer; this.storage = storage; this.artifacts = artifacts; this.versions = versions;
    }

    public record Release(String payload, String signature, String kid, JsonNode manifest) {}

    public Release verify(String envelope) {
        try {
            JsonNode doc = json.readTree(envelope);
            String payload = doc.path("payload").asText();
            String signature = doc.path("signature").asText();
            String kid = doc.path("kid").asText();
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length > 65536 || !signer.verify(kid, bytes, signature))
                throw new IllegalArgumentException("官方发行清单签名无效");
            JsonNode m = json.readTree(bytes);
            String code = m.path("pluginCode").asText();
            if (!PAID_SERVICES.contains(code) || !code.equals(m.path("hostService").asText())
                    || !"standalone".equals(m.path("runtime").asText())
                    || !"Apache-2.0".equals(m.path("sourceLicense").asText())
                    || !m.path("version").asText().matches("[0-9]+\\.[0-9]+\\.[0-9]+(?:-[A-Za-z0-9.-]+)?")
                    || !m.path("image").asText().equals("unimall-" + code + ":" + m.path("version").asText())
                    || !m.path("imageId").asText().matches("sha256:[a-f0-9]{64}"))
                throw new IllegalArgumentException("服务发行清单归属或版本不合法");
            for (String kind : List.of("source", "runtime")) {
                if (!m.path(kind + "Sha256").asText().matches("[a-f0-9]{64}") || m.path(kind + "Size").asLong() <= 0)
                    throw new IllegalArgumentException("发行清单缺少制品摘要或大小");
            }
            return new Release(payload, signature, kid, m);
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalArgumentException("发行清单无法解析", e); }
    }

    public Release release(String code, String version) {
        var rows = jdbc.queryForList("SELECT envelope FROM hou_service_release WHERE plugin_code=? AND version=?", code, version);
        if (rows.size() != 1) throw new DownloadDeniedException(3009, "该版本的源码包和容器制品尚未发布");
        Release release = verify(String.valueOf(rows.get(0).get("envelope")));
        if (!code.equals(release.manifest.path("pluginCode").asText()) || !version.equals(release.manifest.path("version").asText())
                || !code.equals(release.manifest.path("hostService").asText()))
            throw new DownloadDeniedException(3009, "发行清单与请求产品或版本不匹配");
        return release;
    }

    public Release authorize(String code,String version,PluginIdentity identity) {
        authorize(code,version,"source",identity);
        return authorize(code,version,"oci",identity);
    }

    /** Checking eligibility is read-only. Only completed byte transfers establish prior delivery. */
    public Release authorize(String code,String version,String kind,PluginIdentity identity) {
        if(identity==null || !identity.hasInstance())throw new DownloadDeniedException(3001,"未识别登录站点");
        if(!List.of("source","oci").contains(kind))throw new DownloadDeniedException(400,"不支持的商业制品类型");
        var license=licenses.findValidPaid(code,identity.getInstanceId());
        if(license==null)throw new DownloadDeniedException(3002,"只有正式购买对应产品后才能获取源码和服务制品");
        Release release=release(code,version);
        String digest=release.manifest.path(kind.equals("source")?"sourceSha256":"runtimeSha256").asText();
        Integer delivered=jdbc.queryForObject("SELECT COUNT(*) FROM hou_service_artifact_delivery WHERE instance_id=? AND plugin_code=? AND version=? AND artifact_kind=? AND sha256=?",
                Integer.class,identity.getInstanceId(),code,version,kind,digest);
        if(delivered!=null && delivered>0)return release;
        var decision=authorization.authorize(code,version,identity);
        if(!decision.isAllowed())throw new DownloadDeniedException(decision.getCode(),decision.getMessage());
        if(!OfficialMaintenancePolicy.active(license,LocalDateTime.now()))
            throw new DownloadDeniedException(3003,"官方维护期已到期；可继续使用已交付版本，续费后获取新版本");
        return release;
    }

    /** Range downloads become delivered only after all bytes have successfully been sent. */
    @Transactional(rollbackFor=Exception.class)
    public void transferred(String instance,Long operator,String code,String version,String kind,String sha,long size,long start,long end) {
        Release release=release(code,version);
        String prefix="source".equals(kind)?"source":"runtime";
        if(!List.of("source","oci").contains(kind) || !sha.equals(release.manifest.path(prefix+"Sha256").asText())
                || size!=release.manifest.path(prefix+"Size").asLong() || start<0 || end<start || end>=size)
            throw new IllegalArgumentException("下载回执与签名制品不匹配");
        jdbc.update("INSERT INTO hou_service_entitlement_lock(instance_id,plugin_code) VALUES(?,?) ON DUPLICATE KEY UPDATE plugin_code=VALUES(plugin_code)",instance,code);
        jdbc.update("INSERT IGNORE INTO hou_service_artifact_range(instance_id,plugin_code,version,artifact_kind,sha256,range_start,range_end) VALUES(?,?,?,?,?,?,?)",instance,code,version,kind,sha,start,end);
        var ranges=jdbc.queryForList("SELECT range_start,range_end FROM hou_service_artifact_range WHERE instance_id=? AND plugin_code=? AND version=? AND artifact_kind=? AND sha256=? ORDER BY range_start",instance,code,version,kind,sha);
        long covered=0;
        for(var range:ranges) {
            long from=((Number)range.get("range_start")).longValue(),to=((Number)range.get("range_end")).longValue();
            if(from>covered)break;
            covered=Math.max(covered,to+1);
        }
        if(covered==size)jdbc.update("INSERT IGNORE INTO hou_service_artifact_delivery(instance_id,plugin_code,version,artifact_kind,sha256,operator_id) VALUES(?,?,?,?,?,?)",instance,code,version,kind,sha,operator);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publish(String envelope, MultipartFile source, MultipartFile runtime, Long operator) throws Exception {
        Release release = verify(envelope);
        JsonNode m = release.manifest;
        String code = m.path("pluginCode").asText(); String version = m.path("version").asText();
        var existing = jdbc.queryForList("SELECT envelope FROM hou_service_release WHERE plugin_code=? AND version=?", code, version);
        if (!existing.isEmpty()) {
            if (!verify(String.valueOf(existing.get(0).get("envelope"))).payload.equals(release.payload))
                throw new IllegalArgumentException("已发布版本不可替换；请递增版本号");
            return Map.of("pluginCode", code, "version", version, "published", true);
        }
        // Signed checksums are verified before writing immutable storage keys.
        put(code, version, "source", source, m.path("sourceSha256").asText(), m.path("sourceSize").asLong(), release, operator);
        put(code, version, "oci", runtime, m.path("runtimeSha256").asText(), m.path("runtimeSize").asLong(), release, operator);
        jdbc.update("INSERT INTO hou_service_release(plugin_code,version,host_service,envelope) VALUES(?,?,?,?)", code, version, code, envelope);
        MarketPluginVersion row = versions.selectOne(new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, code).eq(MarketPluginVersion::getVersion, version).last("limit 1"));
        if (row == null) { row = new MarketPluginVersion(); row.setPluginCode(code); row.setVersion(version); row.setCreateTime(LocalDateTime.now()); }
        row.setStatus(1); row.setPublishTime(LocalDateTime.now());
        row.setPluginYaml("code: " + code + "\nversion: " + version + "\nruntime: standalone\nhostService: " + code + "\nlicense: PAID\n");
        if (row.getId() == null) versions.insert(row); else versions.updateById(row);
        return Map.of("pluginCode", code, "version", version, "published", true);
    }

    private void put(String code, String version, String kind, MultipartFile file, String sha, long size,
                     Release release, Long operator) throws Exception {
        if (file == null || file.getSize() != size) throw new IllegalArgumentException("制品大小与签名清单不一致");
        var digest = java.security.MessageDigest.getInstance("SHA-256");
        try (var in = file.getInputStream()) { byte[] buffer = new byte[65536]; int n; while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n); }
        if (!java.util.HexFormat.of().formatHex(digest.digest()).equals(sha)) throw new IllegalArgumentException("制品摘要与签名清单不一致");
        String key = kind.equals("source")
                ? ArtifactKeys.commercialSourceKey(code, version)
                : ArtifactKeys.commercialRuntimeKey(code, version);
        StoredObject stored;
        try (var in = file.getInputStream()) { stored = storage.putIfAbsent(key, in, size); }
        catch (com.houjiang.plugin.artifact.exception.ArtifactConflictException alreadyStored) {
            // Retry after a transaction failure must not overwrite an immutable blob.
            var check = java.security.MessageDigest.getInstance("SHA-256");
            try (var stream = storage.open(key, null, null)) { byte[] b = new byte[65536]; int n; while ((n=stream.getStream().read(b))!=-1) check.update(b,0,n); }
            if (!java.util.HexFormat.of().formatHex(check.digest()).equals(sha)) throw alreadyStored;
            stored = new StoredObject(key, "", sha, size, storedStorageTypeFallback());
        }
        var a = new PluginArtifact(); a.setArtifactId(sha); a.setPluginCode(code); a.setVersion(version);
        a.setArtifactKind(kind); a.setHostService(code); a.setStorageType(stored.getStorageType()); a.setStorageKey(key);
        a.setMd5(stored.getMd5()); a.setSha256(sha); a.setSizeBytes(size); a.setUploadStatus(1);
        a.setUploadId(code+"-"+version+"-"+kind); a.setCreatedBy(operator); a.setCreateTime(LocalDateTime.now());
        a.setSignatureKid(release.kid());
        a.setSourceLicense(kind.equals("source") ? "Apache-2.0" : null);
        a.setRuntimeArtifact(kind.equals("oci") ? release.manifest.path("imageId").asText() : null);
        artifacts.insert(a);
    }

    private String storedStorageTypeFallback() {
        String name = storage.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("cos")) return "cos";
        if (name.contains("minio")) return "minio";
        return "local";
    }
}
