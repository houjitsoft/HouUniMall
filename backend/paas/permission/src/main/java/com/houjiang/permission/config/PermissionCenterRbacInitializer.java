package com.houjiang.permission.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 启动时幂等修复权限中心菜单树，确保可用管理员拥有完整权限中心权限，并把管理员角色漏绑的菜单补齐。
 */
@Slf4j
@Component
public class PermissionCenterRbacInitializer implements ApplicationRunner {

    private static final String ROOT_PERMISSION = "shell:setting:permission";
    private static final String CHILD_PERMISSION_PATTERN = "shell:setting:permission:%";

    /**
     * 视为「平台管理员」的角色名（大小写与首尾空白不敏感）。
     *
     * <p>补绑与校验<b>必须用同一份口径</b>：补绑按它决定给谁加菜单，校验按它决定谁是合格管理员。
     * 两处各写一份字面量迟早会分叉（改了一处忘了另一处，会出现「补绑给了 A、校验只认 B」
     * 从而启动直接失败），因此统一由本常量经 {@link #ADMIN_ROLE_SQL_IN} 注入 SQL。
     */
    private static final List<String> ADMIN_ROLE_NAMES =
            List.of("admin", "super_admin", "管理员", "超级管理员");

    /**
     * {@link #ADMIN_ROLE_NAMES} 的 SQL 字面量形式（{@code 'admin', 'super_admin', …}）。
     * 取自类常量而非外部输入，不存在注入面。
     */
    private static final String ADMIN_ROLE_SQL_IN = ADMIN_ROLE_NAMES.stream()
            .map(name -> "'" + name + "'")
            .collect(Collectors.joining(", "));

