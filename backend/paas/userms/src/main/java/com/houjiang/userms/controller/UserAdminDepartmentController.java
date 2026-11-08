package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.AdminDepartmentDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserAdminDepartmentController
 * @Description
 * @Author Amos
 * @Date 2023/7/26 9:21
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/adminDepartment")
public class UserAdminDepartmentController extends BaseController{

    /**
     * 删除 平台部门
     *
     * @param adminDepartmentDto
     * @return
     */
    @PostMapping("/delete")
    @SysLog("删除 平台部门")
    public ResponseResult deleteDepartment(@RequestBody AdminDepartmentDTO adminDepartmentDto) {
        ResponseResult result=userAdminDepartmentService.deleteDepartment(adminDepartmentDto);
        return result;
    }

    /**
     * 查询平台所有部门
     *
     * @param map
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryDepartmentAll(@RequestBody Map<String, Object> map) {
        ResponseResult result=userAdminDepartmentService.queryDepartmentAll(map);
        return result;
    }


    /**
     * 新增 或 修改 平台部门
     *
     * @param adminDepartmentDto
     * @return
     */
    @PostMapping("/add")
    @SysLog("更新平台部门")
    public ResponseResult addDepartment(@RequestBody AdminDepartmentDTO adminDepartmentDto) {
        if (adminDepartmentDto == null) {
            return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
        } else if (adminDepartmentDto.getUserDepartment().length() > 30) {
            return ResponseResult.fail(BizCodeEnume.DEPARTMENT_NAME_LIMIT);
        }
        ResponseResult result=userAdminDepartmentService.addDepartment(adminDepartmentDto);
        return result;
    }
}
