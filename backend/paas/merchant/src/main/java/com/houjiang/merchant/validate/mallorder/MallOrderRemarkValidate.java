package com.houjiang.merchant.validate.mallorder;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallOrderRemarkValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "订单ID", required = true)
    private Integer id;

    @Length(max = 255, message = "备注信息不能超出255个字符")
    @Schema(description = "备注信息")
    private String shopRemark;

}
