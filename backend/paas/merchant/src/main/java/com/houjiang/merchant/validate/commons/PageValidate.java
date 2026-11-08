package com.houjiang.merchant.validate.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PageValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前分页 */
    @DecimalMin(value = "1", message = "pageNo参数必须大于0的数字")
    public Integer page_no = 1;

    /** 每页条数 */
    @DecimalMin(value = "1", message = "pageSize参数必须是大于0的数字")
    ///@DecimalMax(value = "60", message = "pageSize参数必须是小于60的数字")
    private Integer page_size = 20;

}
