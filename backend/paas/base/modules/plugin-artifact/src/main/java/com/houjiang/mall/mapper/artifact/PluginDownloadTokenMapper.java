package com.houjiang.mall.mapper.artifact;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.artifact.PluginDownloadToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 下载令牌留痕 Mapper。
 *
 * <p><b>包路径不能改</b>：mall 未声明 {@code @MapperScan}，MyBatis-Plus 自动扫描范围是启动类
 * 所在包 {@code com.houjiang.mall}，Mapper 只有落在这个包下才会被注册成 Bean。
 *
 * <p>两个高频操作写成显式 SQL 而不是 {@code Wrapper}，原因有二：
 * <ol>
 *   <li><b>命中自增必须是原子的</b>：{@code hits = hits + 1} 交给数据库算，
 *       配合 {@code where hits < max_hits} 一次语句完成「判断 + 自增」，
 *       并发命中同一令牌时不会超发——先查后写会超发。</li>
 *   <li><b>可测</b>：单测里对 {@code selectByNonce / consumeHit} 打桩，
 *       比给 {@code Wrapper} 打桩（要解析 Lambda/String 片段）可靠得多。</li>
 * </ol>
 *
 * @author Amos
 * @since M2
 */
@Mapper
public interface PluginDownloadTokenMapper extends BaseMapper<PluginDownloadToken> {

    /**
     * 按 nonce 定位留痕行（nonce 是令牌里带的唯一句柄）。
     *
     * @param nonce 防重放随机串
     * @return 留痕行，不存在返回 null
     */
    @Select("SELECT * FROM hou_plugin_download_token WHERE nonce = #{nonce} LIMIT 1")
    PluginDownloadToken selectByNonce(@Param("nonce") String nonce);

    /**
     * 原子占用一次命中次数。
     *
     * @param nonce 防重放随机串
     * @return 受影响行数；0 表示次数已用尽（或令牌不存在），调用方应拒绝本次下载
     */
    @Update("UPDATE hou_plugin_download_token SET hits = hits + 1 "
            + "WHERE nonce = #{nonce} AND hits < max_hits")
    int consumeHit(@Param("nonce") String nonce);
}
