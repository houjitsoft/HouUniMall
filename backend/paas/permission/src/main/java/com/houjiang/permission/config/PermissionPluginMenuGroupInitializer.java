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
 * 仅做启动自检：校验「插件中心」存在且可用（一级、权限标识正确、未禁用），并报告残留的历史分组根。
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

    /**
     * 校验「插件中心」存在、是一级、权限标识正确且未被禁用；缺失只告警不中断启动。
     *
     * <p><b>为什么不再校验 display_pos=left</b>：2026-10-28 的菜单铁律
     * （{@code deploy/scripts/sql/init/140-menu-ironrule.sql}）已明确把 id=4 插件中心
     * 移出左栏（{@code display_pos=hidden}），入口改到右上角「插件应用」面板底部
     * 「前往插件中心」，生产库实况同为 hidden。旧自检仍要求 left，于是每次启动都刷一条
     * 「核心管理入口缺失或异常」的 ERROR——数据是对的，判据是旧的。
     */
    private void verifyPluginCenter() {
        List<String> rows = jdbcTemplate.queryForList("""
                SELECT display_pos FROM hou_permission_menu
                WHERE id = ? AND pid = 0 AND perms = ? AND is_disable = 0
                """, String.class, PLUGIN_CENTER_MENU_ID, PLUGIN_CENTER_PERMS);
        if (rows.size() == 1) {
            log.info("核心管理入口「插件中心」校验通过: id={} perms={} display_pos={}",
                    PLUGIN_CENTER_MENU_ID, PLUGIN_CENTER_PERMS, rows.get(0));
            return;
        }
        log.error("核心管理入口「插件中心」缺失或异常（期望 id={} perms={} pid=0 且未禁用），"
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
