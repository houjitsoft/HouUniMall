package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BasekdiniaoConfigDTO;
import com.houjiang.base.entity.BaseKdniaoConfigEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface BaseKdniaoConfigService extends IService<BaseKdniaoConfigEntity> {

    /**
     * 查询快递鸟配置  服务间feign 调用使用
     *
     * @return
     */
    ResponseResult queryConfigFeign();

    /**
     * update 快递鸟配置 存在就更新不存在就添加
     *
     * @param basekdiniaoConfigReq
     * @return
     */
    ResponseResult updateKdiniaoConfig(BasekdiniaoConfigDTO basekdiniaoConfigReq);

    /**
     * 查询快递鸟配置
     *
     * @param param
     * @return
     */
    ResponseResult queryConfig(Map<String, Object> param);

    /**
     * 查询快递鸟账号
     * @param paraMap
     * @return
     */
    ResponseResult queryShopKdNiao(Map<String, Object> paraMap);


    /**
     * 删除快递鸟账号
     * @param paraMap
     * @return
     */
    ResponseResult deleteShopkdniaoParam(Map<String, Object> paraMap);
}
