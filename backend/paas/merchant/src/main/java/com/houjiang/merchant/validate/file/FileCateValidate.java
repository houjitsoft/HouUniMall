package com.houjiang.merchant.validate.file;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FileCateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "缺少pid参数")
    @Min(value = 0, message = "pid参数必须为数字")
    @Schema(description = "上级ID", required = true)
    private Integer pid;

    @NotNull(message = "缺少type参数")
    @IntegerContains(values = {10, 20, 30}, message = "type不在合法值内")
    @Schema(description = "附件类型", required = true)
    private Integer type;

    @NotEmpty(message = "名称不能为空")
    @Length(min = 1, max = 30, message = "名称不能大于30个字符")
    @Schema(description = "附件名称", required = true)
    private String name;

}
