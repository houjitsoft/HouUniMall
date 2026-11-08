package com.houjiang.base.officialdownload;

@FunctionalInterface
public interface VerificationCodeSender {
    String send(String phone);
}
