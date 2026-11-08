package com.houjiang.thirdparty.migration;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationPhaseTwoMigrationTest {

    @Test
    void migrationAddsCompatibleIdempotencyAndOwnershipIndexes() throws Exception {
        try (var input = getClass().getResourceAsStream(
                "/sql/2026-07-24-task-notification-phase2.sql")) {
            assertNotNull(input, "第二阶段通知迁移必须随 thirdparty 服务发布");
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).toLowerCase();

            assertAll(
                    () -> assertTrue(sql.contains("idempotency_key varchar(160) null")),
                    () -> assertTrue(sql.contains("uk_message_record_idempotency")),
                    () -> assertTrue(sql.contains("uk_message_inbox_idempotency")),
                    () -> assertTrue(sql.contains("idx_message_record_org_filter")),
                    () -> assertTrue(sql.contains("idx_message_inbox_owner")),
                    () -> assertTrue(sql.contains("signal sqlstate")),
                    () -> assertFalse(sql.contains("update hou_message_record set idempotency_key")));

            int assertionCall = sql.indexOf("call message_notification_phase2_assert_data()");
            int recordColumn = sql.indexOf("'hou_message_record', 'idempotency_key'");
            int inboxColumn = sql.indexOf("'hou_message_inbox', 'idempotency_key'");
            int firstUniqueIndex = sql.indexOf("'hou_message_record', 'uk_message_record_idempotency'");

            assertAll(
                    () -> assertTrue(assertionCall > recordColumn,
                            "重复键断言必须在发送记录幂等列存在后执行"),
                    () -> assertTrue(assertionCall > inboxColumn,
                            "重复键断言必须在收件箱幂等列存在后执行"),
                    () -> assertTrue(assertionCall < firstUniqueIndex,
                            "重复键断言必须在创建唯一索引前执行"));
        }
    }
}
