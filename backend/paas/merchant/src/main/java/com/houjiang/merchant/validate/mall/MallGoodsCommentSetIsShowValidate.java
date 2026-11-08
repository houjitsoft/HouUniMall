package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCommentSetIsShowValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    @NotNull(message = "必须提交ID")
    private Integer id;

    @Schema(description = "是否显示")
    @NotNull(message = "请选择状态")
    @IntegerContains(values = {0, 1}, message = "请选择状态")
    private Integer isShow;

}
