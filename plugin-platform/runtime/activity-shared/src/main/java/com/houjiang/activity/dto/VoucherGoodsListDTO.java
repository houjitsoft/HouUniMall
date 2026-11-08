package com.houjiang.activity.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName VoucherGoodsListDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/12 16:55
 * @Version 1.0
 * @author Amos
 */
@Data
public class VoucherGoodsListDTO {

    /**
     * 商品skuid
     */
    private Integer commonId;

    /**
     * 代金券模版编号
     */
    private Integer voucherTId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 审核 1通过，0未通过，10审核中
     */
    private Integer activityVerify = 1;

    /**
     * 更新时间
     */
    private Date updateDate;
}
