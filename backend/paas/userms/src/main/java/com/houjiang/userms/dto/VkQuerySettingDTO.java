package com.houjiang.userms.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 微客设置传参
 * @author Amos
 */
@Data
public class VkQuerySettingDTO {

    /**
     *  微客名字
     */
    private String vkName;
    /**
     * 顶部标题
     */
    private String vkTitle;
    /**
     * 顶部图片
     */
    private String vkImage;
    /**
     * 申请条件  1注册成功  2注册成功+购买礼包商品
     */
    private Integer applyType;
    /**
     * 是否开启配置 1位开启 2位关闭
     */
    private Integer vkState;
    /**
     * 海报
     */
    private String posterImage;
    /**
     * 微客须知
     */
    private String vkNotice;
    /**
     * 固定奖励
     */
    private BigDecimal fixedRewards;

}
