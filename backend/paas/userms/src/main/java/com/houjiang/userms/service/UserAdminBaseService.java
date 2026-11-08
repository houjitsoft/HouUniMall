package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserAdminBaseEntity;
import com.houjiang.userms.vo.UserPersonalBaseVO;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName UserAdminBaseService
 * @Description  后台用户基础信息表
 * @Author Amos
 * @Date 2023-06-27 14:05:30
 * @Version 1.0
 * @author Amos
 */
public interface UserAdminBaseService extends IService<UserAdminBaseEntity> {

    /**
     * 获取用户信息
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    ResponseResult getLimit(Map<String, String> paraMap);

    /**
     * 删除平台管理员
     *
     * @param param
     * @return
     */
    ResponseResult deleteAdminUserPass(UserPersonalBaseVO param);


    /**
     * 平台用户 获取个人信息
     *
     * @param param
     * @return
     */
    ResponseResult getPersonalBase(UserPersonalBaseVO param);



    /**
     * 查询用户信息(后台系统)
     *
     * @param map
     * @return
     */
    ResponseResult getPlatformUserInfo(Map<String, String> map) throws IOException;


    /**
     * 添加 管理员
     *
     * @param param
     * @return
     */
    ResponseResult addAdminUser(Map<String, String> param);

    /**
     * 修改平台管理员 密码 权限
     *
     * @param param
     * @return
     */
    ResponseResult updateAdminUserPass(Map<String, String> param);

    /**
     * 查询平台所有管理员
     *
     * @param param
     * @return
     */
    ResponseResult queryAdminUserList(UserPersonalBaseVO param);

}
