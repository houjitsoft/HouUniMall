package com.houjiang.mall.mapper.plugin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站点插件目录（hou_addon_plugin）数据访问。
 *
 * <p>除 MyBatis-Plus 基础能力外，额外提供 P0-3「plugin.yaml → hou_addon_plugin 单向同步」所需的
 * 幂等 upsert 与软删；两者都以显式 SQL 绕过 @TableLogic，以便命中唯一键时能复活已软删行。
 *
 * @author Amos
 */
@Mapper
public interface AddonPluginMapper extends BaseMapper<AddonPlugin> {

    /**
     * 以 plugin.yaml（唯一事实来源）幂等同步站点目录行。
     *
     * <p>命中唯一键 {@code uk_service_plugin(service_code, plugin_code)} 时整行覆盖并复活
     * （{@code deleted=0}），但保留人工维护的正数 {@code cate_id}、{@code sort} 与原始 {@code create_time}，
     * 避免每次安装都重置排序。
     *
     * @param plugin 由 plugin.yaml 映射得到的目录行（service_code / plugin_code 必填）
     * @return 受影响行数（MySQL upsert：插入=1，更新=2）
     */
    @Insert("INSERT INTO hou_addon_plugin "
            + "(service_code, plugin_code, name, category, cate_id, version, description, "
            + " terminals_json, dependencies_json, entry_path, config_schema, status, sort, deleted, create_time, update_time) "
            + "VALUES (#{serviceCode}, #{pluginCode}, #{name}, #{category}, "
            + " COALESCE((SELECT id FROM hou_addon_cate WHERE name=#{category} ORDER BY sort ASC, id ASC LIMIT 1), 0), "
            + " #{version}, #{description}, "
            + " #{terminalsJson}, #{dependenciesJson}, #{entryPath}, #{configSchema}, #{status}, #{sort}, 0, NOW(), NOW()) "
            + "ON DUPLICATE KEY UPDATE name=VALUES(name), "
            + " category=CASE WHEN COALESCE(cate_id,0)>0 THEN "
            + " COALESCE((SELECT name FROM hou_addon_cate WHERE id=hou_addon_plugin.cate_id), category) "
            + " ELSE VALUES(category) END, "
            + " cate_id=CASE WHEN COALESCE(cate_id,0)>0 THEN cate_id ELSE VALUES(cate_id) END, version=VALUES(version), "
            + " description=VALUES(description), terminals_json=VALUES(terminals_json), dependencies_json=VALUES(dependencies_json), "
            + " entry_path=VALUES(entry_path), config_schema=VALUES(config_schema), status=VALUES(status), "
            + " deleted=0, update_time=NOW()")
    int upsertFromManifest(AddonPlugin plugin);

    /**
     * 显式移除目录行（{@code deleted=1}），保留数据可追溯；重新发布可由
     * {@link #upsertFromManifest} 复活。插件卸载不得调用本方法。
     *
     * @param serviceCode 归属服务（如 mall）
     * @param pluginCode  插件唯一编码
     * @return 受影响行数
     */
    @Update("UPDATE hou_addon_plugin SET deleted=1, update_time=NOW() "
            + "WHERE service_code=#{serviceCode} AND plugin_code=#{pluginCode}")
    int softDeleteByCode(@Param("serviceCode") String serviceCode, @Param("pluginCode") String pluginCode);

    /** 同一插件迁移归属时，旧服务行必须退出可见目录。 */
    @Update("UPDATE hou_addon_plugin SET deleted=1, update_time=NOW() "
            + "WHERE plugin_code=#{pluginCode} AND service_code<>#{serviceCode} AND deleted=0")
    int softDeleteOtherServiceRows(@Param("serviceCode") String serviceCode,
                                   @Param("pluginCode") String pluginCode);

    /**
     * 跨服务读取软删保护，兼容正确的 hostService 归属。
     *
     * <p><b>不要再用「按 service_code 过滤」的同名重载</b>：MyBatis 的 statement id 是
     * {@code 接口.方法名}，不含参数签名，两个同名重载会互相覆盖——启动时固定刷一条
     * {@code mapper[...AddonPluginMapper.selectSoftDeletedCodes] is ignored, because it exists}，
     * 而且真正生效的 SQL 取决于注册顺序，实际是「按 service_code 的那条」被静默丢掉。
     * 对账器要的是跨服务全集（插件换个宿主后旧行仍是软删），所以只保留本方法。
     */
    @Select("SELECT DISTINCT plugin_code FROM hou_addon_plugin WHERE deleted = 1")
    List<String> selectSoftDeletedCodes();
}
