package com.houjiang.mall.mapper.plugin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 插件分类表（hou_addon_cate）只读查询。
 *
 * <p>分类数据由 base 独占；permission 通过插件中心 API 获取字典与更新插件分类。
 *
 * <p>字段说明：
 * <ul>
 *   <li>{@code id} —— 分类主键，菜单通过 hou_permission_menu.cate_id 引用它；</li>
 *   <li>{@code name} —— 分类名称，也是插件中心筛选下拉的展示/取值口径；</li>
 *   <li>{@code sort} —— 展示顺序（2026-09-19 补列），未跑迁移的实例为 NULL，
 *       按 0 处理后退化为 id 升序，与补列前行为一致；</li>
 *   <li>{@code create_time}/{@code update_time} —— 秒级时间戳，不参与排序。</li>
 * </ul>
 */
@Mapper
public interface AddonCateMapper {

    /**
     * 全量分类（sort 升序，同 sort 按 id 升序）。
     *
     * <p>2026-09-19：与插件市场、顶栏「插件应用」统一为同一顺序口径
     * （三处都读 hou_addon_cate，顺序必须一致，否则同一套分类在不同页面顺序不同）。
     * sort 缺失（未跑迁移的实例）时按 0 处理，退化为 id 升序。
     *
     * <p>只取 id / name / sort，避免表结构变更（如后续补 parent_id）时把查询写死成
     * {@code select *} 而被迫同步改动。
     */
    @Select("SELECT id, name, COALESCE(sort, 0) AS sort FROM hou_addon_cate ORDER BY sort ASC, id ASC")
    List<Map<String, Object>> selectAll();

    /**
     * 插件编码 → 分类 ID：由「插件位一级菜单」的 cate_id 关联。
     *
     * <p>插件_row 本身没有分类外键，真正把插件和分类绑起来的是菜单行：
     * hou_permission_menu 中 {@code display_pos='plugin'} 的一级菜单
     * （路径形如 {@code /plugin/orderfood}）携带 cate_id，与顶栏「插件应用」
     * 的分组口径完全一致。
     *
     * @param paths 形如 /plugin/{code} 的菜单路径
     * @return 每行含 paths、cate_id
     */
    @Select("<script>" +
            "SELECT paths, cate_id FROM hou_permission_menu " +
            "WHERE display_pos = 'plugin' AND paths IN " +
            "<foreach collection='paths' item='path' open='(' separator=',' close=')'>#{path}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectPluginCateIds(@Param("paths") List<String> paths);

    /**
     * 全量插件位一级菜单（paths + cate_id），不限插件编码。
     *
     * <p>给启动对账器（PluginRegistryReconciler）用：插件位菜单是「本地已安装且在用」
     * 的权威信号 —— 安装会经 PluginMenuPort.sync 建菜单，卸载会 remove 菜单，
     * 因此菜单存在 ⇔ 插件处于已安装状态。
     */
    @Select("SELECT paths, cate_id FROM hou_permission_menu WHERE display_pos = 'plugin'")
    List<Map<String, Object>> selectAllPluginMenus();
    @Update("UPDATE hou_addon_plugin SET cate_id=#{cateId}, category=#{category}, update_time=NOW() "
            + "WHERE plugin_code=#{pluginCode} AND deleted=0")
    int updateCatalogCategory(@Param("pluginCode") String pluginCode, @Param("cateId") int cateId,
                              @Param("category") String category);

    @Select("SELECT COUNT(*) FROM hou_addon_plugin WHERE plugin_code=#{pluginCode} AND deleted=0")
    int countCatalogPlugin(@Param("pluginCode") String pluginCode);

    @Update("UPDATE hou_market_plugin SET cate_id=#{cateId}, category=#{category}, update_time=NOW() "
            + "WHERE plugin_code=#{pluginCode} AND deleted=0")
    int updateMarketCategory(@Param("pluginCode") String pluginCode, @Param("cateId") int cateId,
                             @Param("category") String category);

}
