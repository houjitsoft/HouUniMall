package com.houjiang.merchant.validate.system;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class LoginsValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "账号不能为空")
    @Length(min = 2, max = 20, message = "账号或密码错误")
    @Schema(description = "登录账号", required = true)
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "账号或密码错误")
    @Schema(description = "登录密码", required = true)
    private String password;

    @NotNull(message = "请选择终端类型")
    @Schema(description = "终端类型", required = true)
    @IntegerContains(values = {1, 2}, message = "终端类型错误")
    private Integer terminal;

}
