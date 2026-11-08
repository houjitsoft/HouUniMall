package com.houjiang.order.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VKUserDTO extends PageHelper {
    /**
     * 省id
     */
    private String provinceId;

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户编号
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺用户ID
     */
    private String shopUserId;

    /**
     * 店铺用户名
     */
    private String shopUserName;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * token
     */
    private String token;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     * 邀请人数
     */
    private Integer inviteNum;

    /**
     * 分享人数
     */
    private Integer shareNum;

    /**
     * var1变量描述（根据实际业务含义添加具体描述）
     */
    private String var1;

    /**
     * var2变量描述（根据实际业务含义添加具体描述）
     */
    private String var2;

    /**
     * var3变量描述（根据实际业务含义添加具体描述）
     */
    private String var3;

    /**
     * var4变量描述（根据实际业务含义添加具体描述）
     */
    private String var4;

    /**
     * 创建时间，格式为"yyyy-MM-dd HH:mm:ss"或"yyyy-MM-dd"（根据实际需要添加具体格式）
     */
    private String createTime;

    /**
     * shStatus变量描述（根据实际业务含义添加具体描述）
     */
    private Integer shStatus;

    /**
     * yongJin变量可能代表某种货币值或者货币单位等，在具体业务语境中定义。可能是余额支付的意思。在具体业务语境中定义。
     */
    private String yongJin;

    /**
     * promotionCommission变量可能代表推广佣金，具体含义根据实际业务语境确定。在具体业务语境中定义。
     */
    private BigDecimal promotionCommission;

    /**
     * commissionSharing变量可能代表佣金分享，具体含义根据实际业务语境确定。在具体业务语境中定义。
     */
    private BigDecimal commissionSharing;

    /**
     * totalCommission变量可能代表总佣金，具体含义根据实际业务语境确定。在具体业务语境中定义。
     */
    private BigDecimal totalCommission;

    /**
     * 昵称
     */
    private String userNickname;

    public VKUserDTO(String userId, String var2, String var3) {
        this.userId = userId;
        this.var2 = var2;
        this.var3 = var3;
    }

}