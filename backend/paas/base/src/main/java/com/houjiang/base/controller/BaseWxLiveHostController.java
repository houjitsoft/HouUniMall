package com.houjiang.base.controller;

import com.houjiang.base.dto.WxChatIdParamDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseWxLiveHostController
 * @Description
 * @Author Amos
 * @Date 2023/7/24 13:36
 * @Version 1.0
 **/
@RestController
@RequestMapping("base/wxLiveHost")
public class BaseWxLiveHostController extends BaseController {

    /**
     * @throws
     * @title 查看当前店铺微信状态
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/queryLiveHostState")
    public ResponseResult queryLiveHostState(@RequestBody UserTokenBeanDTO param) {
        return baseWxLiveHostService.queryLiveHostState(param);
    }

    /**
     * @throws
     * @title applyForHost
     * @description 申请主播
     * @author Amos
     * @updateTime 2021/8/17 14:45
     */
    @PostMapping("/applyForHost")
    public ResponseResult applyForHost(@RequestBody Map<String,Object> param) {
        return baseWxLiveHostService.applyForHost(param);
    }

    /**
     * @throws
     * @title 修改直播微信号
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/upWeChatId")
    public ResponseResult upWeChatId(@RequestBody Map<String,Object> param) {
        return baseWxLiveHostService.upWeChatId(param);
    }
    /**
     * @throws
     * @title 查看小程序直播间 根据名称
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/queryLiveRoomsByName")
    public ResponseResult queryLiveRoomsByName(@RequestBody Map<String,String> param){
        return baseWxLiveHostService.queryLiveRoomsByName(param);
    }
    /**
     * @throws
     * @title 查看小程序直播间
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/queryLiveRooms")
    public ResponseResult queryLiveRooms(@RequestBody PageHelper param){
        return baseWxLiveHostService.queryLiveRooms(param);
    }


    /**
     * 平台 停播
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/stopLive")
    public ResponseResult stopLive(@RequestBody Map<String,Object> param) {
        ResponseResult result=baseWxLiveHostService.stopLive(param);
        return result;
    }

    /**
     * 向微信平台申请主播角色
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/applyForHost")
    public ResponseResult platformApplyForHost(@RequestBody WxChatIdParamDTO param) {
        ResponseResult result=baseWxLiveHostService.platformApplyForHost(param);
        return result;
    }

    /**
     * 平台查询 主播申请列表
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryHosts")
    public ResponseResult queryHosts(@RequestBody WxChatIdParamDTO param) {
        ResponseResult result=baseWxLiveHostService.queryHosts(param);
        return result;
    }

}
