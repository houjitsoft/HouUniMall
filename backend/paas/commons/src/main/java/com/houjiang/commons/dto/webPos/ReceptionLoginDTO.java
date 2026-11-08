package com.houjiang.commons.dto.webPos;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Amos
 */
@Data
public class ReceptionLoginDTO {
    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    private String phone;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
