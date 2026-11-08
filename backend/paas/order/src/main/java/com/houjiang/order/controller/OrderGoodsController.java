package com.houjiang.order.controller;

import com.houjiang.commons.annnotation.ShopTokenLimit;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.goods.BaseDetailVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.order.dto.QueryLogisticsMessageDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderGoodsController
 * @Description 订单商品
 * @Author Amos
 * @Date 2023/6/21 10:37
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/goods")
public class OrderGoodsController extends BaseController{

    /**
     * 根据快递单号，快递公司id，收货人 查询物流信息
     */
    @PostMapping("/queryLogisticsByCode")
    public ResponseResult queryLogisticsByCode(@RequestBody @Validated({Select.class}) QueryLogisticsMessageDTO dto) {
        ResponseResult result = orderGoodsService.queryLogisticsByCode(dto);
        return result;
    }

    /**
     * 查询物流信息
     *
     * @param map
     * @return
     */
    @PostMapping("/queryLogistics")
    public ResponseResult queryLogistics(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderGoodsService.queryLogistics(map);
        return result;
    }


    /**
     * @Description: 退款查询商品信息
     * @Param:  [map]
     * @Return: com.yf.ShoppingCart.web.entity.ResultResp
     */
    @PostMapping("/order/goods")
    public ResponseResult queryReturnGoods(@RequestBody Map<String, Object> map){
        ResponseResult result = orderGoodsService.queryReturnGoods(map);
        return result;
    }

    /**
     * @Description: 查询商品信息带分页
     * @Param:  [map]
     * @Return: ResponseResult
     */
    @PostMapping("/selectByPage")
    public ResponseResult selectByPage(@RequestBody Map<String, Object> map){
        ResponseResult result = orderGoodsService.selectByPage(map);
        return result;
    }

    /**
     * 商品销售排行
     *
     * @param map
     * @return
     */
    @PostMapping("/query/salesRanking")
    public ResponseResult querySalesRanking(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderGoodsService.querySalesRanking(map);
        return result;
    }

    /**
     * 定时任务-根据订单id商品信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/job/queryGoodsInfoById")
    public List<AcquiescenceVO> queryGoodsInfoByjob(@RequestParam("orderId") String orderId) {
        List<AcquiescenceVO> voList = orderGoodsService.queryGoodsInfoByjob(orderId);
        return voList;
    }

    /**
     * 查询订单发货商品
     *
     * @param param
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/queryOrderShipmentGoods")
    public ResponseResult queryOrderShipmentGoods(@RequestBody Map<String, Object> param) {
        return orderGoodsService.queryOrderShipmentGoods(param);
    }

    /**
     * 查询供应商订单发货商品
     *
     * @param param
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/querySupplierOrderShipmentGoods")
    public ResponseResult querySupplierOrderShipmentGoods(@RequestBody Map<String, Object> param) {
        return orderGoodsService.querySupplierOrderShipmentGoods(param);
    }

    /**
     * 查询商品数量
     * @param param
     * @return
     */
    @PostMapping("/feign/queryOrderGoodsCountFeign")
    public Integer queryOrderGoodsCountFeign(@RequestBody  Map<String, Object> param){
        Integer count = orderGoodsService.queryOrderGoodsCountFeign(param);
        return count;
    }

    /**
     * 查询商品数量
     * @param param
     * @return
     */
    @PostMapping("/feign/queryOrderGoodsCount")
    public Integer queryOrderGoodsCount(@RequestBody  Map<String, Object> param){
        Integer count = orderGoodsService.queryOrderGoodsCount(param);
        return count;
    }

    /**
     * 查询折扣商品信息，返回单个商品的详细信息
     * 该方法专注于查询单个商品的折扣信息，通过商品列表结果对象作为参数
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件和商品相关信息
     * @return 返回单个商品的折扣信息详细视图对象
     */
    @PostMapping("/feign/queryDiscountGoodsInfo2")
    public DiscountGoodsInfoVO queryDiscountGoodsInfo2(@RequestBody DiscountGoodsListVO discountGoodsListResult){
        DiscountGoodsInfoVO discountGoodsInfoVO = orderGoodsService.queryDiscountGoodsInfo2(discountGoodsListResult);
        return discountGoodsInfoVO;

    }

