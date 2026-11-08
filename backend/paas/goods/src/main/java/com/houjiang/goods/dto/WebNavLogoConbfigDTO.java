package com.houjiang.goods.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName WebNavLogoConbfigDTO
 * @Description 配置表
 * @Author Amos
 * @Date 2023/6/26 17:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebNavLogoConbfigDTO {
    /**
     * 配置键
     */
    private String configKey;
    /**
     //商品id2:店铺id3:链接4:分类10:红包11:美食中国12:为你推荐13:特价推荐14:新品推荐'15:溯源资讯16:店铺活动17:精选商品18:商城公告',19新人专区;20平台活动专区;21:9.9包邮专区;22:品牌生活;23:好货溯源
/**
     * 配置值:imageType;
     */
    private String configValue;
    /**
     * 配置类型
     */
    private String configType;
    /**
     * 配置内容描述
     */
    private String configComment;
    /**
     * 配置排序
     */
    private Integer configSort;
    /**
     * 操作类型，1关键字，2商品编号{"goodsId":5026,"commonId":5026}，3链接{"url":http://},4店铺{"shopId":11} 5不传值 6分类id {\"catId\":10000104}
     */
    private String opType;
    /**
     * 操作类型值
     */
    private String opValue;
    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
    /**
     * 终端选择，1pc，2移动端
     */
    private Integer terminalType;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateDate;
}
