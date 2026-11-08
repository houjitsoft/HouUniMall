package com.houjiang.commons.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 兼容 PHP / 历史数据中的 {@code $2y$} BCrypt 哈希（Spring 默认仅稳定识别 {@code $2a$}）。
 * @author Amos
 */
public class HouBCryptPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    /**
     * 将 {@code $2y$} / {@code $2b$} 规范为 {@code $2a$}，校验逻辑与 PHP password_verify 一致。
     */
    public static String normalizeBcryptHash(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() < 4) {
            return encodedPassword;
        }
        if (encodedPassword.startsWith("$2y$") || encodedPassword.startsWith("$2b$")) {
            return "$2a$" + encodedPassword.substring(4);
        }
        return encodedPassword;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, normalizeBcryptHash(encodedPassword));
    }
}
