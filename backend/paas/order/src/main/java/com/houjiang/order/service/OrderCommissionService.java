package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.order.dto.UserFxOrderListDTO;
import com.houjiang.order.entity.OrderCommissionEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName OrderCommissionService
 * @Description
 * @Author Amos
 * @Date 2023/7/14 9:39
 * @Version 1.0
 * @author Amos
 */

public interface OrderCommissionService extends IService<OrderCommissionEntity> {

    /**
     * 查询微客具体的分销订单
     */
    ResponseResult queryUserFxOrderList(UserFxOrderListDTO param);

    /**
     * 查看微客的累计推广商品
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    ResponseResult queryUserFxOrderGoodsList(UserFxOrderListDTO param);

    /**
     * 商家查询微客具体的分销订单
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    ResponseResult queryUserFxOrderListByShop(UserFxOrderListDTO param);

    /**
     * 根据传入的参数查询今日分享商品
     */
    ResponseResult selectVkTodayShareGoods(UserFxOrderListDTO param);

    /**
     * 查询累计推广订单
     */
    ResponseResult queryVkTotalTgOrderList(UserFxOrderListDTO param);

    /**
     * 查询用户结算佣金订单
     */
    ResponseResult selectVkSettlementOrder(UserFxOrderListDTO param);

    /**
     * 查询用户预估收益
     */
    ResponseResult selectVkEstimated(UserFxOrderListDTO param);


    /**
     * 修改订单分佣状态
     * @param orderId
     */
    void updateOrderCommissionState(String orderId);


    /**
     * 查询用户佣金记录（BFF 层 queryCommissionList 对应接口）
     * 基于 order_commission 表精确查询当前登录用户的全部佣金记录，支持分页与状态筛选。
     *
     * @param param 查询参数（含 token、分页、状态等）
     * @return 佣金记录分页结果
     */
    ResponseResult queryCommissionList(UserFxOrderListDTO param);

    /**
     * 查询佣金订单
     * @param orderId
     * @return
     */
    OrderCommissionVO selectOrderCommission(String orderId);

}
