package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserLogActionEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserLogActionService extends IService<UserLogActionEntity> {

    /**
     * 查询用户操作日志列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult queryUserOperateLog(Map<String, Object> paraMap);
}
