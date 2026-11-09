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

    /**
     * 登录账号：手机号 / 用户名 / 邮箱均可。
     * 上限必须覆盖邮箱长度（user_name 列 varchar(225)），历史上限制 20 字符会把
     * 形如 xxx@example.com 的邮箱账号直接判成「账号或密码错误」，见 2026-11-09 修复记录。
     */
    @NotEmpty(message = "账号不能为空")
    @Length(min = 2, max = 64, message = "账号或密码错误")
    @Schema(description = "登录账号", required = true)
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 64, message = "账号或密码错误")
    @Schema(description = "登录密码", required = true)
    private String password;

    @NotNull(message = "请选择终端类型")
    @Schema(description = "终端类型", required = true)
    @IntegerContains(values = {1, 2}, message = "终端类型错误")
    private Integer terminal;

}
