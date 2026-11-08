package com.houjiang.goods.controller;


import com.houjiang.commons.dto.yiyatong.*;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.GoodsCommonService;
import com.houjiang.goods.service.YiYaTongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName YiYaTongController
 * @Description
 * @Author Amos
 * @Date 2024/11/6 14:00
 * @Version 1.0
 * @author Amos
 */
@RefreshScope
@RestController
@RequestMapping("/yiYaTong")
public class YiYaTongController {


    @Autowired
    private YiYaTongService yiYaTongService;

    @Autowired
    protected GoodsCommonService goodsCommonService;


    /**
     * 同步区域编码
     * @param regionByCodeOpenDTO
     */
    @PostMapping("/synchronousAddress")
    public ResponseResult synchronousAddress(@RequestBody RegionByCodeOpenDTO regionByCodeOpenDTO){
        ResponseResult result=yiYaTongService.synchronousAddress(regionByCodeOpenDTO);
        return result;


    }


    /**
     * 获取省市区地址
     * @param regionByCodeOpenDTO
     */
    @PostMapping("/getRegionByCodeOpen")
    public ResponseResult getRegionByCodeOpen(@RequestBody RegionByCodeOpenDTO regionByCodeOpenDTO){
        ResponseResult result=yiYaTongService.getRegionByCodeOpen(regionByCodeOpenDTO);
        return result;


    }

    /**
     * 获取分类信息
     * @param categoryDTO
     */
    @PostMapping("/getCategorys")
    public ResponseResult getCategorys(@RequestBody CategoryDTO categoryDTO){
        ResponseResult result=yiYaTongService.getCategorys(categoryDTO);
        return result;


    }

    /**
     * 获取品牌列表
     * @param dto
     */
    @PostMapping("/getBrands")
    public ResponseResult getBrands(@RequestBody BrandsDTO dto){
        dto.setPageIndex(dto.getPage());
        dto.setPageSize(dto.getLimit());
        ResponseResult result=yiYaTongService.getBrands(dto);
        return result;

    }


    /**
     * 查询商品库SPUID列表
     * @param spuPageQueryDTO
     */
    @PostMapping("/getSpuIdList")
    public ResponseResult getSpuIdList(@RequestBody SpuPageQueryDTO spuPageQueryDTO){
        ResponseResult result=yiYaTongService.getSpuIdList(spuPageQueryDTO);
        return result;


    }

    /**
     * 查询SPU商品详情API
     * @param dto
     */
    @PostMapping("/getSpuBySpuIds")
    public ResponseResult getSpuBySpuIdsPath(@RequestBody OpenSpuInfoQueryDTO dto){
        //将SPU商品详情集合数据转换为List集合
        ResponseResult result=yiYaTongService.getSpuBySpuIdsPath(dto);
        return result;

    }

    /**
     * 查询SKU规格信息
     * @param dto
     */
    @PostMapping("/listSkuBySpuId")
    public ResponseResult listSkuBySpuId(@RequestBody OpenSkuInfoBySpuIdDTO dto){
        //将SKU规格信息集合数据转换为List集合
        ResponseResult result=yiYaTongService.listSkuBySpuId(dto);
        return result;

    }


    /**
     * 查询商品运费
     * @param dto
     */
    @PostMapping("/calculate")
    public ResponseResult calculate(@RequestBody FreightCalculateDTO dto){
        ResponseResult result=yiYaTongService.calculate(dto);
        return result;

    }

    /**
     * 查询商品价格
     * @param map
     */
    @PostMapping("/findSkuSalePrice")
    public ResponseResult findSkuSalePrice(@RequestBody Map<String, Object> map){
        ResponseResult result=yiYaTongService.findSkuSalePrice(map);
        return result;
    }

    /**
     * 查询商品库存
     * @param dto
     */
    @PostMapping("/findSkuStock")
    public ResponseResult findSkuStock(@RequestBody OpenFindSkuStockDTO dto){
        ResponseResult result=yiYaTongService.findSkuStock(dto);
        return result;

    }


    /**
     * 修改订单收货地址
     * @param dto
     */
    @PostMapping("/updateOrderReceiveAddress")
    public ResponseResult updateOrderReceiveAddress(@RequestBody UpdateOrderReceiveAddressDTO dto){
        ResponseResult result=yiYaTongService.updateOrderReceiveAddress(dto);
        return result;

    }

