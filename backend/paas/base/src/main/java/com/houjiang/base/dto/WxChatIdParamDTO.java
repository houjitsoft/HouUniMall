package com.houjiang.base.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName WxChatIdParam.java
 * @Description TODO 根据微信号申请主播
 * @createTime 2021年08月17日 14:41:00
 */
@Data
public class WxChatIdParamDTO extends UserTokenBeanDTO {

    /**
     * 微信号
     */
    private String wxChatId;

    /**
     * 主播微信号
     */
    private String wechatId;

    /**
     * ID
     */
    private Integer id;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 原因
     */
    private String cause;

    /**
     * 操作
     */
    private Integer operation;

    /**
     * 名称
     */
    private String name;

    /**
     * 开始
     */
    private Integer start;


    /**
     * 商家手机号
     */
    private String shopPhone;

    /**
     * 店铺名称
     */
    private String shopName;


}
