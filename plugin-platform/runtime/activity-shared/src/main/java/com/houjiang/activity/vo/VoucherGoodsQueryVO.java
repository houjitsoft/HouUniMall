package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName VoucherGoodsQueryVO
 * @Description
 * @Author Amos
 * @Date 2023/7/12 17:35
 * @Version 1.0
 * @author Amos
 */
@Data
public class VoucherGoodsQueryVO {

    /**
     * 商品skuId
     */
    private Integer commonId;

    /**
     * 商品sku名称
     */
    private String commonName;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    /**
     * 审核 1通过，0未通过，10审核中
     */
    private Integer activityVerify;

    /**
     * 活动审核备注
     */
    private String activityVerifyRemark;

    /**
     * 商品sku图片
     */
    private String commonImage;

    /**
     * 代金券标题
     */
    private String voucherTTitle;

    /**
     * 代金券类型：1-单个店铺，2-多店铺参与
     */
    private Integer voucherTType;
}
