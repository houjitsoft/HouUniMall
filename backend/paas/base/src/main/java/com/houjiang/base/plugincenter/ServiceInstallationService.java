package com.houjiang.base.plugincenter;

import com.houjiang.plugin.artifact.delivery.CommercialReleaseService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/** Instance-bound task receipts. Old task queries must never overwrite a newer installation. */
@Service
public class ServiceInstallationService {
    private final JdbcTemplate jdbc;
    private final CommercialReleaseService releases;

    public ServiceInstallationService(JdbcTemplate jdbc, CommercialReleaseService releases) {
        this.jdbc = jdbc;
        this.releases = releases;
    }

    public void submitted(String instance, String code, String version, Map<?, ?> task) {
        String id = text(task, "taskId");
        if (!id.matches("[a-f0-9-]{36}") || !code.equals(text(task,"pluginCode")) || !version.equals(text(task,"version")))
            throw new IllegalStateException("安装代理返回了不匹配的任务");
        jdbc.update("INSERT IGNORE INTO hou_service_install_task(instance_id,task_id,plugin_code,version,state) VALUES(?,?,?,?,'PENDING')",
                instance,id,code,version);
    }

    public void requireTask(String instance, String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM hou_service_install_task WHERE instance_id=? AND task_id=?", Integer.class, instance,id);
        if (count == null || count != 1) throw new IllegalArgumentException("当前站点没有该安装任务");
    }

    public List<Map<String,Object>> pending(String instance) {
        return jdbc.queryForList("SELECT task_id FROM hou_service_install_task WHERE instance_id=? AND state IN ('PENDING','RUNNING') ORDER BY id LIMIT 100",instance);
    }

    @Transactional(rollbackFor=Exception.class)
    public void reconcile(String instance, String id, Map<?, ?> task) {
        var rows=jdbc.queryForList("SELECT id,plugin_code,version,state FROM hou_service_install_task WHERE instance_id=? AND task_id=? FOR UPDATE",instance,id);
        if (rows.size()!=1 || !id.equals(text(task,"taskId"))) throw new IllegalArgumentException("安装回执不属于当前站点任务");
        var stored=rows.get(0);
        if (List.of("SUCCESS","FAILED","ROLLED_BACK").contains(String.valueOf(stored.get("state")))) return;
        String state=text(task,"state"),code=text(task,"pluginCode"),version=text(task,"version");
        if (!code.equals(stored.get("plugin_code")) || !version.equals(stored.get("version"))
                || !List.of("PENDING","RUNNING","SUCCESS","FAILED","ROLLED_BACK").contains(state))
            throw new IllegalArgumentException("安装回执产品、版本或状态不匹配");
        if ("SUCCESS".equals(state)) {
            var manifest=releases.release(code,version).manifest();
            String image=text(task,"imageId"),source=text(task,"sourceSha256"),runtime=text(task,"runtimeSha256");
            if (!image.equals(manifest.path("imageId").asText()) || !source.equals(manifest.path("sourceSha256").asText())
                    || !runtime.equals(manifest.path("runtimeSha256").asText()))
                throw new IllegalArgumentException("安装成功回执与官方签名制品不一致");
            long sequence=((Number)stored.get("id")).longValue();
            jdbc.update("""
                    INSERT INTO hou_service_installation(instance_id,plugin_code,version,image_id,source_sha256,runtime_sha256,status,task_sequence,installed_at,updated_at)
                    VALUES(?,?,?,?,?,?,1,?,NOW(),NOW())
                    ON DUPLICATE KEY UPDATE
                        version=IF(task_sequence<VALUES(task_sequence),VALUES(version),version),
                        image_id=IF(task_sequence<VALUES(task_sequence),VALUES(image_id),image_id),
                        source_sha256=IF(task_sequence<VALUES(task_sequence),VALUES(source_sha256),source_sha256),
                        runtime_sha256=IF(task_sequence<VALUES(task_sequence),VALUES(runtime_sha256),runtime_sha256),
                        status=IF(task_sequence<VALUES(task_sequence),1,status),
                        updated_at=IF(task_sequence<VALUES(task_sequence),NOW(),updated_at),
                        task_sequence=GREATEST(task_sequence,VALUES(task_sequence))
                    """,instance,code,version,image,source,runtime,sequence);
        }
        jdbc.update("UPDATE hou_service_install_task SET state=?,updated_at=NOW() WHERE instance_id=? AND task_id=?",state,instance,id);
    }

    private static String text(Map<?,?> task,String key) {
        Object v=task.get(key);
        if(v==null || v.toString().isBlank())throw new IllegalArgumentException("安装回执缺少 "+key);
        return v.toString();
    }
}
