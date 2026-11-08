package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShopAccountLogListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "流水号")
    private String sn;

    @Schema(description = "变动对象 0余额")
    private Integer changeObject;

    @Schema(description = "变动对象 0余额")
    private String changeObjectText;

    @Schema(description = "变动类型")
    private Integer changeType;

    @Schema(description = "变动类型")
    private String changeTypeText;

    @Schema(description = "账户状态 0无变化 1增益 2减益")
    private Integer accountStatus;

    @Schema(description = "来源id")
    private Integer fromId;

    @Schema(description = "来源单号")
    private String fromSn;

    @Schema(description = "变动金额")
    private BigDecimal changeAmount;

    @Schema(description = "剩余金额")
    private BigDecimal leftAmount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "记录参数")
    private String params;


}
