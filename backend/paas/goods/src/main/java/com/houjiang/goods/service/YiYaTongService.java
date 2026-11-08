package com.houjiang.goods.service;


import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName YiYaTongService
 * @Description
 * @Author Amos
 * @Date 2024/11/13 11:47
 * @Version 1.0
 * @author Amos
 */

public interface YiYaTongService {

    /**
     * 获取省市区地址
     * @param regionByCodeOpenDTO
     */
    ResponseResult synchronousAddress(RegionByCodeOpenDTO regionByCodeOpenDTO);


    /**
     * 获取省市区地址
     * @param regionByCodeOpenDTO
     */
   ResponseResult getRegionByCodeOpen(RegionByCodeOpenDTO regionByCodeOpenDTO);


    /**
     * 获取分类信息
     * @param categoryDTO
     */
   ResponseResult getCategorys(CategoryDTO categoryDTO);


    /**
     * 获取品牌列表
     * @param dto
     */
    ResponseResult getBrands( BrandsDTO dto);

    /**
     * 查询商品库SPUID列表
     * @param spuPageQueryDTO
     */
    ResponseResult getSpuIdList(SpuPageQueryDTO spuPageQueryDTO);


    /**
     * 查询SPU商品详情并封装
     * @param dto
     */
    ResponseResult getSpuBySpuIdsGoodsList( OpenSpuInfoQueryDTO dto);
    /**
     * 查询SPU商品详情
     * @param dto
     */
    ResponseResult getSpuBySpuIdsPath( OpenSpuInfoQueryDTO dto);


    /**
     * 查询SKU规格信息并封装
     * @param dto
     */
    ResponseResult  getSkuBySpuId(OpenSkuInfoBySpuIdDTO dto);
    /**
     * 查询SKU规格信息
     * @param dto
     */
    ResponseResult  listSkuBySpuId(OpenSkuInfoBySpuIdDTO dto);


 /**
  * 查询商品运费
  * @param dto
  */
    ResponseResult calculate(FreightCalculateDTO dto);


    /**
     * 查询商品价格
     * @param map
     */
    ResponseResult findSkuSalePrice(Map<String, Object> map);

    /**
     * 查询商品库存
     * @param dto
     */
    ResponseResult findSkuStock(OpenFindSkuStockDTO dto);

    /**
     * 修改订单收货地址
     * @param dto
     */
     ResponseResult updateOrderReceiveAddress(UpdateOrderReceiveAddressDTO dto);

    /**
     * 批量校验下单商品销售范围和库存
     * @param dto
     */
    ResponseResult checkSkuStock(StockQueryDTO dto);


    /**
     * 创建订单
     * @param dto
     */
     ResponseResult submitOrder(SubmitOrderDTO dto);


    /**
     * 根据外部订单号查询订单详情列表
     * @param dto
     */
    ResponseResult getOrderDetailList(OrderDetailListDTO dto);

    /**
     * 查询订单详情
     * @param dto
     */
    ResponseResult getOrderDetail(OrderDetailDTO dto);

    /**
     * 查询物流信息
     * @param dto
     */
    ResponseResult getOrderExpressListByOs(OrderExpressListByOsDTO dto);

    /**
     * 确认收货
     * @param dto
     */
    ResponseResult confirmReceive(ConfirmReceiveDTO dto);


    /**
     * 售后申请
     * @param dto
     */
    ResponseResult applyRefund(ApplyRefundDTO dto);

    /**
     * 退货/换货邮寄卖家
     * @param dto
     */
    ResponseResult sendReturnGoods(SendReturnGoodsDTO dto);

    /**
     * 换货确认收货
     * @param dto
     */
    ResponseResult receiptRefund(ReceiptRefundDTO dto);

    /**
     * 取消售后申请
     * @param dto
     */
    ResponseResult cancelRefund(CancelRefundDTO dto);

    /**
     * 取消售后申请
     * @param dto
     */
    ResponseResult orderReturnGoodsDetail(CancelRefundDTO dto);

    /**
     * 查询SPU下所有的SKU规格信息
     * @param dto
     */
    ResponseResult  listAllSkuBySpuId(OpenSkuInfoBySpuIdDTO dto);

    /**
     * 查询怡亚通商品skuId
     * @param goodsId
     * @return
     */
    String queryYiYaTongSkuId(Integer goodsId);
}
