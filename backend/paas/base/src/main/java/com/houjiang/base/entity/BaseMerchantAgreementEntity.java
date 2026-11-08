package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BaseMerchantAgreementEntity
 * @Description 入驻协议
 * @Author Amos
 * @Date 2023/6/21 14:05
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_base_merchant_agreement")
@Data
public class BaseMerchantAgreementEntity {

    /**
     * 协议id
     */
    private Integer id;

    /**
     * 商家协议/用户协议
     */
    private String name;

    /**
     * url
     */
    private String url;

    /**
     * 标记
     */
    private String sign;

    /**
     * 上传时间
     */
    private Date updateTime;

}
