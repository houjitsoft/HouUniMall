package com.houjiang.goods.dto;


import lombok.Data;


/**
 * @ClassName UserAccess
 * @Description UserAccess
 * @Author Amos
 * @Date 2024/6/21 16:01
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserAccess {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 商品id
     */
    private String goodsId;
}
