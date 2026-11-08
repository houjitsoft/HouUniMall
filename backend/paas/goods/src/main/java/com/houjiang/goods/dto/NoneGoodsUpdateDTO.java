package com.houjiang.goods.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName NoneGoodsUpdateDTO
 * @Author Amos
 * @Date 2023/6/26 11:30
 * @Version 1.0
 * @author Amos
 */
@Data
public class NoneGoodsUpdateDTO {

    /**
     * 商品id
     */
    private List<Integer> commonIdList;

    /**
     * 1新增,2编辑
     */
    private Integer opType;

    /**
     * 排序，数字越大，越靠前
     */
    private Integer displayOrder;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 商品类型
     */
    private Integer noneType;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer activityVerify = 1;

    /**
     *  审核备注
     */
    private String activityVerifyRemark;

    /**
     * 活动角标
     */
    private Integer cornerId;
}
