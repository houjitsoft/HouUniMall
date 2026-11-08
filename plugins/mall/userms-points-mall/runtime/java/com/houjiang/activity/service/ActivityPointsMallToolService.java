package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.entity.ActivityPointsMallToolEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * 积分商城工具配置 Service
 * @author Amos
 */
public interface ActivityPointsMallToolService extends IService<ActivityPointsMallToolEntity> {

    /**
     * 带业务校验的保存/更新
     */
    ResponseResult saveWithValidation(ActivityPointsMallToolEntity entity);
}
