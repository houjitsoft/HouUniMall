package com.houjiang.base.vo;

import lombok.Data;

/**
 * 微信地址
 * @author Amos
 */
@Data
public class WxHostsVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 主播微信号
     */
    private String wechatId;

    /**
     * 商家手机号
     */
    private String shopPhone;

    /**
     * 头像
     */
    private String headPortrait;

    /**
     * 创建时间
     */
    private String creataTime;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 审核原因
     */
    private String cause;

    /**
     * 是否停播
     */
    private Integer hoststate;


}
