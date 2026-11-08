package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserPlusOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserPlusOrderService
 * @Description
 * @Author Amos
 * @Date 2023-07-04 09:36:32
 * @Version 1.0
 * @author Amos
 */
public interface UserPlusOrderService extends IService<UserPlusOrderEntity> {
    /**
     * 查询会员plus充值记录
     * @param pageUserTokenBeanDTO
     * @return
     */
    ResponseResult get(PageUserTokenBeanDTO pageUserTokenBeanDTO);
    /**
     * 会员充值列表导出
     * @param map
     * @return
     */
    ResponseResult exp(Map<String, Object> map);


    /**
     * 会员充值列表查询
     * @param map
     * @return
     */
    ResponseResult list(Map<String, Object> map);

    /**
     * 支付宝 微信 支付时创建 预支付订单  fegin
     * @param map
     * @return
     */
    ResponseResult aLiWxCreatePlusOrder(Map<String, Object> map);

    /**
     * 查询plus 会员订单是否支付成功
     * @param userId
     * @param plusOrder
     * @return
     */
    Integer queryplusorder(String userId, String plusOrder);

    /**
     * 根据用户ID查询用户的Plus会员状态。
     *
     * @param userId 用户的唯一标识，用于查询用户的Plus会员状态。
     * @return 返回用户的Plus会员状态。具体状态码的含义需要参考相关文档或枚举定义。
     */
    Integer queryStatusByPlus(String userId);
}
