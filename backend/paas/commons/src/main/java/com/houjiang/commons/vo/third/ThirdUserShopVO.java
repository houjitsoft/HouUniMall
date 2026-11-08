package com.houjiang.commons.vo.third;

import lombok.Data;

/**
 * @Description:
 * @CreateDate: 2025/7/8 23:23
 * @author Amos
 */
@Data
public class ThirdUserShopVO {

    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户等级，1-一级决策者；2-二级扫码者
     */
    private Integer userLevel;

    /**
     * 审核状态，0-未审核；1-已审核; 2-审核不通过
     */
    private Integer auditStatus;

    /**
     * 用户信息
     */
    private ThirdUserVO user;


    /**
     * 店铺信息
     */
    private ThirdShopVO shop;

}