    /**
     * 批量校验下单商品销售范围和库存
     * @param dto
     */
    @PostMapping("/checkSkuStock")
    public ResponseResult checkSkuStock(@RequestBody StockQueryDTO dto){
        ResponseResult result=yiYaTongService.checkSkuStock(dto);
        return result;

    }

    /**
     * 创建订单
     * @param dto
     */
    @PostMapping("/submitOrder")
    public ResponseResult submitOrder(@RequestBody SubmitOrderDTO dto){
        ResponseResult result=yiYaTongService.submitOrder(dto);
        return result;

    }

    /**
     * 根据外部订单号查询订单详情列表
     * @param dto
     */
    @PostMapping("/getOrderDetailList")
    public ResponseResult getOrderDetailList(@RequestBody OrderDetailListDTO dto){
        ResponseResult result=yiYaTongService.getOrderDetailList(dto);
        return result;

    }

    /**
     * 查询订单详情
     * @param dto
     */
    @PostMapping("/getOrderDetail")
    public ResponseResult getOrderDetail(@RequestBody OrderDetailDTO dto){
        ResponseResult result=yiYaTongService.getOrderDetail(dto);
        return result;

    }

    /**
     * 查询物流信息
     * @param dto
     */
    @PostMapping("/getOrderExpressListByOs")
    public ResponseResult getOrderExpressListByOs(@RequestBody OrderExpressListByOsDTO dto){
        ResponseResult result=yiYaTongService.getOrderExpressListByOs(dto);
        return result;

    }

    /**
     * 确认收货
     * @param dto
     */
    @PostMapping("/confirmReceive")
    public ResponseResult confirmReceive(@RequestBody ConfirmReceiveDTO dto){
        ResponseResult result=yiYaTongService.confirmReceive(dto);
        return result;

    }

    /**
     * 售后申请
     * @param dto
     */
    @PostMapping("/applyRefund")
    public ResponseResult applyRefund(@RequestBody ApplyRefundDTO dto){
        ResponseResult result=yiYaTongService.applyRefund(dto);
        return result;

    }

    /**
     * 退货/换货邮寄卖家
     * @param dto
     */
    @PostMapping("/sendReturnGoods")
    public ResponseResult sendReturnGoods(@RequestBody SendReturnGoodsDTO dto){
        ResponseResult result=yiYaTongService.sendReturnGoods(dto);
        return result;

    }

    /**
     * 换货确认收货
     * @param dto
     */
    @PostMapping("/receiptRefund")
    public ResponseResult receiptRefund(@RequestBody ReceiptRefundDTO dto){
        ResponseResult result=yiYaTongService.receiptRefund(dto);
        return result;

    }

    /**
     * 取消售后申请
     * @param dto
     */
    @PostMapping("/cancelRefund")
    public ResponseResult cancelRefund(@RequestBody CancelRefundDTO dto){
        ResponseResult result=yiYaTongService.cancelRefund(dto);
        return result;

    }

    /**
     * 查询售后单详细信息
     * @param dto
     */
    @PostMapping("/orderReturnGoods/detail")
    public ResponseResult orderReturnGoodsDetail(@RequestBody CancelRefundDTO dto){
        ResponseResult result=yiYaTongService.orderReturnGoodsDetail(dto);
        return result;

    }

    /**
     * 一键代发
     */
    @PostMapping("oneClickDropShipping")
    public ResponseResult oneClickDropShipping(@RequestBody OneClickProductListingDTO oneClickProductListingDTO) {
        return goodsCommonService.oneClickDropShipping(oneClickProductListingDTO);
    }


    /**
     * 怡亚通消息回调接口
     */
    @PostMapping("/msg/callBack")
    public ResponseResult msgCallBack(@RequestBody MsgCallBackDTO msgCallBackDTO) {
        return goodsCommonService.msgCallBack(msgCallBackDTO);
    }
    /**
     * 查询怡亚通商品skuId
     * @param goodsId
     * @return
     */
    @GetMapping("/queryYiYaTongSkuId")
    String queryYiYaTongSkuId(@RequestParam("goodsId") Integer goodsId){
        return yiYaTongService.queryYiYaTongSkuId(goodsId);
    }
}
