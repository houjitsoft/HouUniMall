package com.houjiang.commons.dto.userms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户详情信息
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessEmployeeDetailDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;
    /**
     * 姓名
     */
    private String userNickName;

    /**
     * 部门id
     */
    private Integer sectorId;

    /**
     * 部门名称
     */
    private String sectorName;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 公司id
     */
    private Integer businessId;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 员工许可
     */
    private String employeePermissions;

    /**
     * 员工许可集合
     */
    private List<Integer> employeePermissionsList;

    /**
     * token
     */
    private String token;

}
