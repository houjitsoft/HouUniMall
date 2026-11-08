package com.houjiang.userms.ucenter.service;

public class UcenterInvitationExpiredException extends UcenterDomainException {
    public UcenterInvitationExpiredException() {
        super(Kind.EXPIRED, "邀请已过期");
    }
}
