package com.houjiang.order.controller;

import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.annnotation.ShopTokenLimit;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.order.dto.UserFxOrderListDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName OrderCommissionController
 * @Description
 * @Author Amos
 * @Date 2023/7/14 9:44
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/commission")
public class OrderCommissionController extends BaseController {

    /**
     * 查询用户佣金记录（BFF 层 queryCommissionList 对应接口）
     * 返回当前登录用户的全部佣金记录分页结果。
     *
     * @param param 查询参数
     * @return 佣金记录分页结果
     */
    @PostMapping("/queryCommissionList")
    public ResponseResult queryCommissionList(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.queryCommissionList(param);
    }

    /**
     * 查询微客具体的分销订单
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    @AdminTokenLimit
    @PostMapping("/queryUserFxOrderList")
    public ResponseResult queryUserFxOrderList(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.queryUserFxOrderList(param);
    }

    /**
     * 查看微客的累计推广商品
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    @PostMapping("/queryUserFxOrderGoodsList")
    public ResponseResult queryUserFxOrderGoodsList(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.queryUserFxOrderGoodsList(param);
    }

    /**
     * 商家查询微客具体的分销订单
     *
     * @param param 包含查询条件的参数对象
     * @return 返回查询结果的响应对象
     */
    @ShopTokenLimit
    @PostMapping("/queryUserFxOrderListByShop")
    public ResponseResult queryUserFxOrderListByShop(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.queryUserFxOrderListByShop(param);
    }

    /**
     * 根据传入的参数查询今日分享商品
     */
    @PostMapping("/selectVkTodayShareGoods")
    public ResponseResult selectVkTodayShareGoods(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.selectVkTodayShareGoods(param);
    }

    /**
     * 查询累计推广订单
     */
    @PostMapping("/queryVkTotalTgOrderList")
    public ResponseResult queryVkTotalTgOrderList(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.queryVkTotalTgOrderList(param);
    }

    /**
     * 查询用户结算佣金订单
     */
    @PostMapping("/selectVkSettlementOrder")
    public ResponseResult selectVkSettlementOrder(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.selectVkSettlementOrder(param);
    }

    /**
     * 查询用户预估收益
     */
    @PostMapping("/selectVkEstimated")
    public ResponseResult selectVkEstimated(@RequestBody UserFxOrderListDTO param) {
        return orderCommissionService.selectVkEstimated(param);
    }


    /**
     * 修改订单分佣状态
     * @param orderId
     */
    @GetMapping("/feign/updateOrderCommissionState")
    public void updateOrderCommissionState(@RequestParam("orderId") String orderId){
        orderCommissionService.updateOrderCommissionState(orderId);

    }


    /**
     * 查询佣金订单
     * @param orderId
     * @return
     */
    @GetMapping("/feign/selectOrderCommission")
    public OrderCommissionVO selectOrderCommission(@RequestParam("orderId") String orderId){
        OrderCommissionVO orderCommission = orderCommissionService.selectOrderCommission(orderId);
        return orderCommission;
    }

}
