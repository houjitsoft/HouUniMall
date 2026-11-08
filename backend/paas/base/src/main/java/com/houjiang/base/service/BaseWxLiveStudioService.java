package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.AdLiveRoomParamDTO;
import com.houjiang.base.entity.BaseWxLiveStudioEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseWxLiveStudioService
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:54
 * @Version 1.0
 * @author Amos
 */
public interface BaseWxLiveStudioService extends IService<BaseWxLiveStudioEntity> {

    /**
     * 添加直播间
     * @param param
     * @return
     */
    ResponseResult addLiveRoom(AdLiveRoomParamDTO param);
}
