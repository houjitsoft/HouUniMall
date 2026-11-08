package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台侧：已接入协议回传的站点。
 *
 * <p>首次回传时按 {@code site_code} 落一条，之后只更新最近回传时间与累计条数，
 * 便于平台侧看「哪些站点在回传、最后一次是什么时候」。</p>
 *
 * @author Amos
 */
@Data
@TableName("hou_platform_site")
public class PlatformSiteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 站点唯一标识 */
    private String siteCode;

    /** 站点名称 */
    private String siteName;

    private Date firstReportTime;

    private Date lastReportTime;

    /** 累计回传条数 */
    private Long reportCount;
}
