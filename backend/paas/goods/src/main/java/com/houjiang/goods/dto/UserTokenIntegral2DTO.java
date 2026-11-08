package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserTokenIntegral2DTO extends PageHelper {

    /**
     * 用户token
     */
    private String token;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 备注
     */
    private String describe;

    /**
     * 积分类型1.会员注册,2.会员登录3.评价4.购买商品5.6.管理员操作7.积分换购商品8.积分兑换代金券9退货
     */
    private Integer classId;

    /**
     * 虚拟订单核销状态 0是1否 2已过期
     */
    private Integer isCancel;
}
