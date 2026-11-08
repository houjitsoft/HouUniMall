package com.houjiang.base.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecognizeSchemaInitializer {
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        for (String sql : statements()) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception ex) {
                log.warn("智能入库表初始化失败，服务继续启动: {}", ex.getMessage());
                return;
            }
        }
    }

    static List<String> statements() {
        return List.of(
                "CREATE TABLE IF NOT EXISTS hou_base_ai_import_draft (id BIGINT NOT NULL AUTO_INCREMENT,draft_no VARCHAR(64) NOT NULL,user_id VARCHAR(64) NOT NULL,org_id VARCHAR(64) DEFAULT NULL,system_key VARCHAR(64) NOT NULL,description VARCHAR(500) NOT NULL,status VARCHAR(32) NOT NULL DEFAULT 'draft',create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY(id),UNIQUE KEY uk_ai_import_draft_no(draft_no),KEY idx_ai_import_draft_user_status(user_id,status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能入库草稿'",
                "CREATE TABLE IF NOT EXISTS hou_base_ai_import_file (id BIGINT NOT NULL AUTO_INCREMENT,draft_id BIGINT NOT NULL,file_name VARCHAR(255) NOT NULL,file_hash VARCHAR(64) NOT NULL,file_url VARCHAR(1000) DEFAULT NULL,file_size BIGINT NOT NULL DEFAULT 0,content_type VARCHAR(128) DEFAULT NULL,ai_raw_json JSON DEFAULT NULL,edited_fields_json JSON DEFAULT NULL,status VARCHAR(32) NOT NULL DEFAULT 'uploaded',error_message VARCHAR(1000) DEFAULT NULL,create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY(id),UNIQUE KEY uk_ai_import_draft_file(draft_id,file_hash),KEY idx_ai_import_file_draft_status(draft_id,status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能入库文件'",
                "CREATE TABLE IF NOT EXISTS hou_base_ai_import_result (id BIGINT NOT NULL AUTO_INCREMENT,draft_id BIGINT NOT NULL,file_id BIGINT NOT NULL,function_key VARCHAR(128) DEFAULT NULL,object_key VARCHAR(128) DEFAULT NULL,target_table VARCHAR(128) DEFAULT NULL,ai_fields_json JSON DEFAULT NULL,edited_fields_json JSON DEFAULT NULL,status VARCHAR(32) NOT NULL DEFAULT 'pending',error_message VARCHAR(1000) DEFAULT NULL,business_record_id VARCHAR(128) DEFAULT NULL,detail_url VARCHAR(1000) DEFAULT NULL,create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY(id),UNIQUE KEY uk_ai_import_result_file(file_id),KEY idx_ai_import_result_draft_status(draft_id,status)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能入库结果'",
                "CREATE TABLE IF NOT EXISTS hou_base_ai_import_attachment (id BIGINT NOT NULL AUTO_INCREMENT,draft_id BIGINT NOT NULL,file_id BIGINT NOT NULL,object_key VARCHAR(128) DEFAULT NULL,business_record_id VARCHAR(128) DEFAULT NULL,file_name VARCHAR(255) NOT NULL,file_url VARCHAR(1000) NOT NULL,status VARCHAR(32) NOT NULL DEFAULT 'pending',error_message VARCHAR(1000) DEFAULT NULL,create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY(id),KEY idx_ai_import_attachment_record(object_key,business_record_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能入库附件'"
        );
    }
}
