package com.houjiang.commons.vo.fx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVkSetVO {

    public static final Integer VK_SET_ID = 1;

    /**
     * id
     */
    private String id;
    /**
     * 0 无业绩归属   1推广人分佣  2分享人分佣  3推广+分享
     */
    private Integer commissionConfig;
    /**
     * 一级推广佣金
     */
    private String oneTgRate;
    /**
     * 二级推广佣金
     */
    private String twoTgRate;
    /**
     * 分享佣金
     */
    private String fxRate;
    /**
     * 分销中心背景图
     */
    private String backgroundImage;
    /**
     * 售后天数
     */
    private Integer afterSalesDays;
    /**
     * 1开启 2关闭
     */
    private Integer vkState;

    public GoodsVkSetVO(Integer vkState) {
        this.vkState = vkState;
    }

}
