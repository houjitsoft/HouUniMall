package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.third.*;
import com.houjiang.userms.third.From518AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * @Description: 从518平台获取数据
 * @CreateDate: 2025/7/8 22:40
 * @author Amos
 */
@RestController
@RequestMapping("/from518")
public class From518AppController {

    private static final Logger log = LoggerFactory.getLogger(From518AppController.class);
    @Autowired
    private From518AppService appService;

    /**
     * 获取用户店铺信息（用户身份）
     * @param req
     * @return
     */
    @PostMapping("/getUserAndShopInfo")
    public ResponseResult<ThirdUserShopVO> getUserAndShopInfo(@Valid @RequestBody UserAndShopInfoReqVo req) {
        log.info("从518平台获取用户信息:{}", req);
        From518ResVO<ThirdUserShopVO> vo = appService.getUserAndShopInfo(req.getShopId(), req.getUserId());
        return ResponseResult.success(vo.getData());
    }

    /**
     * 获取用户积分信息
     * @param req
     * @return
     */
    @PostMapping("/getUserPointsInfo")
    public ResponseResult<UserPointsInfoVO> getUserPointsInfo(@Valid @RequestBody UserAndShopInfoReqVo req) {
        From518ResVO<UserPointsInfoVO> vo = appService.getUserPointsInfo(req.getShopId(), req.getUserId());
        return ResponseResult.success(vo.getData());
    }


    /**
     * 获取用户积分流水
     * @param req
     * @return
     */
    @PostMapping("/getPointsFlowDetail")
    public ResponseResult<PageResVO<PointsFlowDetailResVO>> getPointsFlowDetail(@Valid @RequestBody PointsFlowDetailReqVO req) {
        From518ResVO<PageResVO<PointsFlowDetailResVO>> vo = appService.getPointsFlowDetail(req);
        return ResponseResult.success(vo.getData());
    }

    /**
     * 扣减流水
     * @param req
     * @return
     */
    @PostMapping("/consumePoints")
    public ResponseResult consumePoints(@Valid @RequestBody ConsumePointsReqVO req) {
        From518ResVO vo = appService.consumePoints(req);
        return ResponseResult.success(vo.getData());
    }

    /**
     * 新增流水
     * @param req
     * @return
     */
    @PostMapping("/addPoints")
    public ResponseResult addPoints(@Valid @RequestBody AddPointsReqVO req) {
        From518ResVO vo = appService.addPoints(req);
        return ResponseResult.success(vo.getData());
    }


    /**
     * 获取用户店铺详细信息
     * @param req
     * @return
     */
    @PostMapping("/getUserAndShopDetail")
    public ResponseResult<ThirdUserShopVO> getUserAndShopDetail(@Valid @RequestBody UserAndShopInfoReqVo req) {
        From518ResVO<ThirdUserShopVO> vo = appService.getUserAndShopDetail(req.getShopId(), req.getUserId());
        return ResponseResult.success(vo.getData());
    }

}
