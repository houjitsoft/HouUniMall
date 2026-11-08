package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商城支付方式（支付场景）实体。
 * 对应表 hou_merchant_mall_pay_scene。
 * 场景：1=微信小程序，2=微信公众号，3=H5，4=PC，5=APP。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_mall_pay_scene")
public class SettingMallPayScene implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关联 hou_merchant_mall_pay.id */
    private Integer mallPayId;

    /** 场景编码：1=微信小程序, 2=微信公众号, 3=H5, 4=PC, 5=APP */
    private Integer scene;

    /** 默认支付：0=否, 1=是 */
    private Integer isDefault;

    /** 方式状态：0=关闭, 1=开启 */
    private Integer status;

    private Long createTime;
    private Long updateTime;
    private Long deleteTime;
}
