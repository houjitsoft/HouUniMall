package com.houjiang.mall.mapper.market;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.market.MarketPluginDownload;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 插件市场-下载明细 Mapper。
 *
 * @author Amos
 */
@Mapper
public interface MarketPluginDownloadMapper extends BaseMapper<MarketPluginDownload> {

    /**
     * 幂等写入一条下载明细。
     *
     * <p>用 {@code INSERT IGNORE} 而不是「先查后插」：并发上报（多站点同时装同一个插件）
     * 时先查后插会撞唯一键抛异常，把一次正常上报变成 500。IGNORE 让重复上报静默变成
     * 「这行已存在」，计数天然不虚增。
     *
     * @return 影响行数；1 = 新增（真实一次下载），0 = 重复上报
     */
    @Insert("INSERT IGNORE INTO hou_market_plugin_download "
            + "(plugin_code, version, site_code, site_name, source, client_ip, create_time, update_time) "
            + "VALUES (#{pluginCode}, #{version}, #{siteCode}, #{siteName}, #{source}, #{clientIp}, NOW(), NOW())")
    int insertIgnore(@Param("pluginCode") String pluginCode,
                     @Param("version") String version,
                     @Param("siteCode") String siteCode,
                     @Param("siteName") String siteName,
                     @Param("source") String source,
                     @Param("clientIp") String clientIp);

    /**
     * 按插件批量聚合真实下载次数（一行一个插件）。
     *
     * @param codes 插件编码；调用方保证非空
     * @return 每行 {code, cnt}
     */
    @Select("<script>SELECT plugin_code AS code, COUNT(*) AS cnt FROM hou_market_plugin_download "
            + "WHERE plugin_code IN "
            + "<foreach collection='codes' item='c' open='(' separator=',' close=')'>#{c}</foreach> "
            + "GROUP BY plugin_code</script>")
    List<Map<String, Object>> countByCodes(@Param("codes") List<String> codes);

    /**
     * 把聚合结果回写到市场表的主流转字段。
     *
     * <p>回写而不是自增：自增在「重复上报 / 安装回滚 / 手工修数」后必然和明细对不上，
     * 回写则任何时刻都满足 {@code download_count = COUNT(明细)}。
     *
     * @param pluginCode 插件编码
     * @return 影响行数
     */
    @Update("UPDATE hou_market_plugin SET download_count = "
            + "(SELECT COUNT(*) FROM hou_market_plugin_download d WHERE d.plugin_code = #{pluginCode}) "
            + "WHERE plugin_code = #{pluginCode}")
    int syncDownloadCount(@Param("pluginCode") String pluginCode);

    /**
     * 某插件真实下载过的站点数（去重站点，比次数更能反映「几个安装方在用」）。
     *
     * @param pluginCode 插件编码
     * @return 站点数
     */
    @Select("SELECT COUNT(DISTINCT site_code) FROM hou_market_plugin_download WHERE plugin_code = #{pluginCode}")
    int countSites(@Param("pluginCode") String pluginCode);
}
