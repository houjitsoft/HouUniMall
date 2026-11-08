package com.houjiang.userms.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.group.Select;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubAccountDTO extends UserTokenBeanDTO {

    /**
     * 部门id
     */
    private Integer sectorId;

    /**
     * 昵称
     */
    private String userNickName;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名
     */
    @NotNull(message = "用户名不能为空",groups = {Select.class})
    private String userName;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 手机号
     */
    private String userMobile;

    /**
     * 权限
     */
    private List<Integer> employeePermissionsList;

}
