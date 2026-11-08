package com.houjiang.mall.vo.setting;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付配置 VO（对外输出）。
 * config 字段返回反序列化后的 Object（前端需要 Map/Object 而非字符串）。
 *
 * @author Amos
 */
@Data
public class SettingMallPayVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /** 模板名称 */
    private String name;

    /** 显示名称（按 payType 推导，如 "微信支付"） */
    private String payTypeName;

    /** 渠道图标 */
    private String icon;

    /** 支付方式：1=余额支付, 2=微信支付, 3=支付宝支付 */
    private Integer payType;

    /** 排序编号 */
    private Integer sort;

    /** 备注信息 */
    private String remark;

    /** 配置参数（JSON 反序列化对象） */
    private Object config;
}
