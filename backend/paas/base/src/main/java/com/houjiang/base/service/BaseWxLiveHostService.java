package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.WxChatIdParamDTO;
import com.houjiang.base.entity.BaseWxLiveHostEntity;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName BaseWxLiveHostService
 * @Description
 * @Author Amos
 * @Date 2023/7/24 13:37
 * @Version 1.0
 * @author Amos
 */
public interface BaseWxLiveHostService extends IService<BaseWxLiveHostEntity> {

    /**
     * 查看当前店铺微信状态
     *
     * @param param
     * @return
     */
    ResponseResult queryLiveHostState(@RequestBody UserTokenBeanDTO param);

    /**
     * 申请主播
     *
     * @param param
     * @return
     */
    ResponseResult applyForHost(@RequestBody Map<String, Object> param);

    /**
     * 修改直播微信号
     *
     * @param param
     * @return
     */
    ResponseResult upWeChatId(@RequestBody Map<String, Object> param);

    /**
     * 查看小程序直播间 根据名称
     * @param param
     * @return
     */
    ResponseResult queryLiveRoomsByName(Map<String, String> param);

    /**
     * 查看小程序直播间
     * @param param
     * @return
     */
    ResponseResult queryLiveRooms(PageHelper param);

    /**
     * 平台 停播
     *
     * @param param
     * @return
     */
    ResponseResult stopLive(Map<String,Object> param);


    /**
     * 向微信平台申请主播角色
     *
     * @param param
     * @return
     */
   ResponseResult platformApplyForHost(WxChatIdParamDTO param);

    /**
     * 平台查询 主播申请列表
     *
     * @param param
     * @return
     */
    ResponseResult queryHosts(WxChatIdParamDTO param);
}
