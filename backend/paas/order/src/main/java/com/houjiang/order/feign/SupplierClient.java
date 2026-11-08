package com.houjiang.order.feign;

import com.houjiang.commons.dto.order.OrderDropGoodsDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.supplier.SupplierSyncGoodsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ClassName SupplierClient
 * @Description
 * @Author Amos
 * @Date 2024/7/24 15:31
 * @Version 1.0
 * @author Amos
 */
@FeignClient("supplier")
public interface SupplierClient {


    /**
     * 下单成功减少商品库存增加商品销量
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/supplierCommonBase/updateSupplierSubStockAndAddSaleNum")
    ResponseResult updateSupplierSubStockAndAddSaleNum(@RequestBody String wapGood);


    /**
     * 同步供应商商品
     */
    @PostMapping("/supplierCommonBase/synchronousGoods")
    ResponseResult synchronousGoods(@RequestBody SupplierSyncGoodsDTO dto);

    /**
     * 修改商品库存
     *
     * @param wapGood
     * @return
     */
    @PostMapping("/supplierCommonBase/update/goodsstock")
    ResponseResult updateGoodsStock(@RequestBody List<WapGoodsSaleSumDTO> wapGood);



    /**
     * 更新商品销量commomn
     *
     * @param goodsNumMap
     * @return
     */
    @PostMapping("/supplierGoodsBase/info/updateGoodsSaleNum")
    void updateGoodsSaleNum(@RequestBody Map<String, Object> goodsNumMap);

    /**
     * 查询商品库存
     * @param goodsId 商品id
     * @return 商品库存
     */
    @GetMapping("/supplierCommonBase/feign/queryGoodsStock")
    Integer queryGoodsStock(@RequestParam("goodsId") Integer goodsId);

    /**
     * 查询换货商品信息
     * @param barterGoodsId 换货商品id
     */
    @GetMapping("/supplierGoodsBase/info/feign/selectBarterGoods")
    ResponseResult<OrderGoodsBarterVO> selectBarterGoods(@RequestParam("goodsId") Integer barterGoodsId);

    /**
     * 查询根据经销商品id查询供应商商品信息
     * @param goodsId 经销商品id
     * @return 供应商商品信息
     */
    @GetMapping("/supplierGoodsBase/info/feign/queryDropGoodsInfo")
    OrderDropGoodsDTO queryDropGoodsInfo(@RequestParam("goodsId") String goodsId);

}
