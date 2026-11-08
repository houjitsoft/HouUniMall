package com.houjiang.userms.ucenter.service;

public class UcenterDomainException extends RuntimeException {
    public enum Kind {
        UNAUTHENTICATED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        EXPIRED,
        INVALID_TRANSITION,
        INVALID_INPUT
    }

    private final Kind kind;

    public UcenterDomainException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
