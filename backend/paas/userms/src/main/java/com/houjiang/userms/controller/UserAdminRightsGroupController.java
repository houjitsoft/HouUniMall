package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.AdminRightsGroupDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserAdminRightsGroupController
 * @Description
 * @Author Amos
 * @Date 2023/7/26 10:08
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/adminRightsGroup")
public class UserAdminRightsGroupController extends BaseController{

    /**
     * 新增用户时查询权限组
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/getInfo")
    public ResponseResult getStaffInfo(@RequestBody Map<String, String> paraMap) {
        ResponseResult result=userAdminRightsGroupService.getStaffInfo(paraMap);
        return result;
    }


    /**
     * 删除 平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    @PostMapping("/deleteGroupInfo")
    @SysLog("删除平台权限组")
    public ResponseResult deleteGroupInfo(@RequestBody AdminRightsGroupDTO queryGroupInfo) {
        ResponseResult result=userAdminRightsGroupService.deleteGroupInfo(queryGroupInfo);
        return result;
    }

    /**
     * 修改平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    @PostMapping("/updateGroupInfo")
    @SysLog("修改平台权限组")
    public ResponseResult updateGroupInfo(@RequestBody AdminRightsGroupDTO queryGroupInfo) {
        ResponseResult result=userAdminRightsGroupService.updateGroupInfo(queryGroupInfo);
        return result;
    }

    /**
     * 查询平台权限组详情
     *
     * @param queryGroupInfo
     * @return
     */
    @PostMapping("/queryGroupInfo")
    public ResponseResult queryGroupInfo(@RequestBody AdminRightsGroupDTO queryGroupInfo) {
        ResponseResult result=userAdminRightsGroupService.queryGroupInfo(queryGroupInfo);
        return result;
    }

    /**
     * 查询平台权限组
     *
     * @param map
     * @return
     */
    @PostMapping("/queryGroupList")
    public ResponseResult queryGroupList(@RequestBody Map<String, Object> map) {
        ResponseResult result=userAdminRightsGroupService.queryGroupList(map);
        return result;
    }

    /**
     * 新增 平台权限组
     *
     * @param queryGroupInfo
     * @return
     */
    @PostMapping("/addGroupInfo")
    @SysLog("新增平台权限组")
    public ResponseResult addGroupInfo(@RequestBody AdminRightsGroupDTO queryGroupInfo) {
        ResponseResult result=userAdminRightsGroupService.addGroupInfo(queryGroupInfo);
        return result;
    }
}
