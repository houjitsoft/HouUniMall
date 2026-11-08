package com.houjiang.merchant.validate.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class IdsValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ids参数缺失")
    @Schema(description = "ID数组", required = true)
    private List<Integer> ids;

}
