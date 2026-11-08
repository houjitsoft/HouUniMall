package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName AssetPlatformQueryExp
 * @Description
 * @Author Amos
 * @Date 2022/8/17 13:41
 * @Version 1.0
 * @author Amos
 */
@Data
public class AssetPlatformQueryExpVO {

    /**
     * 详情类型
     */
    private String detailType;
    /**
     * 金额
     */
    private BigDecimal assetAmount;
    /**
     * 来源/去向
     */
    private String assetDirection;

    /**
     * 商户订单id
     */
    private String orderId;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 用户昵称
     */
    private String userNickname;
    /**
     * 用户手机号
     */
    private String userMobile;


}
