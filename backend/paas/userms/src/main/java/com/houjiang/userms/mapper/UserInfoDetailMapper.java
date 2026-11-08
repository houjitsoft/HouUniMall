package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.shop.ShopPersonInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.entity.UserInfoDetaileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserInfoDetail
 * @author Amos
 */
@Mapper
public interface UserInfoDetailMapper extends BaseMapper<UserInfoDetaileEntity> {

    /**
     * 更新用户信息
     * @param paraMap
     * @return
     */
    Integer updateUcenterInfo(@Param("param") Map<String, Object> paraMap);

    /**
     * 分月查询
     * @return
     */
    List<Map<String,Object>> queryUserGrouping();

    /**
     * 修改用户头像
     * @param updateUserParam
     */
    void updateUserAtarUrl(@Param("param") Map<String, Object> updateUserParam);

    /**
     * 查询世明
     * @param userId
     * @return
     */
    Integer queryShiming(@Param("userId") String userId);

    /**
     * 查询登录密码
     * @param userId
     * @return
     */
    Integer queryLoginPassword(@Param("userId") String userId);

    /**
     * 查询支付密码
     * @param userId
     * @return
     */
    Integer queryPayPassword(@Param("userId") String userId);

    /**
     * 删除用户信息
     * @param userId
     */
    void deleteUserInfo(@Param("userId") String userId);
    /**
     * 删除用户详情
     * @param userId
     */
    void deleteUserDetail(@Param("userId") String userId);

    /**
     * 删除用户绑定信息
     * @param userId
     */
    void deleteUserConnect(@Param("userId") String userId);

    /**
     * 删除微客信息
     * @param userId
     */
    void deleteVkUser(@Param("userId") String userId);

    /**
     * 修改用户性别
     * @param paraMap
     * @return
     */
    Integer updateGender(@Param("paraMap")Map<String, String> paraMap);

    /**
     * 修改用户信息
     * @param paraMap
     * @return
     */
    Integer updateUserName(@Param("paraMap")Map<String, String> paraMap);

    /**
     * 修改用户绑定信息
     * @param paraMap
     * @return
     */
    Integer updateUserBindConnect2(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 根据手机号查询用户信
     * @param userMobile
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumber(@Param("userMobile") String userMobile);

    /**
     * 查询用户绑定信息
     * @param bindOpenid
     * @return
     */
    Map<String, String> selectInfo(@Param("bindOpenid")String bindOpenid);

    /**
     * 查询店铺是否存在
     * @param userId
     * @return
     */
    Integer queryShopStatus(@Param("userId")String userId);

    /**
     * 查找用户的实名信息
     *
     * @param map
     * @return
     */
    ShopPersonInfoVO queryShopPersonInfo(@Param("param") Map<String, Object> map);


    /**
     * 查找用户的实名信息
     * @param param
     * @return
     */
    Map<String, Object> queryShopPersonInfoFeign(@Param("param") Map<String, Object> param);
}




