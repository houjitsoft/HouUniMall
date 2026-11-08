package com.houjiang.base.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;
/**
 * @ClassName BusinessSwitchService
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
public interface BusinessSwitchService {
    /**
     * 查询企业注册开关
     *
     * @param
     * @return
     */
    ResponseResult query(Map<String, Object> param);

    /**
     * 更新企业注册开关
     *
     * @param
     * @return
     */
    ResponseResult update(Map<String, Object> param);

    /**
     * 查询注册密码配置
     *
     * @param
     * @return
     */
     ResponseResult queryPwdConfig(Map<String, Object> param);

    /**
     * 更新注册密码配置
     *
     * @param
     * @return
     */
     ResponseResult updatePwdConfig(Map<String, Object> param);
}
