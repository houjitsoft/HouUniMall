package com.houjiang.merchant.validate.setting;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DictTypeUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;

    @NotNull(message = "name参数缺失")
    @Length(max = 200, message = "名称不能超出200个字符")
    @Schema(description = "名称", required = true)
    private String name;

    @NotNull(message = "type参数缺失")
    @Length(max = 200, message = "类型不能超出200个字符")
    @Schema(description = "类型", required = true)
    private String type;

    @Length(max = 200, message = "备注不能超出200个字符")
    @Schema(description = "备注")
    private String remark;

    @NotNull(message = "dictStatus参数缺失")
    @IntegerContains(values = {0, 1}, message = "dictStatus参数不在合法值内")
    @Schema(description = "状态", required = true)
    private Integer status;

}
