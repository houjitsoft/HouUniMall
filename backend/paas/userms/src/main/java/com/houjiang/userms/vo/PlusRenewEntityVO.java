package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlusRenewEntityVO {

    /**
     * 套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;

    /**
     *用户id
     */
    private String userId;

    /**
     * 充值id
     */
    private Integer renewId;

    /**
     *权益id
     */
    private String plusEquity;

    /**
     *充值订单
     */
    private String plusOrderid;

    /**
     *续费开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date renewStarttime;

    /**
     *续费结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date renewEndtime;
}
