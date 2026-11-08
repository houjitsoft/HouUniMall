package com.houjiang.merchant.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/** Idempotently owns the bcenter advertising menu row created by plugin lifecycle calls. */
@Service
public class AdvertisingMenuLifecycleService {

    public static final String MENU_ACCESS = "advertising/applications:list:save:submit:withdraw";
    public static final String MENU_COMPONENT = "plugin:mall-advertising/merchant-applications";

    private final MenuGateway gateway;

    @Autowired
    public AdvertisingMenuLifecycleService(JdbcTemplate jdbcTemplate) {
        this(new JdbcMenuGateway(jdbcTemplate));
    }

    public AdvertisingMenuLifecycleService(MenuGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public int sync() {
        MenuRecord parent = gateway.lockMarketingParent();
        if (parent == null) {
            throw new IllegalStateException("未找到商家端营销/运营父菜单");
        }
        long now = Instant.now().getEpochSecond();
        MenuRecord current = gateway.findAdvertisingMenu();
        MenuRecord desired = new MenuRecord(
                current == null ? null : current.id(), parent.id(), 1, "广告投放", "Promotion",
                "advertising", 80, MENU_COMPONENT, "", "", MENU_ACCESS,
                0, 1, 1, current == null || current.createTime() == null ? now : current.createTime(), now, 0L);
        if (current == null) {
            return gateway.insert(desired);
        }
        gateway.update(desired);
        return desired.id();
    }

    @Transactional(rollbackFor = Exception.class)
    public void disable() {
        changeVisibility(false, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove() {
        changeVisibility(false, true);
    }

    private void changeVisibility(boolean visible, boolean deleted) {
        MenuRecord current = gateway.findAdvertisingMenu();
        if (current == null) return;
        long now = Instant.now().getEpochSecond();
        gateway.update(new MenuRecord(
                current.id(), current.pid(), current.type(), current.name(), current.icon(), current.route(),
                current.sort(), current.component(), current.selected(), current.routeParams(), current.access(),
                current.isCache(), visible ? 1 : 0, visible ? 1 : 0,
                current.createTime(), now, deleted ? now : 0L));
    }

    public interface MenuGateway {
        MenuRecord lockMarketingParent();

        MenuRecord findAdvertisingMenu();

        int insert(MenuRecord menu);

        void update(MenuRecord menu);
    }

    public record MenuRecord(
            Integer id, Integer pid, Integer type, String name, String icon, String route, Integer sort,
            String component, String selected, String routeParams, String access, Integer isCache,
            Integer status, Integer showStatus, Long createTime, Long updateTime, Long deleteTime) {
        public MenuRecord withId(Integer value) {
            return new MenuRecord(value, pid, type, name, icon, route, sort, component, selected, routeParams,
                    access, isCache, status, showStatus, createTime, updateTime, deleteTime);
        }
    }

    static final class JdbcMenuGateway implements MenuGateway {
        private static final String COLUMNS = "id,pid,type,name,icon,route,sort,component,selected,route_params," +
                "access,is_cache,status,show_status,create_time,update_time,delete_time";
        private final JdbcTemplate jdbc;

        JdbcMenuGateway(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        @Override
        public MenuRecord lockMarketingParent() {
            List<MenuRecord> rows = jdbc.query("SELECT " + COLUMNS + " FROM hou_merchant_mall_shop_menu " +
                            "WHERE delete_time=0 AND status=1 AND type=0 AND (" +
                            "route IN ('marketing','operation','marketing-operation') OR " +
                            "access IN ('marketing','marketing/index','operation','operation/index') OR " +
                            "name IN ('营销管理','营销中心','营销/运营','运营管理')) " +
                            "ORDER BY CASE WHEN route='marketing' THEN 0 WHEN name='营销管理' THEN 1 ELSE 2 END,id " +
                            "LIMIT 1 FOR UPDATE",
                    JdbcMenuGateway::map);
            return rows.isEmpty() ? null : rows.getFirst();
        }

        @Override
        public MenuRecord findAdvertisingMenu() {
            List<MenuRecord> rows = jdbc.query("SELECT " + COLUMNS + " FROM hou_merchant_mall_shop_menu " +
                            "WHERE access=? ORDER BY id LIMIT 1",
                    JdbcMenuGateway::map, MENU_ACCESS);
            return rows.isEmpty() ? null : rows.getFirst();
        }

        @Override
        public int insert(MenuRecord menu) {
            jdbc.update("INSERT INTO hou_merchant_mall_shop_menu " +
                            "(pid,type,name,icon,route,sort,component,selected,route_params,access,is_cache,status," +
                            "show_status,create_time,update_time,delete_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    menu.pid(), menu.type(), menu.name(), menu.icon(), menu.route(), menu.sort(), menu.component(),
                    menu.selected(), menu.routeParams(), menu.access(), menu.isCache(), menu.status(),
                    menu.showStatus(), menu.createTime(), menu.updateTime(), menu.deleteTime());
            MenuRecord inserted = findAdvertisingMenu();
            if (inserted == null) throw new IllegalStateException("广告投放菜单写入失败");
            return inserted.id();
        }

        @Override
        public void update(MenuRecord menu) {
            int updated = jdbc.update("UPDATE hou_merchant_mall_shop_menu SET pid=?,type=?,name=?,icon=?,route=?," +
                            "sort=?,component=?,selected=?,route_params=?,access=?,is_cache=?,status=?,show_status=?," +
                            "create_time=?,update_time=?,delete_time=? WHERE id=?",
                    menu.pid(), menu.type(), menu.name(), menu.icon(), menu.route(), menu.sort(), menu.component(),
                    menu.selected(), menu.routeParams(), menu.access(), menu.isCache(), menu.status(),
                    menu.showStatus(), menu.createTime(), menu.updateTime(), menu.deleteTime(), menu.id());
            if (updated != 1) throw new IllegalStateException("广告投放菜单更新失败");
        }

        private static MenuRecord map(ResultSet result, int rowNum) throws SQLException {
            return new MenuRecord(result.getInt("id"), result.getInt("pid"), result.getInt("type"),
                    result.getString("name"), result.getString("icon"), result.getString("route"),
                    result.getInt("sort"), result.getString("component"), result.getString("selected"),
                    result.getString("route_params"), result.getString("access"), result.getInt("is_cache"),
                    result.getInt("status"), result.getInt("show_status"), result.getLong("create_time"),
                    result.getLong("update_time"), result.getLong("delete_time"));
        }
    }
}
