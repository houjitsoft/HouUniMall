package com.houjiang.order.feign;

import com.houjiang.commons.dto.goods.StorageInventoryFlowDTO;
import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import com.houjiang.commons.dto.vop.JingDongReturnGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.dto.order.OrderGoodsBarterDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.supplier.MerchantGoodsBaseDTO;
import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.goods.BaseDetailVO;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonVO;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.order.dto.GoodsCommonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/20 12:47
 */
@FeignClient("goods")
public interface GoodServiceClient {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/mall/goods/saleable-sku")
    MallSkuSnapshot requireSaleableSku(@RequestParam("skuId") Long skuId);

    /**
     * 根据goods_id查询积分商品的价格和所需的积分
     */
    @PostMapping("/goods/common/integralGoods/query")
    ResponseResult queryIntegralGoodsBygoodsId(@RequestBody Map<String,Object> param);

    /**
     * 查询是否为plus商品
     * @param commonId
     * @return
     */
    @PostMapping("goods/common/queryGoodsIsPlus")
    Integer queryGoodsIsPlus(@RequestParam Integer commonId);

    /**
     * 根据商家的商品id 查询供应商的商品价格
     * @param param
     * @return
     */
    @PostMapping("/goods/common/querySupperGoodsPrice")
    ResponseResult querySupplierGoodsPrice(@RequestBody Map<String, Object> param);

    /**
     * 判断当前商品是否是 虚拟商品
     */
    @PostMapping("goods/common/queryVirtualgoods")
    ResponseResult queryVirtualgoods(@RequestBody Map<String,String> param);

    /**
     * 更新商品销量 库存
     * @param param
     */
    @PostMapping("goods/common/update/salesumStock")
    ResponseResult updateSaleSum(@RequestBody Map<String,String> param);

    /**
     * 获取商品信息
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/get/cart/getGoodsDetails")
    ResponseResult getGoodsDetails(@RequestParam("goodsId") Integer goodsId);

    /**
     * 取消下单时，增加库存
     *
     * @param param
     * @return
     */
    @PostMapping("/goods/common/update/stock/reduce")
    ResponseResult updateStockReduce(@RequestBody WapGoodsSaleSumDTO param);

    /**
     * 修改商品库存
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/goods/common/update/goodsStock")
    ResponseResult updateGoodsStock(@RequestBody List<WapGoodsSaleSumDTO> wapGood);

    /**
     * 更新商品评论数
     *
     * @param map
     * @return
     */
    @PostMapping("/goods/common/updateCommentCount")
    ResponseResult updateCommentCount(@RequestBody Map<String, Object> map);


    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/goods/base/get/cart/getGoodsCommonDetails")
    ResponseResult getGoodsCommonDetails(@RequestParam("commonId") Integer commonId);

    /**
     * TODO 补充方法说明
     * @param map TODO
     */
    @PostMapping("/goods/common/dealerOrderGoods")
    void dealerOrderGoods(Map<String, String> map);

    /**
     * 满赠减少赠品库存增加商品销量
     *
     * @param commonIdList
     * @return
     */
    @PostMapping("/goods/common/updateGiveawayStock")
    void updateGiveawayStock(@RequestBody List<Integer> commonIdList);

    /**
     * 查询兑换券下单 商品信息 feign
     *
     * @param goodsId
     * @return
     */
    @PostMapping("/goods/base/queryProductVoucherGoods")
    WapShippingQueryResultVO queryProductVoucherGoods(@RequestParam("goodsId") Integer goodsId);
    /**
     * 查询兑换券下单 商品信息 feign
     *
     * @param commonId
     * @return
     */
    @PostMapping("/goods/common/queryProductVoucherGoodsCommon")
    GdbMerchantGoodsCommonVO queryProductVoucherGoodsCommon(@RequestParam("commonId") Integer commonId);

    /**
     * 更新兑换券商品销量和库存
     *
     * @return
     */
    @PostMapping("/goods/common/update/ProductVoucherGoodsStock")
    ResponseResult updateProductVoucherGoodsStock(@RequestBody Map<String, Object> param);

