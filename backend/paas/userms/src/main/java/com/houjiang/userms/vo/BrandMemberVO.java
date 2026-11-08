package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class BrandMemberVO {

    /**
     *用户id
     */
    private String userId;
    /**
     *总数
     */
    private Integer totalCount;
    /**
     *用户名
     */
    private String userName;
    /**
     *用户手机号
     */
    private String userMobile;
    /**
     *1 企业注册 0 个人
     */
    private Integer isBusiness;
    /**
     *1 禁用  0启用
     */
    private Integer isDelete;
    /**
     *注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /**  */
    private Date memberCreateTime;
    /**
     *最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userLastloginTime;
    /**
     *上次购买时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userLastShopTime;
    /**
     *总消费金额
     */
    private String totalAmount;
}
