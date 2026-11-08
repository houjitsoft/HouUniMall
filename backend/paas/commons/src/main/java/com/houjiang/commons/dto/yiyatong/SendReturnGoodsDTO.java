package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName SendReturnGoodsDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 11:19
 * @Version 1.0
 * @author Amos
 */
@Data
public class SendReturnGoodsDTO {

    /**
     * 物流公司编码
     */
    private String deliveryCorpSn;

    /**
     * 物流单号
     */
    private String deliverySn;

    /**
     * 物流公司名称
     */
    private String deliveryCorpName;

    /**
     * 售后单号
     */
    private String returnSn;


}
