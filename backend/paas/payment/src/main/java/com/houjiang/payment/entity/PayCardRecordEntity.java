package com.houjiang.payment.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName
 * @Description  生成充值卡信息记录表
 * @Author Amos
 * @Date 2023-05-31 15:35:50
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_pay_card_record")
@Data
public class PayCardRecordEntity  implements Serializable {
    /**
     * 卡的id
     */
    @TableId(type = IdType.AUTO)
    private Integer cardId;
    /**
     * app id  ： 9999 通用
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
     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private Date creatTime;
    /**
     * 有效开始时间
     */
    private Integer stime;
    /**
     * 到期时间
     */
    private Integer etime;
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
