package com.houjiang.mall.validator.setting;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付配置 - 编辑参数。
 *
 * @author Amos
 */
@Data
public class PayConfigUpdateParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "id参数缺失")
    @Min(value = 1, message = "id 必须为大于0的整数")
    private Integer id;

    @NotBlank(message = "name参数缺失")
    @Size(max = 100, message = "模板名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "icon 长度不能超过500")
    private String icon;

    @Min(value = 0, message = "sort 不能小于0")
    private Integer sort;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    /** 配置参数（前端传 JSON 对象） */
    private JsonNode config;
}
