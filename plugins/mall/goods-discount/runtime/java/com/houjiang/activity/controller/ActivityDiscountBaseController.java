package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityDiscountBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.DiscountBaseAddParamDTO;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountBaseListQueryDTO;
import com.houjiang.activity.dto.FinishBaseListQueryDTO;
import com.houjiang.commons.annnotation.Log;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivityDiscountBaseController
 * @Description 限时折扣活动表
 * @Author Amos
 * @Date 2023-05-30 18:05:19
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("discountBase")
public class ActivityDiscountBaseController {
    @Autowired
    protected ActivityDiscountBaseService activityDiscountBaseService;


    /**
     * 查询限时折扣 活动限购信息
     *
     * @param param
     * @return
     */
    @PostMapping("/check/limit")
    public ResponseResult discountCheckLimit(@RequestBody Map<String, Object> param) {
        ResponseResult result=activityDiscountBaseService.discountCheckLimit(param);
        return result;

    }
    /**
     * 查询所有 限时折扣商品
     *
     * @return
     */
    @PostMapping("/queryAllZheKou")
    public ResponseResult queryAllZheKou(@RequestBody ListPageQueryParamDTO param) {
        ResponseResult result=activityDiscountBaseService.queryAllZheKou(param);
        return result;

    }

    /**
     * 查询限时折扣活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryDiscountBaseList(@RequestBody DiscountBaseListQueryDTO param) {
        ResponseResult result=activityDiscountBaseService.queryDiscountBaseList(param);
        return result;
    }

    /**
     * 新增限时折扣
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"discountName"})
    @Log("新增限时折扣")
    @PostMapping("/add")
    public ResponseResult addDiscountBase(@RequestBody DiscountBaseAddParamDTO param) {
        ResponseResult result=activityDiscountBaseService.addDiscountBase(param);
        return result;
    }

    /**
     * 查询限时折扣活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/detail")
    public ResponseResult queryDiscountBaseDetail(@RequestBody DiscountBaseDetailQueryDTO param) throws IllegalAccessException {
        ResponseResult result=activityDiscountBaseService.queryDiscountBaseDetail(param);
        return result;
    }
    /**
     * 装修 查看限时 活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/modularization/queryAll")
    public ResponseResult queryFinishBaseList(@RequestBody FinishBaseListQueryDTO param) {
        ResponseResult result=activityDiscountBaseService.queryFinishBaseList(param);
        return result;
    }

    /**
     * 平台删除 限时折扣
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/delete")
    public ResponseResult deletePlatforDiscountBase(@RequestBody DiscountBaseListQueryDTO param) {
        ResponseResult result=activityDiscountBaseService.deletePlatforDiscountBase(param);
        return result;
    }


    /**
     * 平台查询 限时折扣详情
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryDetail")
    public ResponseResult queryPlatforDiscountBaseDetail(@RequestBody DiscountBaseListQueryDTO param) {
        ResponseResult result=activityDiscountBaseService.queryPlatforDiscountBaseDetail(param);
        return result;
    }

    /**
     * 平台查询 限时折扣列表
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryList")
    public ResponseResult queryPlatformDiscountBaseList(@RequestBody DiscountBaseListQueryDTO param) {
        ResponseResult result=activityDiscountBaseService.queryPlatformDiscountBaseList(param);
        return result;
    }


    /**
     * 复制限时折扣活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/copy")
    public ResponseResult queryDiscountBaseCopy(@RequestBody  Map<String, Object> param) {
        ResponseResult result=activityDiscountBaseService.queryDiscountBaseCopy(param);
        return result;
    }
}
