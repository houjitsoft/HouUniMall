package com.houjiang.commons.dto.userms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ShopYgUserInfoDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/27 10:44
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopYgUserInfoDTO {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 商店ID
     */
    private Integer shopId;

    /**
     * 商店名称
     */
    private String shopName;

    /**
     * 店铺ID
     */
    private Integer storeId;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 商店用户ID
     */
    private String shopUserId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 用户状态（自定义状态）
     */
    private Integer userStatus;

    /**
     * 推广人数
     */
    private String var1;

    /**
     * 分享订单总额
     */
    private String var2;

    /**
     * 分享订单退款总额
     */
    private String var3;

    private String var4;

    public ShopYgUserInfoDTO(String userId, String var3) {
        this.userId = userId;
        this.var3 = var3;
    }
}
