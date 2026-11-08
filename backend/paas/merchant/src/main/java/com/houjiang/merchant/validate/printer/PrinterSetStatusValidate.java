package com.houjiang.merchant.validate.printer;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 打印机参数
 * @author Amos
 */
@Data
@Schema(description = "打印机状态编辑参数")
public class PrinterSetStatusValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @NotNull(message = "状态参数缺失")
    @IntegerContains(values = {0, 1}, message = "状态参数格式错误")
    @Schema(description = "状态")
    private Integer status;
}
