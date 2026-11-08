package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 插件市场-插件仓库（hou_market_plugin）。
 *
 * <p>形态 A：市场并入 mall，市场表与站点库同实例。承载「第三方可开发 / 可定价」元数据。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin")
public class MarketPlugin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginCode;

    private String name;

    /** 开发者/作者 ID，平台自营为 1 */
    private Long authorId;

    /**
     * 应用分类外键（hou_addon_cate.id），0 表示未分类。
     *
     * <p>2026-09-19 新增：此前市场侧只有自由文本的 {@link #category}，实测 8 个取值
     * （营销工具 / 内容营销 / …）与 hou_addon_cate 的 10 个分类<b>零重叠</b>，导致市场页、
     * 插件中心、顶栏「插件应用」三处分类名与数量各说各话。收敛方式：分类归属以
     * <b>外键引用</b>为准，{@link #category} 退化为由字典派生的展示名，不再独立维护。
     */
    private Integer cateId;

    private String category;

    private String description;

    private String shortDesc;

    private String iconUrl;

    private String screenshotsJson;

    private String latestVersion;

    /** FREE / PAID / TRIAL */
    private String licenseType;

    /** 价格（分） */
    private Integer priceCents;

    /** 试用天数（TRIAL 有效） */
    private Integer trialDays;

    private Integer downloadCount;

    private Integer installCount;

    private BigDecimal ratingAvg;

    private Integer ratingCount;

    /** 0草稿 1审核中 2已上架 3已下架 4已拒绝 */
    private Integer status;

    private String minFrameworkVersion;

    private String tagsJson;

    private String terminalsJson;

    private String dependenciesJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
