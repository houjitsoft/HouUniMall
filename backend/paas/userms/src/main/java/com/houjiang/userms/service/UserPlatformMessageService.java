package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPlatformMessageEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserPlatformMessageService extends IService<UserPlatformMessageEntity> {

    /**
     * 发送站内信
     *
     * @param param
     * @return
     */
    ResponseResult sendPlatformMessage(Map<String, Object> param);

    /**
     * 发送站内信 feign
     * @param param
     * @return
     */
    ResponseResult sendPlatformMessageFeign(Map<String, Object> param);

    /**
     * 查询我的站内信
     *
     * @param param
     * @return
     */
    ResponseResult queryPlatformMessage(Map<String, Object> param);

    /**
     * 阅读站内信
     *
     * @param param
     * @return
     */
    ResponseResult readPlatformMessage(Map<String, Object> param);

    /**
     * 删除站内信
     *
     * @param param
     * @return
     */
    ResponseResult deletePlatformMessage(Map<String, Object> param);
}
