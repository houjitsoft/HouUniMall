package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserVkVO;
import com.houjiang.userms.dto.VkAddUserDTO;
import com.houjiang.userms.entity.UserInviterEntity;
import com.houjiang.userms.entity.UserVkEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName UserVkService
 * @Description 店铺微客会员表
 * @Author Amos
 * @Date 2023-06-05 11:00:51
 * @Version 1.0
 * @author Amos
 */
public interface UserVkService extends IService<UserVkEntity> {

    /**
     * 查询微客用户信息
     *
     * @param paraMap
     * @return
     */
    UserInviterEntity queryVKUserInfo(Map<String, String> paraMap);

    /**
     * 我的邀请人
     *
     * @param paraMap
     * @return
     */
    ResponseResult getAskUserInfo(Map<String, String> paraMap);

    /**
     * 添加微客
     *
     * @param param
     * @return
     */
    ResponseResult addVkUser(VkAddUserDTO param);

    /**
     * 添加用户绑定更新
     *
     * @param param
     * @return
     */
    ResponseResult addUserInviter(VkAddUserDTO param);

    /**
     * 平台新增微客用户
     */
    ResponseResult addVkUserToPlatform(VkAddUserDTO param);

    /**
     * 平台修改上级
     */
    ResponseResult updateVkUserSuperior(VkAddUserDTO param);

    /**
     * TODO 补充方法说明
     * @param userId TODO
     */
    void delUserVk(String userId);

    /**
     * 平台查看微客用户
     */
    ResponseResult selectVkUserList(VkAddUserDTO param);

    /**
     * 导出平台查看微客用户
     */
    ResponseResult selectVkUserListExp(VkAddUserDTO param);

    /**
     * 购买礼包，设置微客用户，获取固定奖励
     *
     * @param orderId 订单id
     * @return 操作结果
     */
    ResponseResult setVKInfo(String orderId);

    /**
     * 查询微客累计推广用户
     */
    ResponseResult selectVkTotalTgUser(VkAddUserDTO param);

    /**
     * 查询微客的分销信息
     */
    ResponseResult selectVkDistributionInfo(VkAddUserDTO param);

    /**
     * 查询我的客户信息
     */
    ResponseResult selectVkCustomerInfo(VkAddUserDTO param);

    /**
     * 查询我的分销账户金额
     */
    ResponseResult selectVkCommissionInfo(VkAddUserDTO param);

    /**
     * 查询用户是否是微客
     */
    ResponseResult queryUserIsVk(VkAddUserDTO param);

    /**
     * 同步微客推广数量
     */
    ResponseResult syncVkTgNum();

    /**
     * 查询账号信息
     * @param userId
     * @return
     */
    UserVkVO queryUserAccount(@Param("userId") String userId);

    /**
     * 更新用户分销佣金账户表
     * @param paraMap
     * @return
     */
    int subUserAccount(Map<String, String> paraMap);
}
