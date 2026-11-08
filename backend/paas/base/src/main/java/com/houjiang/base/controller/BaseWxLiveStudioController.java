package com.houjiang.base.controller;

import com.houjiang.base.dto.AdLiveRoomParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName BaseWxLiveStudioController
 * @Description
 * @Author Amos
 * @Date 2023/7/28 10:54
 * @Version 1.0
 **/
@RestController
@RequestMapping("base/wxLiveStudio")
public class BaseWxLiveStudioController extends BaseController {

    /**
     * @throws
     * @title 添加直播间
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/addLiveRoom")
    public ResponseResult addLiveRoom(@RequestBody AdLiveRoomParamDTO param){
            return baseWxLiveStudioService.addLiveRoom(param);
    }
}
