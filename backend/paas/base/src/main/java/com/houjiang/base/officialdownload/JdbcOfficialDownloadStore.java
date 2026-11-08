package com.houjiang.base.officialdownload;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcOfficialDownloadStore implements VerificationStore, LeadStore, ArtifactCatalog,
        SessionStore, DownloadTicketStore {
    private final JdbcTemplate jdbc;

    public JdbcOfficialDownloadStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public VerificationRecord findLatest(String phoneHash) {
        List<VerificationRecord> rows = jdbc.query("select id,phone_hash,phone_masked,code_hash,ip_hash,sent_at,expires_at,attempts,verified,locked from hou_open_download_verification where phone_hash=? order by id desc limit 1",
                (rs, n) -> new VerificationRecord(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getTimestamp(6).toInstant(), rs.getTimestamp(7).toInstant(), rs.getInt(8),
                        rs.getBoolean(9), rs.getBoolean(10)), phoneHash);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override public long countRecentByIp(String ipHash, Instant since) {
        Long count = jdbc.queryForObject("select count(*) from hou_open_download_verification where ip_hash=? and sent_at>=?",
                Long.class, ipHash, Timestamp.from(since));
        return count == null ? 0 : count;
    }

    @Override public void save(VerificationRecord r) {
        jdbc.update("insert into hou_open_download_verification(phone_hash,phone_masked,code_hash,ip_hash,sent_at,expires_at,attempts,verified,locked) values(?,?,?,?,?,?,?,?,?)",
                r.phoneHash(), r.phoneMasked(), r.codeHash(), r.ipHash(), Timestamp.from(r.sentAt()),
                Timestamp.from(r.expiresAt()), r.attempts(), r.verified(), r.locked());
    }

    @Override public void update(VerificationRecord r) {
        jdbc.update("update hou_open_download_verification set attempts=?,verified=?,locked=? where id=?",
                r.attempts(), r.verified(), r.locked(), r.id());
    }

    @Override @Transactional
    public DownloadLead upsert(DownloadLead c) {
        List<Long> ids = jdbc.query("select id from hou_open_download_lead where phone_hash=? limit 1",
                (rs, n) -> rs.getLong(1), c.phoneHash());
        if (!ids.isEmpty()) {
            Long id = ids.get(0);
            jdbc.update("update hou_open_download_lead set phone_encrypted=?,phone_masked=?,identity_type=?,company_name=?,use_case_text=?,source=?,privacy_version=?,marketing_consent=?,consent_at=?,last_seen_at=? where id=?",
                    c.phoneEncrypted(), c.phoneMasked(), c.identityType(), c.companyName(), c.useCase(), c.source(),
                    c.privacyVersion(), c.marketingConsent(), timestamp(c.consentAt()), Timestamp.from(c.firstSeenAt()), id);
            return c.withId(id);
        }
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into hou_open_download_lead(phone_encrypted,phone_hash,phone_masked,identity_type,company_name,use_case_text,source,privacy_version,marketing_consent,consent_at,first_seen_at,last_seen_at) values(?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            Object[] values = {c.phoneEncrypted(), c.phoneHash(), c.phoneMasked(), c.identityType(), c.companyName(),
                    c.useCase(), c.source(), c.privacyVersion(), c.marketingConsent(), timestamp(c.consentAt()),
                    Timestamp.from(c.firstSeenAt()), Timestamp.from(c.firstSeenAt())};
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, key);
        return c.withId(key.getKey().longValue());
    }

    @Override public DownloadArtifact findPublished(String product, String version) {
        List<DownloadArtifact> rows = jdbc.query("select id,product_code,version,file_name,object_key,sha256,published from hou_open_download_artifact where product_code=? and version=? and published=1 limit 1",
                (rs, n) -> new DownloadArtifact(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getBoolean(7)), product, version);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override public void save(VerifiedSession s) {
        jdbc.update("insert into hou_open_download_session(token_hash,lead_id,phone_hash,ip_hash,expires_at) values(?,?,?,?,?)",
                s.tokenHash(), s.leadId(), s.phoneHash(), s.ipHash(), Timestamp.from(s.expiresAt()));
    }

    @Override public VerifiedSession findActive(String tokenHash, Instant now) {
        List<VerifiedSession> rows = jdbc.query("select lead_id,token_hash,phone_hash,expires_at,ip_hash from hou_open_download_session where token_hash=? and expires_at>? limit 1",
                (rs, n) -> new VerifiedSession(rs.getLong(1), null, rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4).toInstant(), rs.getString(5)), tokenHash, Timestamp.from(now));
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override public void save(DownloadTicket t) {
        jdbc.update("insert into hou_open_download_ticket(token_hash,lead_id,artifact_id,ip_hash,expires_at,remaining_downloads) values(?,?,?,?,?,?)",
                t.tokenHash(), t.leadId(), t.artifactId(), t.ipHash(), Timestamp.from(t.expiresAt()), t.remainingDownloads());
    }

    @Override @Transactional
    public DownloadTicket consume(String tokenHash, String ipHash, Instant now) {
        int changed = jdbc.update("update hou_open_download_ticket set remaining_downloads=remaining_downloads-1,last_download_at=? where token_hash=? and ip_hash=? and expires_at>? and remaining_downloads>0",
                Timestamp.from(now), tokenHash, ipHash, Timestamp.from(now));
        if (changed == 0) return null;
        return jdbc.queryForObject("select token_hash,lead_id,artifact_id,ip_hash,expires_at,remaining_downloads from hou_open_download_ticket where token_hash=?",
                (rs, n) -> new DownloadTicket(null, rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getString(4),
                        rs.getTimestamp(5).toInstant(), rs.getInt(6)), tokenHash);
    }

    public DownloadArtifact findById(Long id) {
        return jdbc.queryForObject("select id,product_code,version,file_name,object_key,sha256,published from hou_open_download_artifact where id=?",
                (rs, n) -> new DownloadArtifact(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getBoolean(7)), id);
    }

    public List<Map<String, Object>> listPublishedArtifacts() {
        return jdbc.query("select product_code,version,file_name,sha256,published_at from hou_open_download_artifact where published=1 order by published_at desc",
                (rs, n) -> Map.of("product", rs.getString(1), "version", rs.getString(2),
                        "fileName", rs.getString(3), "sha256", rs.getString(4),
                        "publishedAt", rs.getTimestamp(5).toInstant().toString()));
    }

    public void recordEvent(Long leadId, Long artifactId, String type, String ipHash, String result) {
        jdbc.update("insert into hou_open_download_event(lead_id,artifact_id,event_type,ip_hash,result,created_at) values(?,?,?,?,?,?)",
                leadId, artifactId, type, ipHash, result, Timestamp.from(Instant.now()));
    }

    public void registerArtifact(String product, String version, String gitTag, String gitCommit,
                                 String fileName, String objectKey, String sha256) {
        jdbc.update("insert into hou_open_download_artifact(product_code,version,git_tag,git_commit,file_name,object_key,sha256,published,published_at) values(?,?,?,?,?,?,?,1,?) on duplicate key update git_tag=values(git_tag),git_commit=values(git_commit),file_name=values(file_name),object_key=values(object_key),sha256=values(sha256),published=1,published_at=values(published_at)",
                product, version, gitTag, gitCommit, fileName, objectKey, sha256, Timestamp.from(Instant.now()));
    }

    private static Timestamp timestamp(Instant value) { return value == null ? null : Timestamp.from(value); }
}
