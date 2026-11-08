package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.goods.BaseDetailVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.order.dto.QueryLogisticsMessageDTO;
import com.houjiang.order.entity.OrderGoodsEntity;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderGoodsService
 * @Description
 * @Author Amos
 * @Date 2023/6/21 10:40
 * @Version 1.0
 * @author Amos
 */

public interface OrderGoodsService extends IService<OrderGoodsEntity> {

    /**
     * 根据快递单号，快递公司id，收货人 查询物流信息
     */
    ResponseResult queryLogisticsByCode(QueryLogisticsMessageDTO dto);

    /**
     * 查询物流信息
     *
     * @param map
     * @return
     */
    ResponseResult queryLogistics(Map<Object, Object> map);



    /**
     * @Description: 退款查询商品信息
     * @Param:  [map]
     * @Return: ResponseResult
     */
    ResponseResult queryReturnGoods(Map<String, Object> map);



    /**
     * @Description: 查询商品信息带分页
     * @Param:  [map]
     * @Return: ResponseResult
     */
    ResponseResult selectByPage(Map<String, Object> map);

    /**
     * 商品销售排行
     *
     * @param map
     * @return
     */
    ResponseResult querySalesRanking(Map<String, Object> map);


    /**
     * 根据订单id商品信息
     * @param orderId
     * @return
     */
    List<AcquiescenceVO> queryGoodsInfoByjob(String orderId);

    /**
     * 查询订单发货商品
     *
     * @param param
     * @return
     */
    ResponseResult queryOrderShipmentGoods(Map<String, Object> param);

    /**
     * 查询供应商订单发货商品
     *
     * @param param
     * @return
     */
    ResponseResult querySupplierOrderShipmentGoods(Map<String, Object> param);


    /**
     * 查询商品数量
     * @param param
     * @return
     */
    Integer queryOrderGoodsCountFeign(Map<String, Object> param);

    /**
     * 查询商品数量
     * @param param
     * @return
     */
    Integer queryOrderGoodsCount(Map<String, Object> param);


    /**
     * 查询折扣商品信息，返回单个商品的详细信息
     * 该方法专注于查询单个商品的折扣信息，通过商品列表结果对象作为参数
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件和商品相关信息
     * @return 返回单个商品的折扣信息详细视图对象
     */
    DiscountGoodsInfoVO queryDiscountGoodsInfo2(DiscountGoodsListVO discountGoodsListResult);

    /**
     * 查询符合条件的买家用户列表
     * 该方法用于筛选出符合条件的买家用户，条件通过商品列表结果对象传递
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件
     * @return 返回符合条件的买家用户ID列表
     */
    List<String> queryBuyerUsers(DiscountGoodsListVO discountGoodsListResult);


    /**
     * 查询满足特定条件的记录数量
     * 该方法用于统计满足特定条件的记录数，这些条件通过一个参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回满足条件的记录数量
     */
   Integer queryCountForOlder(Map<String,Object> paramMap);


    /**
     * 查询用户的订单数量
     * 该方法用于统计用户的订单数量，统计条件通过参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回用户的订单数量
     */
    Integer queryUserOrderCount(Map<String, Object> paramMap);


    /**
     *查询商品信息
/**
     * @param appSeckillGoodsResult
     * @return
     */
    SeckillGoodsInfoVO querySeckillGoodsInfo2(AppSeckillGoodsQueryResultVO appSeckillGoodsResult);

    /**
     * 通过申请换货查询订单换货商品信息
     *
     * @param dto 包含订单商品以物易物查询条件的数据传输对象
     * @return 返回查询结果的响应对象
     */
    ResponseResult<OrderGoodsBarterVO> queryOrderBarterGoods(OrderGoodsBarterDTO dto);

    /**
     * 通过规格值查询订单换货商品规格信息
     *
     * @param dto 包含订单商品以物易物查询条件的数据传输对象
     * @return 返回查询结果的响应对象
     */
    ResponseResult<BaseDetailVO> queryOrderBarterGoodsSpec(OrderGoodsBarterDTO dto);

    /**
     * 将京东售后申请号存入库
     * @param jdOrderId
     * @param jdReturenOrderId
     * @return
     */
    ResponseResult updateJDReturnId(String jdOrderId, String jdReturenOrderId);

    /**
     * 批量查询订单下商品售后权益
     * @param jingDongReturnOrderDTO
     * @return
     */
    ResponseResult queryJdWarrantyRights(JingDongReturnOrderDTO jingDongReturnOrderDTO);

}
