package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付配置实体（对应 hou_merchant_mall_pay）。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_mall_pay")
public class SettingMallPay implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 模板名称 */
    private String name;

    /** 渠道图标 */
    private String icon;

    /** 支付方式：1=余额支付, 2=微信支付, 3=支付宝支付 */
    private Integer payType;

    /** 排序编号 */
    private Integer sort;

    /** 备注信息 */
    private String remark;

    /** 配置参数（JSON 字符串） */
    private String config;

    /** 创建时间（秒级时间戳） */
    private Long createTime;

    /** 更新时间（秒级时间戳） */
    private Long updateTime;

    /** 删除时间（秒级时间戳，NULL 表示未删除） */
    private Long deleteTime;
}
