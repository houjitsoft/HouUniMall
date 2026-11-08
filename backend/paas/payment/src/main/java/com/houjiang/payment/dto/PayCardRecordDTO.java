package com.houjiang.payment.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * 生成充值卡信息记录表(PayCardRecord)
 *
 * @since 2023-04-13 09:47:45
 * @author Amos
 */
@Data
public class PayCardRecordDTO extends PageHelper {

    /**
     * 创建人
     */
    private String useName;

    /**
     * 卡的id
     */
    private Integer cardId;

    /**
     * app id
     */
    private Integer appId;

    /**
     * 创建者
     */
    private String cardCreator;

    /**
     * 面额
     */
    private Integer totalPrice;

    /**
     * 卡片描述
     */
    private String cardDesc;

    /**
     * 创建时间
     */
    private String creatTime;

    /**
     * 有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date stime;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date etime;

    /**
     * 卡片数量
     */
    private Integer cardNumber;

    /**
     * 开始卡序
     */
    private Integer cardStart;

    /**
     * 结束卡序
     */
    private Integer cardEnd;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

}