    /**
     * 插入出库/入库/调拨单接口
     * <p>
     * 通过POST请求接收客户端发送的库存流水信息，并委托服务层插入数据库
     *
     * @param param 经过校验的库存流水数据传输对象，用于接收客户端请求参数
     */
    @PostMapping("/storageInventoryFlow/insert")
    void insertStorageInventoryFlow(@RequestBody StorageInventoryFlowDTO param);

    /**
     * 根据仓库id查询仓库信息
     *
     * @param storageId 仓库Id
     * @return 返回仓库信息封装在ResponseResult中
     */
    @GetMapping("/storageInfo/queryDetail")
    ResponseResult<Map<String, Object>> queryStorageDetail(@RequestParam("storageId") String storageId);

    /**
     * 根据商品id 查询库存信息 feign
     *
     * 该方法用于根据商品的唯一标识符（goodsId）来查询该商品的库存信息
     * 主要用于需要快速获取某个特定商品库存信息的场景，例如商品详情页、库存管理系统等
     *
     * @param goodsId 商品的唯一标识符，用于定位到具体的商品
     * @return 返回包含商品库存信息的列表，列表中的每个元素对应一个库存地点的库存信息
     */
    @PostMapping("/storageGoodsBase/queryGoodsStorageByGoodsId")
    ResponseResult queryGoodsStorageByGoodsId(@RequestParam("goodsId") Integer goodsId);

    /**
     * 批量校验下单商品销售范围和库存
     * @param dto
     */
    @PostMapping("/yiYaTong/checkSkuStock")
    ResponseResult checkSkuStock(StockQueryDTO dto);

    /**
     *  查询商品运费
     * @param dto
     */
    @PostMapping("/yiYaTong/calculate")
    ResponseResult calculate(FreightCalculateDTO dto);

    /**
     * 查询订单详情
     * @param dto
     */
    @PostMapping("/yiYaTong/getOrderDetail")
    ResponseResult getOrderDetail(@RequestBody OrderDetailDTO dto);

    /**
     * 确认收货
     * @param dto
     */
    @PostMapping("/yiYaTong/confirmReceive")
    ResponseResult confirmReceive(@RequestBody ConfirmReceiveDTO dto);


    /**
     * 售后申请
     * @param dto
     */
    @PostMapping("/yiYaTong/applyRefund")
    ResponseResult applyRefund(@RequestBody ApplyRefundDTO dto);

    /**
     * 取消售后申请
     * @param dto
     */
    @PostMapping("/yiYaTong/cancelRefund")
    ResponseResult cancelRefund(@RequestBody CancelRefundDTO dto);

    /**
     * 查询物流信息
     * @param dto
     */
    @PostMapping("/yiYaTong/getOrderExpressListByOs")
    ResponseResult getOrderExpressListByOs(@RequestBody OrderExpressListByOsDTO dto);


    /**
     * 查询售后单详细信息
     * @param dto
     */
    @PostMapping("/yiYaTong/orderReturnGoods/detail")
    ResponseResult orderReturnGoodsDetail(@RequestBody CancelRefundDTO dto);


    /**
     * 退货/换货邮寄卖家
     * @param dto
     */
    @PostMapping("/yiYaTong/sendReturnGoods")
    ResponseResult sendReturnGoods(@RequestBody SendReturnGoodsDTO dto);

    /**
     * 查看申请换货商品
     */
    @PostMapping("/goods/base/queryGoodsToBarter")
    ResponseResult<OrderGoodsBarterVO> queryGoodsToBarter(OrderGoodsBarterDTO dto);

    /**
     * 查询换货商品信息
     * @param barterGoodsId 换货商品id
     */
    @GetMapping("/goods/base/feign/selectBarterGoods")
    ResponseResult<OrderGoodsBarterVO> selectBarterGoods(@RequestParam("goodsId") Integer barterGoodsId);

    /**
     * 下单成功减少商品库存增加商品销量
     *
     * @RequestParam saleSum
     * @return
     */
    @PostMapping("/goods/common/updateSubStockAndAddSaleNum")
    ResponseResult updateSubStockAndAddSaleNum(@RequestBody String saleSum);

    /**
     * 查看申请换货商品
     */
    @PostMapping("/goods/base/feign/queryGoodsSpecToBarter")
    ResponseResult<BaseDetailVO> queryGoodsSpecToBarter(@RequestBody OrderGoodsBarterDTO param);

