package com.houjiang.base.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CommissionSettingVO {
    /** 分佣id */
    private Integer id;
    /** 分佣业绩配置:0为无业绩，1为推广人分佣，2为分享人分佣，3为推广人+分享人分佣 */
    private Integer commissionConfig;
    /**
     * 一级推广佣金
     */
    private String oneTgRate;
    /**
     * 二级推广佣金
     */
    private String twoTgRate;
    /** 分享佣金比率 */
    private String fxRate;
    /** 分销中心背景图 */
    private String backgroundImage;
    /** 售后天数 */
    private Integer afterSalesDays;
    /** 微客推广是否开启，1开启，2关闭 */
    private Integer vkState;

    public Integer getId() {
        return id;
    }


}
