package com.houjiang.order.dto;

import lombok.Data;

/**
 * @ClassName AssetDirectionAndPayTypeInfoDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 9:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class AssetDirectionAndPayTypeInfoDTO {

    /**
     * 资产方向（收入或支出）
     */
    private String assetDirection;

    /**
     * 支付类型（1表示微信支付，2表示支付宝支付，3表示银联支付）
     */
    private Integer payType;

    /**
     * 用户ID
     */
    private String userId;

}