    /**
     * 根据goodsid
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/base/feign/queryByGoodsId")
    MerchantGoodsBaseDTO queryByGoodsId(@RequestParam("goodsId") Integer goodsId);

    /**
     * 查询商品库存
     * @param goodsId 商品id
     * @return 商品库存
     */
    @GetMapping("/merchantGoodsStock/feign/selectGoodsStock")
    Integer queryGoodsStock(@RequestParam("goodsId") Integer goodsId);

    /**
     * 更新商品销量
     *
     * @param goodsNumMap
     * @return
     */
    @PostMapping("/goods/base/updateGoodsSaleNum")
    void updateGoodsSaleNum(@RequestBody Map<String, Object> goodsNumMap);
    /**
     * 判断当前商品在在京东是否有货
     * @param map
     * @return
     */
    @PostMapping("/jingDong/queryGoodsJingDongExistStock")
    ResponseResult queryGoodsJingDongExistStock(@RequestBody Map<String,Object> map);

    /**
     * 查询京东的物流信息
     * @param jingDongOrderId
     * @return
     */
    @GetMapping("/jingDong/queryJingDongLogistics")
    Map<String,Object> queryJingDongLogistics(@RequestParam("jingDongOrderId") String jingDongOrderId);

    /**
     * 取消京东订单 （代发货前）
     * @param returnReason
     * @param jingDongOrderId
     * @return
     */
    @PostMapping("/jingDong/cancelJingDongOrder")
    ResponseResult cancelJingDongOrder(@RequestParam("returnReason") String returnReason,@RequestParam("jingDongOrderId") String jingDongOrderId);

    /**
     * 创建京东售后单
     * @param jingDongReturnOrderDTO
     * @return
     */
    @PostMapping("/jingDong/createJdAfterSaleOrder")
    ResponseResult createJdAfterSaleOrder(@RequestBody JingDongReturnOrderDTO jingDongReturnOrderDTO);
    /**
     * 查询订单下商品售后权益
     * @param jingDongReturnGoodsDTOList
     * @return
     */
    @PostMapping("/jingDong/queryJdWarrantyRights")
    ResponseResult queryJdWarrantyRights(@RequestBody  List<JingDongReturnGoodsDTO> jingDongReturnGoodsDTOList);

    /**
     * 查询京东账户余额
     * @return
     */
    @GetMapping("/jingDong/queryJongDongBalance")
    ResponseResult queryJongDongBalance();
    /**
     * 京东售后单 取消
     * @param JdOrderId
     * @param returnOrderId
     * @return
     */
    @PostMapping("/jingDong/cancelJdAfterSaleOrder")
    ResponseResult cancelJdAfterSaleOrder(@RequestParam("JdOrderId") String JdOrderId,@RequestParam("returnOrderId") String returnOrderId);
    /**
     /**
     * 售后 查询寄回地址
     * @param jdOrderId
     * @param jdReturnOrderId
     * @return
     */
    @GetMapping("/jingDong/queryJdReturnAddres")
    Map<String, String> queryJdReturnAddres(@RequestParam("jdOrderId") String jdOrderId,@RequestParam("jdReturnOrderId")String jdReturnOrderId);

    /**
     * 调用京东接口 填写运单信息
     * @param jingDongReturnOrderDTO
     */
    @PostMapping("/jingDong/updateJdShippingInfo")
    ResponseResult updateJdShippingInfo(@RequestBody JingDongReturnOrderDTO jingDongReturnOrderDTO);

    /**
     * 确认京东售后服务单
     * @param jdOrderId
     * @param jdReturnOrderId
     * @return
     */
    @PostMapping("/jingDong/confirmJingDongRetunrOrde")
    ResponseResult confirmJingDongRetunrOrde(@RequestParam("jdOrderId") String jdOrderId, @RequestParam("jdReturnOrderId") String jdReturnOrderId);

    /**
     * 确认京东售后服务单
     * @param goodsIds 商品id集合
     * @return
     */
    @PostMapping("/goods/common/feign/selectGoodsByIds")
    List<GoodsCommonDTO> selectGoodsByIds(@RequestBody List<Integer> goodsIds);

}
