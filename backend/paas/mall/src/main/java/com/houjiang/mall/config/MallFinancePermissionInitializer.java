package com.houjiang.mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 补齐财务按钮权限，并让已有财务页面权限的角色自动继承对应操作权限。
 *
 * <p>Mall 有一套独立菜单表，Cloud 权限服务也有一套菜单表。只更新 SQL 快照
 * 会导致新环境或独立商城管理员拿不到按钮权限，因此这里做幂等初始化。</p>
 */
@Slf4j
@Component
public class MallFinancePermissionInitializer {

    private static final List<ActionPermission> ACTIONS = List.of(
            new ActionPermission("mall:finance:refund:retry", "重新退款", "mall:finance:refund"),
            new ActionPermission("mall:finance:shop_settle:settle", "执行结算", "mall:finance:shop_settle"),
            new ActionPermission("mall:finance:shop_withdraw:audit", "审核店铺提现", "mall:finance:shop_withdraw"),
            new ActionPermission("mall:finance:user_withdraw:audit", "审核用户提现", "mall:finance:user_withdraw"),
            new ActionPermission("mall:finance:income:create", "新增收入", "mall:finance:mall"),
            new ActionPermission("mall:finance:income:confirm", "确认收入", "mall:finance:mall"),
            new ActionPermission("mall:finance:expense:create", "新增支出", "mall:finance:mall"),
            new ActionPermission("mall:finance:expense:confirm", "确认支出", "mall:finance:mall"),
            new ActionPermission("mall:finance:settlement:create", "新建结算", "mall:finance:shop_settle")
    );

    private final JdbcTemplate jdbcTemplate;

    public MallFinancePermissionInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensurePermissions() {
        try {
            for (ActionPermission action : ACTIONS) {
                ensureAction("hou_permission_menu", action);
                ensureAction("hou_mall_system_menu", action);
                inheritToCloudRoles(action);
                inheritToMallRoles(action);
            }
            log.info("商城财务按钮权限初始化完成，共 {} 项", ACTIONS.size());
        } catch (Exception e) {
            // 旧环境可能尚未迁移权限表，不能因此阻断商城启动；发布 SQL 可再次补齐。
            log.warn("商城财务按钮权限初始化失败，将由权限 SQL 补丁处理: {}", e.getMessage());
        }
    }

    private void ensureAction(String menuTable, ActionPermission action) {
        String sql = "INSERT INTO " + menuTable
                + " (pid, type, name, icon, sort, perms, paths, component, selected, params, "
                + "is_cache, is_show, is_disable, create_time, update_time) "
                + "SELECT parent.id, 'A', ?, '', 1, ?, '', '', '', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP() "
                + "FROM " + menuTable + " parent "
                + "WHERE parent.perms = ? "
                + "AND NOT EXISTS (SELECT 1 FROM " + menuTable + " existing WHERE existing.perms = ?)";
        jdbcTemplate.update(sql, action.name(), action.perms(), action.parentPerm(), action.perms());
    }

    private void inheritToCloudRoles(ActionPermission action) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO hou_permission_role_menu (role_id, menu_id) "
                        + "SELECT DISTINCT rm.role_id, action.id "
                        + "FROM hou_permission_role_menu rm "
                        + "JOIN hou_permission_menu parent ON parent.id = rm.menu_id "
                        + "JOIN hou_permission_menu action ON action.perms = ? "
                        + "WHERE parent.perms = ?",
                action.perms(), action.parentPerm());
    }

    private void inheritToMallRoles(ActionPermission action) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO hou_mall_system_role_menu (role_id, menu_id) "
                        + "SELECT DISTINCT rm.role_id, action.id "
                        + "FROM hou_mall_system_role_menu rm "
                        + "JOIN hou_mall_system_menu parent ON parent.id = rm.menu_id "
                        + "JOIN hou_mall_system_menu action ON action.perms = ? "
                        + "WHERE parent.perms = ?",
                action.perms(), action.parentPerm());
    }

    record ActionPermission(String perms, String name, String parentPerm) {
    }
}
