package com.houjiang.base.controller;

import com.houjiang.base.dto.QuCommonsParamDTO;
import com.houjiang.base.dto.SubmitAuditCommonsDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName BaseWxLiveGoodsController
 * @Description
 * @Author Amos
 * @Date 2023/7/18 10:56
 * @Version 1.0
 **/
@RestController
@RequestMapping("base/wxLiveGoods")
public class BaseWxLiveGoodsController extends BaseController {

    /**
     * @throws
     * @title 商品删除小程序平台审核
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/upAuditCommons")
    public ResponseResult upAuditCommons(@RequestBody Map<String, Object> param) {
        ResponseResult result = baseWxLiveGoodsService.upAuditCommons(param);
        return result;

    }

    /**
     * @throws
     * @title 获取全部提交的直播商品
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/queryLiveCommons")
    public ResponseResult queryLiveCommons(@RequestBody Map<String, Object> param) {
        return baseWxLiveGoodsService.queryLiveCommons(param);
    }

    /**
     * @throws
     * @title 删除 提交审核的直播商品
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/upLiveCommons")
    public ResponseResult delLiveCommons(@RequestBody Map<String, Object> param) {
        return baseWxLiveGoodsService.delLiveCommons(param);
    }

    /**
     * @throws
     * @title 提交审核商品
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    @PostMapping("/addLiveCommons")
    public ResponseResult addLiveCommons(@RequestBody QuCommonsParamDTO param) {
        return baseWxLiveGoodsService.addLiveCommons(param);
    }


    /**
     * 平台 商品提交小程序平台审核
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/submitAuditCommons")
    public ResponseResult submitAuditCommons(@RequestBody SubmitAuditCommonsDTO param) {
        ResponseResult result=baseWxLiveGoodsService.submitAuditCommons(param);
        return result;
    }

}
