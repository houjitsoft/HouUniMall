package com.houjiang.userms.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 部门表
 * @author Amos
 */
@Data
public class AdminDepartmentDTO extends PageHelper {

    /**
     * 部门id
     */
    private Integer id;

    /**
     * 部门名称
     */
    private String userDepartment;

}
