package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.entity.BaseMerchantPlusThaliEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface BaseMerchantPlusThaliService extends IService<BaseMerchantPlusThaliEntity> {


    /**
     * 查询plus 会员 套餐信息  feign
     */
    ResponseResult queryMerchantPlusThali(Map<String, Object> map) throws IllegalAccessException;


    /**
     *plus套餐查询
/**
     * @param map
     * @return
     */
    ResponseResult selectplusthali( Map<String, Object> map);


    /**
     *会员充值列表查看权益
/**
     * @param map
     * @return
     */
    ResponseResult renewplusthali(Map<String, Object> map);

    /**
     * plus套餐设置
     * @param map
     * @return
     */
    ResponseResult setplusthali(Map<String, Object> map);


    /**
     * 查询折扣率
     * @return
     */
   ResponseResult selectplusdiscount();
}
