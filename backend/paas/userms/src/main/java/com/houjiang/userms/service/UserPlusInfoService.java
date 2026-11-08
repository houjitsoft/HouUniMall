package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPlusInfoEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
public interface UserPlusInfoService extends IService<UserPlusInfoEntity> {


    /**
     * 查询plus会员信息
     *
     * @param map
     * @return
     */
    ResponseResult selectByUserId(Map<String, String> map);
    /**
     * 查询当前用户的plus 会员信息 feign
     */
    ResponseResult queryUserPlusInfo(String userId);

    /**
     * 修改当前用户 plus信息表已用额度
     * @param hashMap
     * @return
     */
    ResponseResult updateUserPluslimit(HashMap<Object, Object> hashMap);

    /**
     * 开通plus会员
     * @param map
     * @return
     */
    ResponseResult plusMemberOpen(Map<String, Object> map);

    /**
     * 开通页面权益查询
     * @param map
     * @return
     */
    ResponseResult selectPlusMember(Map<String, Object> map);

    /**
     * 平台开通plus会员
     * @param map
     * @return
     */
    ResponseResult openMember(Map<String, Object> map) throws IllegalAccessException;

    /**
     * plus会员过期更新
     */
    void plusMemberUpdate();

    /**
     * plus会员额度更新
     * @return
     */
    ResponseResult updateLimit();


    /**
     * plus会员门槛红包发放
     */
   void redgrant();

    /**
     * 微信 支付宝 回调失败后 pilus 会员权益补偿
     * @param map
     * @return
     */
    ResponseResult plusOrderCompensate(Map<String, Object> map);
}
