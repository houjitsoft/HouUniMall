package com.houjiang.plugin.sdk.api;

import java.util.List;
import java.util.Map;

/**
 * 插件受限数据访问。
 *
 * <p>插件<strong>不允许</strong>自行定义 <code>DataSource</code> 或使用 <code>@MapperScan</code>；
 * 只能通过本接口访问数据库，且表名会被宿主强制校验为 <code>hou_plugin_{code}_</code> 前缀，
 * 越权访问会抛出 {@link com.houjiang.plugin.sdk.exception.PluginException}。
 *
 * <p>实现由 <code>plugin-engine</code> 提供，插件通过
 * {@code context.getService(PluginDao.class)} 获取。
 *
 * @author HouCloud
 */
public interface PluginDao {

    /**
     * 新增记录。
     *
     * @param table 表名（可省略 <code>hou_plugin_{code}_</code> 前缀，宿主自动补全）
     * @param row   字段名 → 值
     * @return 影响行数
     */
    int insert(String table, Map<String, Object> row);

    /**
     * 按主键更新。
     *
     * @param table 表名
     * @param id    主键值
     * @param row   待更新字段
     * @return 影响行数
     */
    int updateById(String table, Object id, Map<String, Object> row);

    /**
     * 按主键删除（软删除，写入 deleted 标记）。
     */
    int deleteById(String table, Object id);

    /**
     * 按主键查询。
     */
    Map<String, Object> selectById(String table, Object id);

    /**
     * 按条件查询。
     *
     * @param condition 等值条件，字段名 → 值
     */
    List<Map<String, Object>> selectList(String table, Map<String, Object> condition);

    /**
     * 分页查询。
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数
     */
    List<Map<String, Object>> selectPage(String table, Map<String, Object> condition, long current, long size);

    /**
     * 计数。
     */
    long count(String table, Map<String, Object> condition);

    /**
     * 执行插件自带的 SELECT 语句；表名仍需满足前缀校验。
     */
    List<Map<String, Object>> selectBySql(String sql, Object... args);

    /**
     * 执行插件自带的写语句；禁止 DDL，禁止操作非本插件前缀的表。
     */
    int execute(String sql, Object... args);
}
