package com.houjiang.mall.validator.setting;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型编辑参数
 *
 * @author Amos
 */
@Data
public class DictTypeUpdateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "id参数必传")
    @Min(value = 1, message = "id必须大于0")
    private Integer id;

    @NotBlank(message = "name参数缺失")
    @Size(max = 200, message = "名称不能超出200个字符")
    private String name;

    @NotBlank(message = "type参数缺失")
    @Size(max = 200, message = "类型不能超出200个字符")
    private String type;

    @Size(max = 1000, message = "备注不能超出1000个字符")
    private String remark;

    @NotNull(message = "status参数缺失")
    private Integer status;
}
