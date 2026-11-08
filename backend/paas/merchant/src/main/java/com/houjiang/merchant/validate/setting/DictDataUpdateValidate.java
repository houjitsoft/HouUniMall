package com.houjiang.merchant.validate.setting;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DictDataUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;

    @IDMust(message = "typeId参数必传且需大于0")
    @Schema(description = "类型ID", required = true)
    private Integer typeId;

    @NotNull(message = "name参数缺失")
    @Schema(description = "键", required = true)
    private String name;

    private String typeValue;

    @NotNull(message = "value参数缺失")
    @Length(max = 100, message = "键名不能超出100个字符")
    @Schema(description = "值", required = true)
    private String value;

    @Length(max = 200, message = "数值不能超出200个字符")
    @Schema(description = "备注")
    private String remark;

    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @Schema(description = "排序")
    private Integer sort;

    @NotNull(message = "status参数缺失")
    @IntegerContains(values = {0, 1}, message = "status参数不在合法值内")
    @Schema(description = "状态", required = true)
    private Integer status;

}
