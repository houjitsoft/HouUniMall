package com.houjiang.userms.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserVkVO;
import com.houjiang.userms.dto.VkAddUserDTO;
import com.houjiang.userms.entity.UserInviterEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName UserVkController
 * @Description 店铺微客会员表
 * @Author Amos
 * @Date 2023-06-05 11:00:51
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("userVk")
public class UserVkController extends BaseController {

    /**
     * 查询微客用户信息
     * @param paraMap
     * @return
     */
    @PostMapping("/queryVKUserInfo")
    public ResponseResult queryVKUserInfo(@RequestBody Map<String, String> paraMap) {
        UserInviterEntity vkUserInfo = userVkService.queryVKUserInfo(paraMap);
        return ResponseResult.success(vkUserInfo);
    }

    /**
     * 我的邀请人
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    @PostMapping("/getAskUserInfo")
    public ResponseResult getAskUserInfo(@RequestBody Map<String, String> paraMap){
        return userVkService.getAskUserInfo(paraMap);
    }

    /**
     * 新增微客用户
     */
    @RequestMapping("/addVkUser")
    public ResponseResult addVkUser(@RequestBody VkAddUserDTO param) {
        return userVkService.addVkUser(param);
    }

    /**
     * 平台新增微客用户
     */
    @RequestMapping("/platform/addVkUser")
    public ResponseResult addVkUserToPlatform(@RequestBody VkAddUserDTO param) {
        return userVkService.addVkUserToPlatform(param);
    }

    /**
     * 平台修改上级
     */
    @RequestMapping("/updateVkUserSuperior")
    public ResponseResult updateVkUserSuperior(@RequestBody VkAddUserDTO param) {
        return userVkService.updateVkUserSuperior(param);
    }

    /**
     * 平台查看微客用户
     */
    @RequestMapping("/selectVkUserList")
    public ResponseResult selectVkUserList(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkUserList(param);
    }

    /**
     * 导出平台查看微客用户
     */
    @RequestMapping("/selectVkUserListExp")
    public ResponseResult selectVkUserListExp(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkUserListExp(param);
    }

    /**
     * 购买礼包，设置微客用户，获取固定奖励
     * @param orderId 订单id
     * @return 操作结果
     */
    @PostMapping("/setVKInfo")
    public ResponseResult setVKInfo(@RequestParam("orderId") String orderId){
        return userVkService.setVKInfo(orderId);
    }

    /**
     * 查询微客累计推广用户
     */
    @RequestMapping("/selectVkTotalTgUser")
    public ResponseResult selectVkTotalTgUser(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkTotalTgUser(param);
    }

    /**
     * 查询微客的分销信息
     */
    @PostMapping("/selectVkDistributionInfo")
    public ResponseResult selectVkDistributionInfo(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkDistributionInfo(param);
    }

    /**
     * 查询我的客户信息
     */
    @PostMapping("/selectVkCustomerInfo")
    public ResponseResult selectVkCustomerInfo(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkCustomerInfo(param);
    }

    /**
     * 查询我的分销账户金额
     */
    @PostMapping("/selectVkCommissionInfo")
    public ResponseResult selectVkCommissionInfo(@RequestBody VkAddUserDTO param) {
        return userVkService.selectVkCommissionInfo(param);
    }

    /**
     * 查询用户是否是微客
     */
    @PostMapping("/queryUserIsVk")
    public ResponseResult queryUserIsVk(@RequestBody VkAddUserDTO param) {
        return userVkService.queryUserIsVk(param);
    }

    /**
     * 同步微客推广数量
     */
    @GetMapping("/syncVkTgNum")
    public ResponseResult syncVkTgNum() {
        return userVkService.syncVkTgNum();
    }

    /**
     * 查询账号信息
     * @param userId
     * @return
     */
    @GetMapping("/feign/queryUserAccount")
    public UserVkVO queryUserAccount(@RequestParam("userId") String userId){
        UserVkVO vo=userVkService.queryUserAccount(userId);
        return vo;
    }

    /**
     * 更新用户分销佣金账户表
     * @param paraMap
     * @return
     */
    @PostMapping("/feign/subUserAccount")
    public int subUserAccount(@RequestBody  Map<String, String> paraMap){
        int result=userVkService.subUserAccount(paraMap);
        return result;
    }
}
