package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPointsLogEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserPointsLogService extends IService<UserPointsLogEntity> {

    /**
     * 判断当前用户当天是否已经登录过
     *
     * @param map
     * @return
     */
    ResponseResult queryLoginExit(Map<String, String> map);


    /**
     * 添加积分修改记录
     *
     * @param map
     * @return
     */
    ResponseResult insertByMap(Map<String, Object> map);

    /**
     * 积分明细列表
     *
     * @param params
     * @return
     */
    ResponseResult queryIntegralDetailList(Map<String, Object> params);


    /**
     * 平台查询 积分明细列表
     *
     * @param params
     * @return
     */
    ResponseResult query(Map<String, Object> params);

    /**
     * 查询订单获取的积分
     *
     * @param orderId
     * @return
     */
    Integer queryPointsByOrderId(String orderId);
}
