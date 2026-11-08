/*
 * Copyright 2026 厚匠科技
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.houjiang.commons.util.secverifyapiutil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * MD5 摘要工具类。
 *
 * <p>提供 32 位十六进制与 Base64 两种输出形式，供一键登录、短信网关等外部
 * 接口的签名计算使用。MD5 不具备抗碰撞性，仅用于对接既有外部协议，
 * 不应用于口令存储等安全场景。
 *
 * @author Amos
 */
public final class MessageMD5Util {

    private static final String ALGORITHM = "MD5";

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private MessageMD5Util() {
    }

    /**
     * 计算字符串的 MD5 摘要，返回 32 位小写十六进制。
     *
     * @param str 待摘要的字符串（按 UTF-8 编码）
     * @return 32 位小写十六进制字符串
     */
    public static String encoder32ByMd5(String str) {
        return toHex(digest(str));
    }

    /**
     * 计算字符串的 MD5 摘要，返回 Base64 编码结果。
     *
     * @param str 待摘要的字符串（按 UTF-8 编码）
     * @return Base64 编码字符串
     */
    public static String Encoder64ByMd5(String str) {
        return Base64.getEncoder().encodeToString(digest(str));
    }

    private static byte[] digest(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(str.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            // JDK 规范要求所有实现必须支持 MD5，此处理论不可达。
            // 原实现调用 System.exit(-1) 会直接终止整个 JVM，改为抛出异常交由调用方处理。
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX[(b >> 4) & 0x0f]).append(HEX[b & 0x0f]);
        }
        return sb.toString();
    }
}
