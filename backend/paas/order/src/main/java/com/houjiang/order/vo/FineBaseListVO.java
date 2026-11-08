package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName FineBaseListVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 10:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class FineBaseListVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 买家账号
     */
    private String buyuserName;

    /**
     * 买家联系方式
     */
    private String buyuserMobile;

    /**
     * 投诉内容
     */
    private String complainContent;

    /**
     * 商家罚款金额
     */
    private BigDecimal penaltyMoney;

    /**
     * 投诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date complainDatetime;
}
