package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserBindConnectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserBindConnect
 * @author Amos
 */
@Mapper
public interface UserBindConnectMapper extends BaseMapper<UserBindConnectEntity> {
    /**
     * 判断用户是否授权登录过
     * @param bindOpenid
     * @return
     */
    String selectUserIdByOpenid(@Param("bindOpenid") String bindOpenid);

    /**
     * 根据授权id 查询用户信息
     * @param bindId
     * @return
     */
    UserInfoVO getUserInfoByUserId(@Param("bindId") String bindId);

    /**
     * 修改当前用户 是否自动登录
     * @param paramMap
     */
    void updateAotuLogin(@Param("param") Map<String, Object> paramMap);

    /**
     * 插入第三方用户关联信息
     * @param userThreeBindMap
     */
    void insertUserThreeBindMap(Map<String, Object> userThreeBindMap);

    /**
     * 查询用户是否已经绑定
     * @param bindId
     * @return
     */
    String selectUserIdByBindId(String bindId);

    /**
     * 修改用户昵称 头像
     * @param updateUserParam
     */
    void updateUserNickNameAndAvatarUrl(@Param("param") Map<String, Object> updateUserParam);

    /**
     * 查询用户是否自动登录
     * @param openId
     * @return
     */
    Integer queryAutoLogin(@Param("openId") String openId);

}




