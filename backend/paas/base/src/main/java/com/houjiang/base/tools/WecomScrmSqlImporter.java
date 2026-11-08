package com.houjiang.base.tools;

import com.houjiang.base.util.SqlScriptRunner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.Map;

/**
 * 命令行导入企微 SCRM SQL 补丁（不启动 Spring 容器）
 *
 * <pre>
 *   cd backend/paas/base
 *   mvn -q compile exec:java -Dexec.mainClass=com.houjiang.base.tools.WecomScrmSqlImporter
 * </pre>
 * @author Amos
 */
public class WecomScrmSqlImporter {

    private static final String PATCH_DIR = "D:/wwwroot/houcloud/docs/sql/patches";
    private static final String[] FILES = {
            "hou_oa_wecom_scrm_extend.sql",
            "permission_wecom_menus_extend.sql"
    };

    public static void localMain(String[] args) throws Exception {
        String host = env("DB_HOST", "127.0.0.1");
        String port = env("DB_PORT", "3306");
        String db = env("DB_NAME", "meoc");
        String user = env("DB_USER", "meoc");
        String pass = env("DB_PASS", "");  // 口令由环境变量 DB_PASS 注入，禁止在源码中内置默认值

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(2);

        try (HikariDataSource ds = new HikariDataSource(config)) {
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            for (String fileName : FILES) {
                File file = new File(PATCH_DIR, fileName);
                if (!file.exists()) {
                    System.err.println("[SKIP] 文件不存在: " + file.getAbsolutePath());
                    continue;
                }
                System.out.println("[RUN] " + fileName);
                Map<String, Object> result = SqlScriptRunner.executeFile(jdbc, file, true);
                System.out.println(result);
            }
            System.out.println("[DONE] 企微 SCRM SQL 导入完成");
        }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? def : v;
    }
}
