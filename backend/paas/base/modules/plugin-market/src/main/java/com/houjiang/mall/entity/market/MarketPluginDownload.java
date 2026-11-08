package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件市场-下载 / 安装明细（{@code hou_market_plugin_download}）。
 *
 * <p><b>为什么要有明细表</b>：{@code hou_market_plugin.download_count} 只是一个自增计数器——
 * 谁下载的、哪个版本、来自哪个站点，全都没有留痕；站点重装一次就虚增一次，也没有任何
 * 可回溯依据。下载次数一旦要「对别的安装人展示」，就必须是可审计的真实数据：
 * 一行 = 一个真实安装方的真实一次下载。
 *
 * <p><b>计数口径</b>：唯一键 {@code (site_code, plugin_code, version)}，同一站点同一版本
 * 只记一次。{@code hou_market_plugin.download_count} 由本表按插件聚合回写，
 * 永远等于「真实下载过的站点 × 版本」数，重装不会刷量。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin_download")
public class MarketPluginDownload implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 插件编码 */
    private String pluginCode;

    /** 版本号（空串表示未知版本） */
    private String version;

    /**
     * 下载方站点标识。
     *
     * <p>{@code platform} 表示平台自身（unimall.houjit.com）；其它站点用
     * {@code houcloud.platform.site-code} 的值，保证跨站点不重名。
     */
    private String siteCode;

    /** 站点名称（展示用，可空） */
    private String siteName;

    /** 来源：{@code install} 在线安装 / {@code download} 手动下载 / {@code platform} 平台自装 */
    private String source;

    /** 客户端 IP（服务端取，非请求体传入） */
    private String clientIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
