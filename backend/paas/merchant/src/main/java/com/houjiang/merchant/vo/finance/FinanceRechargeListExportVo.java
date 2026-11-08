package com.houjiang.merchant.vo.finance;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceRechargeListExportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @ExcelProperty("ID")
    @ColumnWidth(50)
    private Integer id;

    @Schema(description = "用户昵称")
    @ExcelProperty("用户昵称")
    @ColumnWidth(50)
    private String nickname;

    @Schema(description = "用户头像")
    @ExcelProperty("用户头像")
    @ColumnWidth(50)
    private String avatar;

    @Schema(description = "账号")
    @ExcelProperty("账号")
    @ColumnWidth(50)
    private String account;

    @Schema(description = "订单编号")
    @ExcelProperty("订单编号")
    @ColumnWidth(50)
    private String sn;

    @Schema(description = "支付方式: [2=微信支付, 3=支付宝支付]")
    @ExcelProperty("支付方式: [2=微信支付, 3=支付宝支付]")
    @ColumnWidth(50)
    private String payWay;

    @Schema(description = "支付状态: [0=待支付, 1=已支付]")
    @ExcelProperty("支付状态: [0=待支付, 1=已支付]")
    @ColumnWidth(50)
    private Integer payStatus;

    @Schema(description = "退款状态: [0=未退款 , 1=已退款]")
    @ExcelProperty("退款状态: [0=未退款 , 1=已退款]")
    @ColumnWidth(50)
    private Integer refundStatus;

    @Schema(description = "支付状态: [0=待支付, 1=已支付]")
    @ExcelProperty("支付状态: [0=待支付, 1=已支付]")
    @ColumnWidth(50)
    private String payStatusText;

    @Schema(description = "支付方式")
    @ExcelProperty("支付方式")
    @ColumnWidth(50)
    private String payWayText;

    @Schema(description = "支付金额")
    @ExcelProperty("支付金额")
    @ColumnWidth(50)
    private BigDecimal orderAmount;

    @Schema(description = "支付时间")
    @ExcelProperty("支付时间")
    @ColumnWidth(50)
    private String payTime;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    @ColumnWidth(50)
    private String createTime;

}
