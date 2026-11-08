package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserInfoDetaileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserInfoDetailService extends IService<UserInfoDetaileEntity> {

    /**
     * 用户按月统计数量
     *
     * @param map
     * @return
     */
    ResponseResult userGroupingStatistics(Map<String, Object> map);

    /**
     * 获取用户安全级别
     *
     * @param paraMap
     * @return
     */
    ResponseResult getUserSecurityLevel(Map<String, String> paraMap);

    /**
     * 修改用户头像
     *
     * @param updateUserParam
     */
    void updateUserAtarUrl(Map<String, Object> updateUserParam);

    /**
     * 删除用户信息
     *
     * @param userId
     * @return
     */
    ResponseResult delUserInfo(String userId);

    /**
     * 更新昵称
     *
     * @param paraMap
     * @return
     */
    ResponseResult updateUserNickname(Map<String, String> paraMap);

    /**
     * 查找用户的实名信息
     *
     * @param map
     * @return
     */
    ResponseResult queryShopPersonInfo(Map<String, Object> map);


    /**
     * 查找用户的实名信息
     * @param param
     * @return
     */
    Map<String, Object> queryShopPersonInfoFeign(@Param("param") Map<String, Object> param);
}
