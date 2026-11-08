package com.houjiang.base.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FileRecognizeDraftJdbcMapper implements FileRecognizeDraftMapper {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> findDraftByNo(String draftNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, draft_no AS draftNo, user_id AS userId, org_id AS orgId, system_key AS systemKey, description, status "
                        + "FROM hou_base_ai_import_draft WHERE draft_no=?", draftNo);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Map<String, Object> findFileByDraftAndHash(Long draftId, String fileHash) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, file_name AS fileName, file_hash AS fileHash, file_url AS fileUrl, file_size AS fileSize, content_type AS contentType, status "
                        + "FROM hou_base_ai_import_file WHERE draft_id=? AND file_hash=?", draftId, fileHash);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public long insertDraft(Map<String, Object> values) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO hou_base_ai_import_draft(draft_no,user_id,org_id,system_key,description,status,create_time,update_time) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            bind(ps, values, "draftNo", "userId", "orgId", "systemKey", "description", "status");
            return ps;
        }, holder);
        return key(holder);
    }

    @Override
    public long insertFile(Map<String, Object> values) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO hou_base_ai_import_file(draft_id,file_name,file_hash,file_url,file_size,content_type,ai_raw_json,edited_fields_json,status,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            bind(ps, values, "draftId", "fileName", "fileHash", "fileUrl", "fileSize", "contentType", "aiRawJson", "editedFieldsJson", "status");
            return ps;
        }, holder);
        return key(holder);
    }

    @Override
    public void updateDraft(Long id, String systemKey, String description, String status) {
        jdbcTemplate.update("UPDATE hou_base_ai_import_draft SET system_key=?,description=?,status=?,update_time=CURRENT_TIMESTAMP WHERE id=?",
                systemKey, description, status, id);
    }

    @Override
    public Map<String, Object> findLatestOpenDraft(String userId, String orgId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, draft_no AS draftNo, user_id AS userId, org_id AS orgId, system_key AS systemKey, description, status "
                        + "FROM hou_base_ai_import_draft WHERE user_id=? AND COALESCE(org_id,'')=COALESCE(?,'') "
                        + "AND status NOT IN ('completed','abandoned') ORDER BY update_time DESC LIMIT 1",
                userId, orgId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public List<Map<String, Object>> findItemsByDraftId(Long draftId) {
        return jdbcTemplate.queryForList(
                "SELECT f.id AS itemId,f.file_name AS fileName,f.file_hash AS fileHash,f.file_url AS fileUrl,"
                        + "f.file_size AS fileSize,f.content_type AS contentType,f.status,"
                        + "f.ai_raw_json AS aiRawJson,"
                        + "COALESCE(r.ai_fields_json,f.ai_raw_json) AS aiFieldsJson,"
                        + "COALESCE(r.edited_fields_json,f.edited_fields_json) AS editedFieldsJson,"
                        + "r.function_key AS functionKey,r.object_key AS objectKey,r.error_message AS errorMessage,"
                        + "r.business_record_id AS businessRecordId,r.detail_url AS detailUrl "
                        + "FROM hou_base_ai_import_file f LEFT JOIN hou_base_ai_import_result r ON r.file_id=f.id "
                        + "WHERE f.draft_id=? ORDER BY f.id", draftId);
    }

    @Override
    public Map<String, Object> findItemContext(Long itemId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT f.id AS itemId,f.draft_id AS draftId,d.draft_no AS draftNo,d.user_id AS userId,d.org_id AS orgId,"
                        + "d.system_key AS systemKey FROM hou_base_ai_import_file f "
                        + "JOIN hou_base_ai_import_draft d ON d.id=f.draft_id WHERE f.id=?", itemId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public void updateItem(Long itemId, String functionKey, String objectKey, String editedFieldsJson) {
        Map<String, Object> context = findItemContext(itemId);
        if (context == null) return;
        jdbcTemplate.update(
                "INSERT INTO hou_base_ai_import_result(draft_id,file_id,function_key,object_key,edited_fields_json,status,create_time,update_time) "
                        + "VALUES(?,?,?,?,?,'edited',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) "
                        + "ON DUPLICATE KEY UPDATE function_key=VALUES(function_key),object_key=VALUES(object_key),"
                        + "edited_fields_json=VALUES(edited_fields_json),status='edited',update_time=CURRENT_TIMESTAMP",
                context.get("draftId"), itemId, functionKey, objectKey, editedFieldsJson);
        jdbcTemplate.update("UPDATE hou_base_ai_import_file SET edited_fields_json=?,status='edited',update_time=CURRENT_TIMESTAMP WHERE id=?",
                editedFieldsJson, itemId);
    }

    @Override
    public void saveRecognition(Long draftId, Long itemId, String functionKey, String objectKey,
                                String aiRawJson, String aiFieldsJson, String status, String errorMessage) {
        jdbcTemplate.update("UPDATE hou_base_ai_import_file SET ai_raw_json=?,status=?,error_message=?,update_time=CURRENT_TIMESTAMP WHERE id=? AND draft_id=?",
                aiRawJson, status, errorMessage, itemId, draftId);
        jdbcTemplate.update(
                "INSERT INTO hou_base_ai_import_result(draft_id,file_id,function_key,object_key,ai_fields_json,status,error_message,create_time,update_time) "
                        + "VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) "
                        + "ON DUPLICATE KEY UPDATE function_key=VALUES(function_key),object_key=VALUES(object_key),"
                        + "ai_fields_json=VALUES(ai_fields_json),status=VALUES(status),error_message=VALUES(error_message),update_time=CURRENT_TIMESTAMP",
                draftId, itemId, functionKey, objectKey, aiFieldsJson, status, errorMessage);
        jdbcTemplate.update("UPDATE hou_base_ai_import_draft SET status='preview',update_time=CURRENT_TIMESTAMP WHERE id=?", draftId);
    }

    @Override
    public Map<String, Object> findConfirmItem(Long itemId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT f.id AS itemId,f.draft_id AS draftId,d.draft_no AS draftNo,d.user_id AS userId,d.org_id AS orgId,"
                        + "d.system_key AS systemKey,f.file_name AS fileName,f.file_url AS fileUrl,f.file_size AS fileSize,"
                        + "f.content_type AS contentType,r.function_key AS functionKey,r.object_key AS objectKey,"
                        + "r.ai_fields_json AS aiFieldsJson,r.edited_fields_json AS editedFieldsJson,r.status "
                        + "FROM hou_base_ai_import_file f JOIN hou_base_ai_import_draft d ON d.id=f.draft_id "
                        + "LEFT JOIN hou_base_ai_import_result r ON r.file_id=f.id WHERE f.id=?", itemId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Override
    public void completeItem(Long draftId, Long itemId, String status, String businessRecordId,
                             String detailUrl, String errorMessage) {
        jdbcTemplate.update("UPDATE hou_base_ai_import_result SET status=?,business_record_id=?,detail_url=?,error_message=?,update_time=CURRENT_TIMESTAMP WHERE file_id=? AND draft_id=?",
                status, businessRecordId, detailUrl, errorMessage, itemId, draftId);
        jdbcTemplate.update("UPDATE hou_base_ai_import_file SET status=?,error_message=?,update_time=CURRENT_TIMESTAMP WHERE id=? AND draft_id=?",
                status, errorMessage, itemId, draftId);
        jdbcTemplate.update("UPDATE hou_base_ai_import_draft d SET status=CASE WHEN NOT EXISTS ("
                        + "SELECT 1 FROM hou_base_ai_import_result r WHERE r.draft_id=d.id AND r.status<>'success'"
                        + ") THEN 'completed' ELSE 'preview' END,d.update_time=CURRENT_TIMESTAMP WHERE d.id=?", draftId);
    }

    @Override
    public void saveAttachment(Long draftId, Long itemId, String objectKey, String businessRecordId,
                               String fileName, String fileUrl, String status, String errorMessage) {
        jdbcTemplate.update("INSERT INTO hou_base_ai_import_attachment(draft_id,file_id,object_key,business_record_id,file_name,file_url,status,error_message,create_time,update_time) "
                        + "SELECT ?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP FROM DUAL WHERE NOT EXISTS ("
                        + "SELECT 1 FROM hou_base_ai_import_attachment WHERE file_id=? AND COALESCE(business_record_id,'')=COALESCE(?,'') AND file_url=?)",
                draftId, itemId, objectKey, businessRecordId, fileName, fileUrl, status, errorMessage,
                itemId, businessRecordId, fileUrl);
    }

    private static void bind(PreparedStatement ps, Map<String, Object> values, String... keys) throws java.sql.SQLException {
        for (int index = 0; index < keys.length; index++) ps.setObject(index + 1, values.get(keys[index]));
    }

    private static long key(KeyHolder holder) {
        Number key = holder.getKey();
        return key == null ? 0L : key.longValue();
    }
}
