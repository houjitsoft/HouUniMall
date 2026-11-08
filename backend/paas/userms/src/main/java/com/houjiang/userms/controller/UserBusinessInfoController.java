package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserBusinessInfoController
 * @Description 企业用户信息
 * @Author Amos
 * @Date 2023/5/18 16:27
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/businessInfo")
public class UserBusinessInfoController extends BaseController{

    /**
     * 判断是否为未通过企业用户
     *
     * @param map
     * @return
     */
    @PostMapping("/infoByMap")
    public ResponseResult getBusinessInfoByMap(@RequestBody Map<String, String> map) {
        ResponseResult result = userBusinessInfoService.isPassedEnt(map);
        return result;
    }


    /**
     * 判断是否企业用户 fegin
     *
     * @param userId
     * @return
     */
    @PostMapping("/isBusiness/fegin")
    public boolean isBusiness(@RequestParam("userId") String userId) {
        boolean result = userBusinessInfoService.isBusinessFegin(userId);
        return result;
    }

    /**
     * 判断是否企业用户
     *
     * @param
     * @return
     */
    @PostMapping("/isBusiness")
    public ResponseResult isBusiness(@RequestBody Map<String, Object> param) {
        ResponseResult result =  userBusinessInfoService.isBusiness(param);
        return result;
    }

    /**
     * 查询企业用户详细信息
     *
     * @param
     * @return
     */
    @PostMapping("/info/query")
    public ResponseResult queryInfo(@RequestBody Map<String, Object> param) {
        ResponseResult result =  userBusinessInfoService.queryInfo(param);
        return result;
    }

    /**
     * 根据管理员找到企业
     *
     * @param userId
     * @return
     */
    @GetMapping("/queryBusiness")
    public ResponseResult queryBusiness(@RequestParam("userId") String userId) {
        ResponseResult result = userBusinessInfoService.queryBusiness(userId);
        return result;
    }

    /**
     * 修改企业用户详细信息
     *
     * @param
     * @return
     */
    @PostMapping("/info/update")
    public ResponseResult updateInfo(@RequestBody Map<String, Object> param) {
        ResponseResult result = userBusinessInfoService.updateBusinessInfo(param);
        return result;
    }


    /**
     * 查询企业用户信息
     *
     * @param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryList(@RequestBody Map<String, Object> param) {
        ResponseResult result = userBusinessInfoService.queryList(param);
        return result;
    }

    /**
     * 注册企业用户
     *
     * @param map
     * @return
     */
    @PostMapping("/register/insertBusiness")
    public ResponseResult registerBusiness(@RequestBody Map<String, Object> map) {
        ResponseResult result = userBusinessInfoService.registerBusiness(map);
        return result;
    }

    /**
     * 更改企业用户信息
     *
     * @param map
     * @return
     */
    @PostMapping("/update/businessInfo")
    @SysLog("更改企业用户信息")
    public ResponseResult updateBusinessInfo(@RequestBody Map<String, Object> map) {
        ResponseResult result = userBusinessInfoService.updateBusinessInfo(map);
        return result;
    }
    /**
     * 校验企业名称是否存在
     * @param paraMap
     * @return
     */
    @PostMapping("/checkCompanyName")
    public ResponseResult chick(@RequestBody Map<String, String> paraMap) {
        ResponseResult result =  userBusinessInfoService.selectBusinessUser(paraMap);
        return result;
    }

    /**
     * 查询部门用户信息
     * @param param
     * @return
     */
    @PostMapping("/feign/queryInfoFeign")
    public BusinessUserInfoDTO queryInfoFeign(@RequestBody Map<String, Object> param){
        BusinessUserInfoDTO businessUserInfoDTO = userBusinessInfoService.queryInfoFeign(param);
        return businessUserInfoDTO;
    }

}
