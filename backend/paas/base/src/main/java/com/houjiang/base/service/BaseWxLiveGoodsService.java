package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.QuCommonsParamDTO;
import com.houjiang.base.dto.SubmitAuditCommonsDTO;
import com.houjiang.base.entity.BaseWxLiveGoodsEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 *
 */
public interface BaseWxLiveGoodsService extends IService<BaseWxLiveGoodsEntity> {

    /**
     * @throws
     * @title 商品删除小程序平台审核
     * @description
     * @author Amos
     * @updateTime 2021/8/18 14:38
     */
    ResponseResult upAuditCommons(Map<String, Object> param);

    /**
     * 获取全部提交的直播商品
     *
     * @param param
     * @return
     */
    ResponseResult queryLiveCommons(Map<String, Object> param);

    /**
     * 删除 提交审核的直播商品
     *
     * @param param
     * @return
     */
    ResponseResult delLiveCommons(Map<String, Object> param);

    /**
     * 提交审核商品
     *
     * @param param
     * @return
     */
    ResponseResult addLiveCommons(QuCommonsParamDTO param);


    /**
     * 平台 商品提交小程序平台审核
     *
     * @param param
     * @return
     */
    ResponseResult submitAuditCommons(SubmitAuditCommonsDTO param);
}
