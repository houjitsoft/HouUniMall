package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.DiscountBaseAddParamDTO;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountBaseListQueryDTO;
import com.houjiang.activity.dto.FinishBaseListQueryDTO;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName ActivityDiscountBaseService
 * @Description 限时折扣活动表
 * @Author Amos
 * @Date 2023-05-30 18:05:20
 * @Version 1.0
 * @author Amos
 */
public interface ActivityDiscountBaseService extends IService<ActivityDiscountBaseEntity> {
    /**
     * 查询限时折扣 活动限购信息
     *
     * @param param
     * @return
     */
    ResponseResult discountCheckLimit(Map<String, Object> param);

    /**
     * 查询所有 限时折扣商品
     * @param param
     * @return
     */
    ResponseResult queryAllZheKou(ListPageQueryParamDTO param);

    /**
     * 查询限时折扣活动列表
     *
     * @param param
     * @return
     */
    ResponseResult queryDiscountBaseList(DiscountBaseListQueryDTO param);

    /**
     * 新增限时折扣
     *
     * @param param
     * @return
     */
    ResponseResult addDiscountBase(DiscountBaseAddParamDTO param);

    /**
     * 查询限时折扣活动详情
     * @param param
     * @return
     */
    ResponseResult queryDiscountBaseDetail(DiscountBaseDetailQueryDTO param) throws IllegalAccessException;

    /**
     * 装修 查看限时 活动列表
     *
     * @param param
     */
    ResponseResult queryFinishBaseList(FinishBaseListQueryDTO param);

    /**
     * 平台删除 限时折扣
     *
     * @param param
     * @return
     */
    ResponseResult deletePlatforDiscountBase(DiscountBaseListQueryDTO param);


    /**
     * 平台查询 限时折扣详情
     *
     * @param param
     * @return
     */
    ResponseResult queryPlatforDiscountBaseDetail(DiscountBaseListQueryDTO param);

    /**
     * 平台查询 限时折扣列表
     *
     * @param param
     * @return
     */
    ResponseResult queryPlatformDiscountBaseList(DiscountBaseListQueryDTO param);


    /**
     * 复制限时折扣活动详情
     *
     * @param param
     * @return
     */
    ResponseResult queryDiscountBaseCopy(Map<String, Object> param);
}