    private static final Map<String, String> EXPECTED_PARENTS = expectedParents();

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PermissionCenterRbacInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(ApplicationArguments args) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));
        transactionTemplate.executeWithoutResult(status -> {
            verifyUniquePermissionCodes();
            executeMigration();
            // 迁移脚本可能刚插入新菜单（如权限中心新增子项），补绑必须排在它之后。
            backfillAdministratorRoleMenus();
            verifyUniquePermissionCodes();
            verifyRequiredPermissionCodes();
            verifyParentLinks();
            verifyUsableAdministrator();
        });
    }

    /**
     * 把管理员角色漏绑的菜单补上（幂等、只增不删）。
     *
     * <p><b>为什么需要</b>：新建菜单不会自动写进 {@code hou_permission_role_menu} —— 无论是后台
     * 加菜单、插件安装注入菜单，还是补数据脚本插入菜单。结果是管理员登录后看不到、也进不去新页面。
     * 此前靠一次性全量补绑 SQL 处理过（1360 条），但那是手工动作，下一个新菜单照样会漏；
     * 这里改成每次启动幂等补齐，把「漏绑」从需要人记得运维的事变成系统自己收敛的事。
     *
     * <p><b>范围是全部菜单</b>而不是仅权限中心子树：漏绑问题对所有模块都存在，只管权限中心等于
     * 只修了一半。<b>只动管理员角色</b>（{@link #ADMIN_ROLE_NAMES}）——其他角色的授权一律不动，
     * 它们是人工配置的，系统自动扩权会越权。
     *
     * <p><b>为什么不删</b>：管理员角色历史上可能被手工收窄过（去掉个别菜单）。自动补绑只能让
     * 权限变宽不能变窄；要收窄请手工操作。这样不会出现「重启一次权限被改回去」的意外。
     */
    private void backfillAdministratorRoleMenus() {
        int inserted = jdbcTemplate.update("""
                INSERT INTO hou_permission_role_menu (role_id, menu_id)
                SELECT r.id, m.id
                FROM hou_permission_role r
                CROSS JOIN hou_permission_menu m
                WHERE r.delete_time IS NULL
                  AND LOWER(TRIM(r.name)) IN (%s)
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hou_permission_role_menu existing
                      WHERE existing.role_id = r.id
                        AND existing.menu_id = m.id
                  )
                """.formatted(ADMIN_ROLE_SQL_IN));
        if (inserted > 0) {
            log.info("管理员角色菜单补绑完成：新增 {} 条授权（新建菜单此前不会自动进角色菜单表）", inserted);
        }
    }

    private static final List<String> REQUIRED_PERMISSION_CODES = List.of(
            ROOT_PERMISSION,
            "shell:setting:permission:menu",
            "shell:setting:permission:menu:add",
            "shell:setting:permission:menu:edit",
            "shell:setting:permission:menu:delete",
            "shell:setting:permission:menu:enable",
            "shell:setting:permission:menu:disable",
            "shell:setting:permission:role",
            "shell:setting:permission:role:add",
            "shell:setting:permission:role:edit",
            "shell:setting:permission:role:delete",
            "shell:setting:permission:role:assign",
            "shell:setting:permission:user",
            "shell:setting:permission:user:add",
            "shell:setting:permission:user:edit",
            "shell:setting:permission:user:delete",
            "shell:setting:permission:user:reset",
            "shell:setting:permission:user:assign",
            "shell:setting:permission:user:enable",
            "shell:setting:permission:user:disable",
            "shell:setting:permission:cache:clear"
    );

    private void executeMigration() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/permission-center-rbac.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }

    private void verifyUniquePermissionCodes() {
        List<String> duplicates = jdbcTemplate.query("""
                SELECT perms, COUNT(*) AS duplicate_count
                FROM hou_permission_menu
                WHERE perms = ? OR perms LIKE ?
                GROUP BY perms
                HAVING COUNT(*) > 1
                """, (resultSet, rowNumber) -> resultSet.getString("perms"),
                ROOT_PERMISSION, CHILD_PERMISSION_PATTERN);
        if (!duplicates.isEmpty()) {
            String duplicateCodes = duplicates.stream().sorted().toList().toString();
            throw new IllegalStateException("权限中心存在重复权限码，拒绝自动选择记录: " + duplicateCodes);
        }
    }

    private void verifyRequiredPermissionCodes() {
        for (String permissionCode : REQUIRED_PERMISSION_CODES) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hou_permission_menu WHERE perms = ?",
                    Integer.class,
                    permissionCode);
            if (count == null || count != 1) {
                throw new IllegalStateException("权限中心必需权限码缺失: " + permissionCode);
            }
        }
    }

    private void verifyParentLinks() {
        for (Map.Entry<String, String> link : EXPECTED_PARENTS.entrySet()) {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM hou_permission_menu child
                    JOIN hou_permission_menu parent ON parent.id = child.pid
                    WHERE child.perms = ? AND parent.perms = ?
                    """, Integer.class, link.getKey(), link.getValue());
            if (count == null || count != 1) {
                throw new IllegalStateException(
                        "权限中心父子关系错误: " + link.getKey() + " -> " + link.getValue());
            }
        }

        verifyType(ROOT_PERMISSION, "M");
        verifyType("shell:setting:permission:menu", "C");
        verifyType("shell:setting:permission:role", "C");
        verifyType("shell:setting:permission:user", "C");
        REQUIRED_PERMISSION_CODES.stream()
                .filter(code -> !ROOT_PERMISSION.equals(code))
                .filter(code -> !code.equals("shell:setting:permission:menu"))
                .filter(code -> !code.equals("shell:setting:permission:role"))
                .filter(code -> !code.equals("shell:setting:permission:user"))
                .forEach(code -> verifyType(code, "A"));
    }

    private void verifyType(String permissionCode, String expectedType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hou_permission_menu WHERE perms = ? AND type = ?",
                Integer.class,
                permissionCode,
                expectedType);
        if (count == null || count != 1) {
            throw new IllegalStateException(
                    "权限中心节点类型错误: " + permissionCode + " 应为 " + expectedType);
        }
    }

    private void verifyUsableAdministrator() {
        // 两类账号任存其一即可：
        // ① is_admin=1 的超管 —— auth 对无角色超管兜底 permissions=["*"]，天然覆盖权限中心子树，
        //    这是 seed 的固定形态（超管有意不绑 RBAC 角色，见 99-seed.sql 注释），新装环境只有这类账号；
        // ② 绑定管理员角色且角色菜单覆盖权限中心子树的启用账号 —— 精确比对形态。
        // 若只认 ②，2026-10-29 起 seed 不再给超管绑角色，全新初始化的环境会启动失败（crash loop）。
        Integer superAdmins = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM hou_user_info u
                WHERE u.is_delete = 0
                  AND u.user_state IN (1, 2)
                  AND u.is_admin = 1
                """, Integer.class);
        if (superAdmins != null && superAdmins > 0) {
            return;
        }
        Integer usableAdministrators = jdbcTemplate.queryForObject("""
                SELECT COUNT(DISTINCT u.user_id)
                FROM hou_user_info u
                JOIN hou_permission_user_role ur ON ur.user_id = u.user_id
                JOIN hou_permission_role r ON r.id = ur.role_id
                WHERE u.is_delete = 0
                  AND u.user_state IN (1, 2)
                  AND r.delete_time IS NULL
                  AND LOWER(TRIM(r.name)) IN (%s)
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hou_permission_menu required_menu
                      WHERE (required_menu.perms = ? OR required_menu.perms LIKE ?)
                        AND NOT EXISTS (
                            SELECT 1
                            FROM hou_permission_user_role user_role
                            JOIN hou_permission_role_menu role_menu
                              ON role_menu.role_id = user_role.role_id
                            WHERE user_role.user_id = u.user_id
                              AND role_menu.menu_id = required_menu.id
                        )
                  )
                """.formatted(ADMIN_ROLE_SQL_IN), Integer.class, ROOT_PERMISSION, CHILD_PERMISSION_PATTERN);
        if (usableAdministrators == null || usableAdministrators < 1) {
            throw new IllegalStateException("没有启用且拥有完整权限中心权限的管理员账号");
        }
    }

    private static Map<String, String> expectedParents() {
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(ROOT_PERMISSION, "shell:setting");
        parents.put("shell:setting:permission:menu", ROOT_PERMISSION);
        parents.put("shell:setting:permission:role", ROOT_PERMISSION);
        parents.put("shell:setting:permission:user", ROOT_PERMISSION);
        parents.put("shell:setting:permission:cache:clear", ROOT_PERMISSION);
        for (String action : List.of("add", "edit", "delete", "enable", "disable")) {
            parents.put("shell:setting:permission:menu:" + action,
                    "shell:setting:permission:menu");
        }
        for (String action : List.of("add", "edit", "delete", "assign")) {
            parents.put("shell:setting:permission:role:" + action,
                    "shell:setting:permission:role");
        }
        for (String action : List.of("add", "edit", "delete", "reset", "assign", "enable", "disable")) {
            parents.put("shell:setting:permission:user:" + action,
                    "shell:setting:permission:user");
        }
        return Map.copyOf(parents);
    }
}
