package com.houjiang.merchant.validate.ms;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.vo.ms.MsCUSkuListedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MsOpenValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
