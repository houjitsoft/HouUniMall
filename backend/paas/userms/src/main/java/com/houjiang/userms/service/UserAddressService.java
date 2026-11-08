package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserAddressEntity;

import java.util.Map;

/**
 * @ClassName UserAddressService
 * @Description  用户地址表
 * @Author Amos
 * @Date 2023-06-16 15:26:50
 * @Version 1.0
 * @author Amos
 */
public interface UserAddressService extends IService<UserAddressEntity> {
    /**
     * 查询用户默认收货地址
     * @param paraMap
     * @return
     */
    ResponseResult getDef(Map<String, String> paraMap);

    /**
     * 查询我的收货地址
     *
     * @param paraMap
     * @return
     */
    ResponseResult get(Map<String, String> paraMap);

    /**
     * 更新收货地址
     *
     * @param paraMap
     * @return
     */
    ResponseResult update(Map<String, String> paraMap);

    /**
     * 删除收货地址
     *
     * @param paraMap
     * @return
     */
    ResponseResult delete(Map<String, String> paraMap);

    /**
     * 新增收货地址
     *
     * @param paraMap
     * @return
     */
    ResponseResult add(Map<String, String> paraMap);

    /**
     * 设置默认地址
     * @param paraMap
     * @return
     */
    ResponseResult setDefault(Map<String, String> paraMap);
}
