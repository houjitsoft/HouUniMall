package com.houjiang.userms.vo;


import lombok.Data;

/**
 * @author Amos
 */
@Data
public class MyCustomerVO {
    /**
     * 用户手机号
     */
    private String userMobile;
    /**
     *用户名
     */
    private String userName;
    /**
     *注册时间
     */
    private String userRegTime;
    /**
     *用户头像
     */
    private String userAvatar;
    /**
     *用户id
     */
    private String userId;



    @Override
    public String toString() {
        return "MyCustomer [userMobile=" + userMobile + ", userName=" + userName + ", userRegTime=" + userRegTime + "]";
    }


}
