-- Task notification phase 2. MySQL 8, safe to execute repeatedly.
DELIMITER $$

DROP PROCEDURE IF EXISTS message_notification_phase2_assert_data$$
CREATE PROCEDURE message_notification_phase2_assert_data()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'hou_message_record'
          AND column_name = 'idempotency_key'
    ) AND EXISTS (
        SELECT idempotency_key
        FROM hou_message_record
        WHERE idempotency_key IS NOT NULL AND idempotency_key <> ''
        GROUP BY idempotency_key
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate message record idempotency key';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'hou_message_inbox'
          AND column_name = 'idempotency_key'
    ) AND EXISTS (
        SELECT idempotency_key
        FROM hou_message_inbox
        WHERE idempotency_key IS NOT NULL AND idempotency_key <> ''
        GROUP BY idempotency_key
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate message inbox idempotency key';
    END IF;
END$$

DROP PROCEDURE IF EXISTS message_notification_phase2_add_column$$
CREATE PROCEDURE message_notification_phase2_add_column(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND column_name = target_column
    ) THEN
        SET @notification_phase2_ddl = ddl_statement;
        PREPARE notification_phase2_statement FROM @notification_phase2_ddl;
        EXECUTE notification_phase2_statement;
        DEALLOCATE PREPARE notification_phase2_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS message_notification_phase2_add_index$$
CREATE PROCEDURE message_notification_phase2_add_index(
    IN target_table VARCHAR(64),
    IN target_index VARCHAR(64),
    IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND index_name = target_index
    ) THEN
        SET @notification_phase2_ddl = ddl_statement;
        PREPARE notification_phase2_statement FROM @notification_phase2_ddl;
        EXECUTE notification_phase2_statement;
        DEALLOCATE PREPARE notification_phase2_statement;
    END IF;
END$$

CALL message_notification_phase2_add_column(
    'hou_message_record', 'idempotency_key',
    'ALTER TABLE hou_message_record ADD COLUMN idempotency_key VARCHAR(160) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'event_type',
    'ALTER TABLE hou_message_record ADD COLUMN event_type VARCHAR(48) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'error_code',
    'ALTER TABLE hou_message_record ADD COLUMN error_code VARCHAR(64) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_record', 'provider_message_id',
    'ALTER TABLE hou_message_record ADD COLUMN provider_message_id VARCHAR(160) NULL'
)$$
CALL message_notification_phase2_add_column(
    'hou_message_inbox', 'idempotency_key',
    'ALTER TABLE hou_message_inbox ADD COLUMN idempotency_key VARCHAR(160) NULL'
)$$

CALL message_notification_phase2_assert_data()$$

CALL message_notification_phase2_add_index(
    'hou_message_record', 'uk_message_record_idempotency',
    'CREATE UNIQUE INDEX uk_message_record_idempotency ON hou_message_record (idempotency_key)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_inbox', 'uk_message_inbox_idempotency',
    'CREATE UNIQUE INDEX uk_message_inbox_idempotency ON hou_message_inbox (idempotency_key)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_record', 'idx_message_record_org_filter',
    'CREATE INDEX idx_message_record_org_filter ON hou_message_record (org_id, biz_type, channel, send_status, create_time)'
)$$
CALL message_notification_phase2_add_index(
    'hou_message_inbox', 'idx_message_inbox_owner',
    'CREATE INDEX idx_message_inbox_owner ON hou_message_inbox (user_id, org_id, is_read, delete_time, create_time)'
)$$

DROP PROCEDURE IF EXISTS message_notification_phase2_assert_data$$
DROP PROCEDURE IF EXISTS message_notification_phase2_add_column$$
DROP PROCEDURE IF EXISTS message_notification_phase2_add_index$$

DELIMITER ;
