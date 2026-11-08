package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayAssetDetailsEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayAssetDetailsService extends IService<PayAssetDetailsEntity> {

    /**
     * 我的余额-明细
     *
     * @param map
     * @return
     */
    ResponseResult queryAssetList(Map<String, Object> map);

    /**
     * 我的余额-查看详情
     *
     * @param map
     * @return
     */
    ResponseResult queryAssetDetail(Map<Object, Object> map);

    /**
     * 平台-明细
     *
     * @return
     */
    ResponseResult queryPlatformAssetList(Map<String, Object> map);

    /**
     * 平台-明细
     *
     * @return
     */
    ResponseResult exportPlatformAssetList(Map<Object, Object> map);


    /**
     * 插入明细
     * @param map
     */
    void insertDetail(Map<Object, Object> map);

}
