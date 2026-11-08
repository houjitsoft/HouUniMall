package com.houjiang.mall.validator.setting;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据 - 新增参数。
 *
 * @author Amos
 */
@Data
public class DictDataCreateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "typeId参数缺失")
    @Min(value = 1, message = "typeId 必须为大于0的整数")
    @JsonAlias({"type_id"})
    private Integer typeId;

    @JsonAlias({"type_value"})
    private String typeValue;

    @NotBlank(message = "name参数缺失")
    @Size(max = 100, message = "字典数据名称不能超过100个字符")
    private String name;

    @NotBlank(message = "value参数缺失")
    @Size(max = 100, message = "字典数据值不能超过100个字符")
    private String value;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sort;

    @NotNull(message = "status参数缺失")
    @Min(value = 0, message = "status 取值范围 [0,1]")
    private Integer status;
}
