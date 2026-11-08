package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserBindConnectEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserBindConnectService extends IService<UserBindConnectEntity> {
    /**
     * 判断用户是否授权登录过
     * @param bindOpenid
     * @return
     */
    String selectUserIdByOpenid(String bindOpenid);

    /**
     * 根据授权id 查询用户信息
     * @param bindId
     * @return
     */
    UserInfoVO getUserInfoByUserId(String bindId);

    /**
     * 修改当前用户 是否自动登录
     * @param paramMap
     * @return
     */
    ResponseResult updateAotuLogin(Map<String, Object> paramMap);

    /**
     * 插入第三方用户关联信息
     * @param userThreeBindMap
     * @return
     */
    ResponseResult insertUserThreeBindMap(Map<String, Object> userThreeBindMap);

    /**
     * 修改用户昵称 头像
     * @param updateUserParam
     */
    void updateUserNickNameAndAvatarUrl(Map<String, Object> updateUserParam);

    /**
     * 查询用户是否自动登录
     * @param openId
     * @return
     */
    Integer queryAutoLogin(String openId);
    /**
     * 根据用户id查找绑定授权的openId
     *
     * @param userId 用户id
     * @return 绑定授权的openId
     */
    Map<String, String> queryUserOpenId(String userId);
    /**
     * 通过unionId插入openId
     *
     * @param openId
     * @param unionId
     */
    void insertOpenIdByUnionId(String openId, String unionId);
}
