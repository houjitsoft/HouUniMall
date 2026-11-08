package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName
 * @Description  发票地址管理表
 * @Author Amos
 * @Date 2023-07-04 14:05:34
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_invoice_address")
@Data
public class OrderInvoiceAddressEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer addressId;
    /**
     * 收票人ID
     */
    private String userId;
    /**
     * 收票人姓名
     */
    private String userName;
    /**
     * 收票人手机号
     */
    private String userMobile;
    /**
     * 收票人邮箱
     */
    private String userMail;
    /**
     * 收票地址-省
     */
    private Integer addressProvince;
    /**
     * 收票地址-市
     */
    private Integer addressCity;
    /**
     * 收票地址-区
     */
    private Integer addressArea;
    /**
     * 详细地址
     */
    private String addressDetail;
    /**
     * 是否默认:1-是；0-否
     */
    private Integer isDefault;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;
    /**
     *  地址中文
     */
    @TableField(exist = false)
    private String completeAddress;

    @TableField(exist = false)
    private String addressInfo;

    @TableField(exist = false)
    private String preCompleteAddress;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
