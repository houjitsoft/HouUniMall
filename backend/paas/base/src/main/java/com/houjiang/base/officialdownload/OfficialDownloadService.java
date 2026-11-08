package com.houjiang.base.officialdownload;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

public final class OfficialDownloadService {
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern CODE = Pattern.compile("^\\d{6}$");

    private final VerificationStore verifications;
    private final LeadStore leads;
    private final ArtifactCatalog artifacts;
    private final SessionStore sessions;
    private final DownloadTicketStore tickets;
    private final DownloadSecurity security;
    private final VerificationCodeSender sender;
    private final Clock clock;
    private final OfficialDownloadPolicy policy;

    public OfficialDownloadService(VerificationStore verifications, LeadStore leads, ArtifactCatalog artifacts,
                                   SessionStore sessions, DownloadTicketStore tickets, DownloadSecurity security,
                                   VerificationCodeSender sender, Clock clock, OfficialDownloadPolicy policy) {
        this.verifications = Objects.requireNonNull(verifications);
        this.leads = Objects.requireNonNull(leads);
        this.artifacts = Objects.requireNonNull(artifacts);
        this.sessions = Objects.requireNonNull(sessions);
        this.tickets = Objects.requireNonNull(tickets);
        this.security = Objects.requireNonNull(security);
        this.sender = Objects.requireNonNull(sender);
        this.clock = Objects.requireNonNull(clock);
        this.policy = Objects.requireNonNull(policy);
    }

    public void requestCode(String phone, String clientIp) {
        String normalized = phone == null ? "" : phone.trim();
        if (!PHONE.matcher(normalized).matches()) fail("PHONE_INVALID", "请输入正确的中国大陆手机号");
        Instant now = clock.instant();
        String phoneHash = security.hmac(normalized);
        VerificationRecord latest = verifications.findLatest(phoneHash);
        if (latest != null && latest.sentAt().plusSeconds(policy.smsCooldownSeconds()).isAfter(now)) {
            fail("SMS_TOO_FREQUENT", "发送过于频繁，请稍后再试");
        }
        String ipHash = security.hmac(nullToEmpty(clientIp));
        if (verifications.countRecentByIp(ipHash, now.minusSeconds(3600)) >= 20) {
            fail("IP_RATE_LIMITED", "当前网络请求过于频繁，请稍后再试");
        }
        String code = sender.send(normalized);
        if (code == null || !CODE.matcher(code).matches()) fail("SMS_SEND_FAILED", "短信服务暂时不可用");
        verifications.save(new VerificationRecord(null, phoneHash, DownloadSecurity.maskPhone(normalized),
                security.hmac(phoneHash + ":" + code), ipHash, now,
                now.plusSeconds(policy.codeTtlSeconds()), 0, false, false));
    }

    public VerifiedSession verifyCode(VerifyCodeCommand command) {
        if (command == null || !command.privacyAccepted() || blank(command.privacyVersion())) {
            fail("PRIVACY_NOT_ACCEPTED", "请先阅读并同意隐私政策");
        }
        String phone = command.phone() == null ? "" : command.phone().trim();
        if (!PHONE.matcher(phone).matches() || !CODE.matcher(nullToEmpty(command.code())).matches()) {
            fail("SMS_CODE_INVALID", "手机号或验证码不正确");
        }
        Instant now = clock.instant();
        String phoneHash = security.hmac(phone);
        VerificationRecord record = verifications.findLatest(phoneHash);
        if (record == null || !record.expiresAt().isAfter(now)) fail("SMS_CODE_EXPIRED", "验证码已过期");
        if (record.locked()) fail("SMS_CODE_LOCKED", "验证码尝试次数过多，请重新获取");
        if (!record.codeHash().equals(security.hmac(phoneHash + ":" + command.code()))) {
            VerificationRecord failed = record.failedAttempt(policy.maxCodeAttempts());
            verifications.update(failed);
            fail(failed.locked() ? "SMS_CODE_LOCKED" : "SMS_CODE_INVALID", "验证码不正确");
        }
        verifications.update(record.markVerified());
        DownloadLead lead = leads.upsert(new DownloadLead(null, security.encrypt(phone), phoneHash,
                DownloadSecurity.maskPhone(phone), clean(command.identityType()), clean(command.companyName()),
                clean(command.useCase()), clean(command.source()), clean(command.privacyVersion()),
                command.marketingConsent(), command.marketingConsent() ? now : null, now, null));
        String token = security.randomToken();
        VerifiedSession session = new VerifiedSession(lead.id(), token, security.hmac(token), phoneHash,
                now.plusSeconds(policy.sessionTtlSeconds()), security.hmac(nullToEmpty(command.clientIp())));
        sessions.save(session);
        return session;
    }

    public DownloadTicket issueTicket(String sessionToken, String product, String version, String clientIp) {
        Instant now = clock.instant();
        if (blank(sessionToken)) fail("SESSION_INVALID", "验证会话无效，请重新验证手机号");
        VerifiedSession session = sessions.findActive(security.hmac(sessionToken), now);
        if (session == null || !session.expiresAt().isAfter(now)) fail("SESSION_INVALID", "验证会话已过期");
        String ipHash = security.hmac(nullToEmpty(clientIp));
        if (!session.ipHash().equals(ipHash)) fail("SESSION_IP_CHANGED", "网络环境发生变化，请重新验证手机号");
        DownloadArtifact artifact = artifacts.findPublished(clean(product), clean(version));
        if (artifact == null || !artifact.published()) fail("ARTIFACT_NOT_FOUND", "该版本暂未提供官方下载包");
        String token = security.randomToken();
        DownloadTicket ticket = new DownloadTicket(token, security.hmac(token), session.leadId(), artifact.id(),
                ipHash, now.plusSeconds(600), policy.maxDownloads());
        tickets.save(ticket);
        return ticket;
    }

    private static void fail(String code, String message) { throw new DownloadFlowException(code, message); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String clean(String value) { return blank(value) ? null : value.trim(); }
    private static String nullToEmpty(String value) { return value == null ? "" : value; }
}
