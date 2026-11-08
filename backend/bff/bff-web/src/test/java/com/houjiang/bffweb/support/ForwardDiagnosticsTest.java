package com.houjiang.bffweb.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ForwardDiagnostics} 单测（P0 502 排查抓手）。
 *
 * <p>重点验证三件事：队列有界（不能把内存吃光）、凭据打码（不能让令牌从诊断口子漏出去）、
 * 失败与解析分开取（排障时两类信息用途不同）。
 *
 * @author Amos
 * @since M4 补丁（502 排查）
 */
class ForwardDiagnosticsTest {

    @Test
    void 队列有界_超出上限丢弃最旧的() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        for (int i = 0; i < ForwardDiagnostics.CAPACITY + 50; i++) {
            diagnostics.recordResolved("http://127.0.0.1:8220/" + i, "config");
        }

        assertEquals(ForwardDiagnostics.CAPACITY, diagnostics.size());
        List<ForwardDiagnostics.Entry> entries = diagnostics.recent(ForwardDiagnostics.CAPACITY);
        // 最旧的 50 条已被丢弃：第一条应该是第 50 号
        assertEquals("http://127.0.0.1:8220/50", entries.get(0).url());
    }

    @Test
    void 失败与解析分开取() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        diagnostics.recordResolved("http://127.0.0.1:8220", "config");
        diagnostics.recordFailure("http://127.0.0.1:8220/mall/artifact/raw", "config",
                new ConnectException("Connection refused"));

        assertEquals(1, diagnostics.recentFailures(10).size());
        assertEquals(1, diagnostics.recentResolved(10).size());
        assertEquals(2, diagnostics.recent(10).size());
    }

    @Test
    void limit大于实际条数时全部返回_不越界() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        diagnostics.recordResolved("http://127.0.0.1:8220", "config");

        assertEquals(1, diagnostics.recent(200).size());
        assertEquals(1, diagnostics.recent(0).size(), "limit 非法时按默认 20 处理");
    }

    @Test
    void 返回的是最近N条_按时间从旧到新() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        for (int i = 0; i < 10; i++) {
            diagnostics.recordResolved("http://127.0.0.1:8220/" + i, "config");
        }

        List<ForwardDiagnostics.Entry> entries = diagnostics.recent(3);
        assertEquals(3, entries.size());
        assertEquals("http://127.0.0.1:8220/7", entries.get(0).url());
        assertEquals("http://127.0.0.1:8220/9", entries.get(2).url());
    }

    @Test
    void 敏感query参数被打码_业务参数保留() {
        String redacted = ForwardDiagnostics.redact(
                "http://127.0.0.1:8220/mall/artifact/raw?pluginCode=demo&version=1.0.0&token=SECRET-TOKEN");

        assertEquals("http://127.0.0.1:8220/mall/artifact/raw?pluginCode=demo&version=1.0.0&token=***",
                redacted);
        assertFalse(redacted.contains("SECRET-TOKEN"), "令牌绝不能出现在诊断输出里");
        assertTrue(redacted.contains("pluginCode=demo"), "排障需要的业务参数必须保留");
    }

    @Test
    void 多种凭据参数都会被打码() {
        String redacted = ForwardDiagnostics.redact(
                "http://h/p?token=a&sign=b&password=c&Signature=d&kid=e&plain=f");

        assertEquals("http://h/p?token=***&sign=***&password=***&Signature=***&kid=***&plain=f", redacted);
    }

    @Test
    void 超长URL被截断() {
        StringBuilder sb = new StringBuilder("http://127.0.0.1:8220/p?x=");
        sb.append("y".repeat(2000));
        String redacted = ForwardDiagnostics.redact(sb.toString());

        assertTrue(redacted.length() <= 520,
                "超长 URL 必须截断，否则一条记录就能把响应撑爆，实际长度 " + redacted.length());
        assertTrue(redacted.endsWith("..."));
    }

    @Test
    void 没有query或为空输入时不报错() {
        assertEquals("http://127.0.0.1:8220/p", ForwardDiagnostics.redact("http://127.0.0.1:8220/p"));
        assertEquals("   ", ForwardDiagnostics.redact("   "));
        assertEquals("", ForwardDiagnostics.redact(""));
    }

    @Test
    void 异常消息为空或超长都不会影响记录() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        diagnostics.recordFailure("http://h/p", "nacos", new SocketTimeoutException());
        diagnostics.recordFailure("http://h/p", "nacos", null);

        List<ForwardDiagnostics.Entry> failures = diagnostics.recentFailures(10);
        assertEquals(2, failures.size());
        assertEquals("java.net.SocketTimeoutException", failures.get(0).errType());
        assertNull(failures.get(1).errType());
    }

    @Test
    void clear可以清空() {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        diagnostics.recordResolved("http://h", "config");
        diagnostics.clear();
        assertEquals(0, diagnostics.size());
    }
}
