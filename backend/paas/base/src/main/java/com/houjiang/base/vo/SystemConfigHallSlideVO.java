package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SystemConfigHallSlideVO
 * @author Amos
 */
@Data
public class SystemConfigHallSlideVO {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    private List<AppIndexSlideBeanVO> slideList;

    /**
     * 操作类型，1关键字，2商品编号，3链接,4json字符串(shopId keyword url
     */
    private String opType;

    /**
     * 5op_value不传值
     */
    private String opValue;

    /**
     * 描述
     */
    private String configComment;

    private String commonName;

    /**
     * 店铺id
     */
    private String shopName;
    private String catName;

    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
}
