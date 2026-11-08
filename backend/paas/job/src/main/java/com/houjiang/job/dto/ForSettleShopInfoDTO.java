package com.houjiang.job.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName ForSettleShopInfoDTO
 * @Description 结算店铺信息
 * @Author Amos
 * @Date 2023/5/17 16:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class ForSettleShopInfoDTO {

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 结算周期
     */
    private Integer shopSettlementCycle;

    /**
     * 上传结算时间
     */
    private Date shopSettlementLastTime;

    /**
     * 平台抽成
     */
    private String shopCommissionRate;
}
