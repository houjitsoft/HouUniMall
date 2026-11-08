package com.houjiang.base.dto;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * 页面导航表(BaseSystemPlatformNav)
 *
 * @since 2023-03-28 17:29:44
 * @author Amos
 */
@Data
public class BaseSystemPlatformNavDTO extends UserTokenBeanDTO {

    /**
     * 索引ID
     */
    private Integer navId;
    /**
     * 类别，0自定义导航，1商品分类，2文章导航，3活动导航，默认为0
     */
    private Integer navType;
    /**
     * 类别ID，对应着nav_type中的内容，默认为0
     */
    private Integer navItemId;
    /**
     * 导航标题
     */
    private String navTitle;
    /**
     * 导航链接
     */
    private String navUrl;
    /**
     * 导航位置，0头部，1中部，2底部，默认为0
     */
    private Integer navLocation;
    /**
     * 是否以新窗口打开，0为否，1为是，默认为0
     */
    private Integer navNewOpen;
    /**
     * 排序
     */
    private Integer navDisplayorder;
    /**
     * 是否启用 0 否  1是
     */
    private Integer navActive;
    /**
     * 不可修改-团购、积分等等
     */
    private Integer navReadonly;
    /**
     * 是否被推荐1推荐
     */
    private Integer navIsRec;
    /**
     * 1商城 2批发市场
     */
    private Integer navProperty;
}

