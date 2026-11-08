package com.houjiang.permission.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionCenterRbacInitializerTest {

    private static final List<String> REQUIRED_PERMISSION_CODES = List.of(
            "shell:setting:permission",
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

    private JdbcTemplate jdbc;
    private PermissionCenterRbacInitializer initializer;

    @BeforeEach
    void setUp() {
        DataSource dataSource = newDataSource();
        jdbc = new JdbcTemplate(dataSource);
        initializer = new PermissionCenterRbacInitializer(dataSource);
        createSchema();
        seedLegacyPermissionData();
    }

    @Test
    void migratesHierarchyAndNamedAdministratorIdempotently() {
        initializer.run(null);
        initializer.run(null);

        for (String permissionCode : REQUIRED_PERMISSION_CODES) {
            assertEquals(1, countMenu(permissionCode), permissionCode);
        }
        assertEquals(menuId("shell:setting"), parentId("shell:setting:permission"));
        assertEquals(menuId("shell:setting:permission"), parentId("shell:setting:permission:user"));
        assertEquals(menuId("shell:setting:permission:user"),
                parentId("shell:setting:permission:user:add"));
        assertEquals(menuId("shell:setting:permission:user"),
                parentId("shell:setting:permission:user:assign"));
        assertEquals(menuId("shell:setting:permission"),
                parentId("shell:setting:permission:cache:clear"));

        assertEquals(0, duplicatePermissionCodeCount());
        assertEquals(permissionCenterMenuCount(), rolePermissionCount(42));
        assertEquals(1, rolePermissionCount(1), "普通角色不能自动获得新增权限");
    }

    @Test
    void rejectsDuplicatePermissionCodesBeforeChangingData() {
        insertMenu(10003, 18, "M", "重复权限管理", "shell:setting:permission");

        assertThrows(IllegalStateException.class, () -> initializer.run(null));

        assertEquals(0, countMenu("shell:setting:permission:user"));
        assertEquals(1, rolePermissionCount(42));
    }

    @Test
    void rollsBackWhenNoEnabledAdministratorCanUseThePermissions() {
        jdbc.update("UPDATE hou_user_info SET user_state = 3 WHERE user_id = 7001");

        assertThrows(IllegalStateException.class, () -> initializer.run(null));

        assertEquals(0, countMenu("shell:setting:permission:user"));
        assertEquals(1, rolePermissionCount(42));
    }

    private DataSource newDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:permission_rbac_"
                + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void createSchema() {
        jdbc.execute("""
                CREATE TABLE hou_permission_menu (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    pid INT NOT NULL DEFAULT 0,
                    type VARCHAR(2) NOT NULL DEFAULT '',
                    name VARCHAR(100) NOT NULL DEFAULT '',
                    icon VARCHAR(100) NOT NULL DEFAULT '',
                    sort SMALLINT NOT NULL DEFAULT 0,
                    perms VARCHAR(100) NOT NULL DEFAULT '',
                    paths VARCHAR(100) NOT NULL DEFAULT '',
                    component VARCHAR(200) NOT NULL DEFAULT '',
                    selected VARCHAR(200) NOT NULL DEFAULT '',
                    params VARCHAR(200) NOT NULL DEFAULT '',
                    is_cache TINYINT NOT NULL DEFAULT 0,
                    is_show TINYINT NOT NULL DEFAULT 1,
                    is_disable TINYINT NOT NULL DEFAULT 0,
                    create_time INT NOT NULL DEFAULT 0,
                    update_time INT NOT NULL DEFAULT 0
                )
                """);
        jdbc.execute("""
                CREATE TABLE hou_permission_role (
                    id INT PRIMARY KEY,
                    name VARCHAR(32) NOT NULL,
                    delete_time INT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE hou_permission_role_menu (
                    role_id INT NOT NULL,
                    menu_id INT NOT NULL,
                    PRIMARY KEY (role_id, menu_id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE hou_permission_user_role (
                    user_id BIGINT NOT NULL,
                    role_id INT NOT NULL,
                    PRIMARY KEY (user_id, role_id)
                )
                """);
        jdbc.execute("""
                CREATE TABLE hou_user_info (
                    user_id BIGINT PRIMARY KEY,
                    user_state TINYINT NOT NULL,
                    is_delete TINYINT NOT NULL DEFAULT 0
                )
                """);
    }

    private void seedLegacyPermissionData() {
        insertMenu(18, 0, "M", "系统设置", "shell:setting");
        insertMenu(10002, 18, "M", "权限管理", "shell:setting:permission");
        insertMenu(11007, 10002, "C", "菜单管理", "shell:setting:permission:menu");
        insertMenu(11008, 10002, "C", "角色管理", "shell:setting:permission:role");
        insertMenu(11009, 18, "M", "三方对接", "shell:setting:thridparty");
        insertMenu(13022, 11009, "A", "新增用户", "shell:setting:permission:user:add");
        insertMenu(13028, 11009, "A", "分配角色", "shell:setting:permission:user:assign");

        jdbc.update("INSERT INTO hou_permission_role (id, name, delete_time) VALUES (1, '普通用户', NULL)");
        jdbc.update("INSERT INTO hou_permission_role (id, name, delete_time) VALUES (42, ' 管理员 ', NULL)");
        jdbc.update("INSERT INTO hou_user_info (user_id, user_state, is_delete) VALUES (7001, 2, 0)");
        jdbc.update("INSERT INTO hou_user_info (user_id, user_state, is_delete) VALUES (7002, 2, 0)");
        jdbc.update("INSERT INTO hou_permission_user_role (user_id, role_id) VALUES (7001, 42)");
        jdbc.update("INSERT INTO hou_permission_user_role (user_id, role_id) VALUES (7002, 1)");
        jdbc.update("INSERT INTO hou_permission_role_menu (role_id, menu_id) VALUES (42, 10002)");
        jdbc.update("INSERT INTO hou_permission_role_menu (role_id, menu_id) VALUES (1, 10002)");
    }

    private void insertMenu(int id, int pid, String type, String name, String permissionCode) {
        jdbc.update("""
                INSERT INTO hou_permission_menu
                    (id, pid, type, name, icon, sort, perms, paths, component, selected, params,
                     is_cache, is_show, is_disable, create_time, update_time)
                VALUES (?, ?, ?, ?, '', 0, ?, '', '', '', '', 0, 1, 0, 1, 1)
                """, id, pid, type, name, permissionCode);
    }

    private int countMenu(String permissionCode) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM hou_permission_menu WHERE perms = ?",
                Integer.class,
                permissionCode);
    }

    private int menuId(String permissionCode) {
        return jdbc.queryForObject(
                "SELECT id FROM hou_permission_menu WHERE perms = ?",
                Integer.class,
                permissionCode);
    }

    private int parentId(String permissionCode) {
        return jdbc.queryForObject(
                "SELECT pid FROM hou_permission_menu WHERE perms = ?",
                Integer.class,
                permissionCode);
    }

    private int duplicatePermissionCodeCount() {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT perms
                    FROM hou_permission_menu
                    WHERE perms = 'shell:setting:permission'
                       OR perms LIKE 'shell:setting:permission:%'
                    GROUP BY perms
                    HAVING COUNT(*) > 1
                ) duplicated
                """, Integer.class);
    }

    private int permissionCenterMenuCount() {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM hou_permission_menu
                WHERE perms = 'shell:setting:permission'
                   OR perms LIKE 'shell:setting:permission:%'
                """, Integer.class);
    }

    private int rolePermissionCount(int roleId) {
        return jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM hou_permission_role_menu rm
                JOIN hou_permission_menu m ON m.id = rm.menu_id
                WHERE rm.role_id = ?
                  AND (m.perms = 'shell:setting:permission'
                       OR m.perms LIKE 'shell:setting:permission:%')
                """, Integer.class, roleId);
    }
}
