package com.houjiang.merchant.common.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallPay implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "模版名称")
    private String name;

    @TableField(exist = false)
    @Schema(description = "显示名称")
    private String payTypeName;

    @Schema(description = "渠道图标")
    private String icon;

    @Schema(description = "支付方式: [1=余额支付, 2=微信支付, 3=支付宝支付]")
    private Integer payType;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "配置参数")
    private Object config;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