    /**
     * 查询符合条件的买家用户列表
     * 该方法用于筛选出符合条件的买家用户，条件通过商品列表结果对象传递
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件
     * @return 返回符合条件的买家用户ID列表
     */
    @PostMapping("/feign/queryBuyerUsers")
    public List<String> queryBuyerUsers(@RequestBody DiscountGoodsListVO discountGoodsListResult){
        List<String> list = orderGoodsService.queryBuyerUsers(discountGoodsListResult);
        return list;
    }

    /**
     * 查询满足特定条件的记录数量
     * 该方法用于统计满足特定条件的记录数，这些条件通过一个参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回满足条件的记录数量
     */
    @PostMapping("/feign/queryCountForOlder")
    public Integer queryCountForOlder(@RequestBody Map<String,Object> paramMap){
        Integer count = orderGoodsService.queryCountForOlder(paramMap);
        return count;
    }

    /**
     * 查询用户的订单数量
     * 该方法用于统计用户的订单数量，统计条件通过参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回用户的订单数量
     */
    @PostMapping("/feign/queryUserOrderCount")
    public Integer queryUserOrderCount(@RequestBody Map<String, Object> paramMap){
        Integer count = orderGoodsService.queryUserOrderCount(paramMap);
        return count;
    }

    /**
     *查询商品信息
/**
     * @param appSeckillGoodsResult
     * @return
     */
    @PostMapping("/feign/querySeckillGoodsInfo2")
    public SeckillGoodsInfoVO querySeckillGoodsInfo2(@RequestBody AppSeckillGoodsQueryResultVO appSeckillGoodsResult){
        SeckillGoodsInfoVO seckillGoodsInfoVO = orderGoodsService.querySeckillGoodsInfo2(appSeckillGoodsResult);
        return seckillGoodsInfoVO;
    }

    /**
     * 通过申请换货查询订单换货商品信息
     *
     * @param dto 包含订单商品以物易物查询条件的数据传输对象
     * @return 返回查询结果的响应对象
     */
    @PostMapping("/queryOrderBarterGoods")
    public ResponseResult<OrderGoodsBarterVO> queryOrderBarterGoods(@RequestBody OrderGoodsBarterDTO dto) {
        return orderGoodsService.queryOrderBarterGoods(dto);
    }

    /**
     * 通过规格值查询订单换货商品规格信息
     *
     * @param dto 包含订单商品以物易物查询条件的数据传输对象
     * @return 返回查询结果的响应对象
     */
    @PostMapping("/queryOrderBarterGoodsSpec")
    public ResponseResult<BaseDetailVO> queryOrderBarterGoodsSpec(@RequestBody OrderGoodsBarterDTO dto) {
        return orderGoodsService.queryOrderBarterGoodsSpec(dto);
    }
    /**
     * 将京东售后申请号存入库
     * @param jdOrderId
     * @param jdReturenOrderId
     */

    @PostMapping("/updateJDReturnId")
    public ResponseResult updateJDReturnId(@RequestParam("jdOrderId") String jdOrderId, @RequestParam("jdReturenOrderId")String jdReturenOrderId){
        return orderGoodsService.updateJDReturnId(jdOrderId,jdReturenOrderId);
    }
    /**
     * 批量查询订单下商品售后权益
     * @param jingDongReturnOrderDTO
     * @return
     */
    @PostMapping("queryJdWarrantyRights")
    public ResponseResult queryJdWarrantyRights(@RequestBody JingDongReturnOrderDTO jingDongReturnOrderDTO){
        return orderGoodsService.queryJdWarrantyRights(jingDongReturnOrderDTO);
    }
}
