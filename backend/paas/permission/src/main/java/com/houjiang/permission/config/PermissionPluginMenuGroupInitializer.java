package com.houjiang.permission.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * 启动自检：核心插件管理入口与退役分组。
 *
 * <p><b>2026-09-20 菜单重构后行为变更</b>：插件一级菜单分组根（{@code shell:group:*}）已退役，
 * 所有插件应用保持 pid=0、display_pos=plugin，分类只用于右上角应用面板。因而本类
 * <b>不再执行分组根种子 SQL</b>（否则每次重启都会把已退役的分组根插回库里），
 * 仅做启动自检：校验「插件中心」存在且左栏可见，并报告残留的历史分组根。
 *
 * <p>插件中心只提供市场和管理入口，不是插件应用的父节点。
 * 自检缺失只记 ERROR 不中断启动——权限菜单以库为准，避免一次数据异常导致整个权限服务起不来。
 *
 * @author Amos
 */
@Component
public class PermissionPluginMenuGroupInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PermissionPluginMenuGroupInitializer.class);

    /** 「插件中心」核心管理入口 id。 */
    private static final int PLUGIN_CENTER_MENU_ID = 4;

    /** 「插件中心」权限标识。 */
    private static final String PLUGIN_CENTER_PERMS = "shell:addons";

    /** 已退役的历史分组根 perms，仅用于报告残留，不再 seed。 */
    private static final List<String> LEGACY_GROUP_PERMS = List.of(
            "shell:group:biz", "shell:group:growth", "shell:group:org",
            "shell:group:fin", "shell:group:ai", "shell:group:other");

    private final JdbcTemplate jdbcTemplate;

    public PermissionPluginMenuGroupInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(ApplicationArguments args) {
        verifyPluginCenter();
        reportLegacyGroupRoots();
    }

    /** 校验「插件中心」存在、是一级且左栏可见；缺失只告警不中断启动。 */
    private void verifyPluginCenter() {
        Integer rows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM hou_permission_menu
                WHERE id = ? AND pid = 0 AND perms = ? AND display_pos = 'left'
                """, Integer.class, PLUGIN_CENTER_MENU_ID, PLUGIN_CENTER_PERMS);
        if (rows != null && rows == 1) {
            log.info("核心管理入口「插件中心」校验通过: id={} perms={}", PLUGIN_CENTER_MENU_ID, PLUGIN_CENTER_PERMS);
            return;
        }
        log.error("核心管理入口「插件中心」缺失或异常（期望 id={} perms={} display_pos=left），"
                        + "请检查 hou_permission_menu 是否执行过菜单重构迁移",
                PLUGIN_CENTER_MENU_ID, PLUGIN_CENTER_PERMS);
    }

    /** 报告残留的历史分组根：它们不会再被 seed，但库里若还有需人工清理。 */
    private void reportLegacyGroupRoots() {
        Integer left = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hou_permission_menu WHERE params = 'host:menu-group'",
                Integer.class);
        if (left != null && left > 0) {
            log.warn("库中仍有 {} 条已退役的插件菜单分组根（{}），建议清理；"
                            + "确认无子菜单后可执行 DELETE FROM hou_permission_menu WHERE params='host:menu-group'",
                    left, String.join(", ", LEGACY_GROUP_PERMS));
            return;
        }
        log.info("无残留的插件菜单分组根");
    }
}
