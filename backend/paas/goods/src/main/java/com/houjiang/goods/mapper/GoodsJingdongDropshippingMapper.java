package com.houjiang.goods.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.vop.OrderGoodsJdVopDTO;
import com.houjiang.goods.dto.PlatFormAllGoodsDTO;
import com.houjiang.goods.vo.GoodsJingdongDropshippingVO;
import com.houjiang.goods.vo.JingDongGoodsVO;
import com.houjiang.goods.vo.QueryDealerGoodsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import com.houjiang.goods.entity.GoodsJingdongDropshippingEntity;

/**
 * 京东商品代发记录(GoodsJingdongDropshipping)
 *
 * @author Amos
 * @since 2025-05-29 14:07:48
 */
@Mapper
public interface GoodsJingdongDropshippingMapper extends BaseMapper<GoodsJingdongDropshippingEntity> {
    /**
     * 查询当前供应商商品是否已代发
     * @param supplierGoodsId
     * @param shopId
     * @return
     */
    Boolean selectBySupplierGoodsId(@Param("supplierGoodsId") String supplierGoodsId, @Param("shopId")Integer shopId);

    /**
     * 查询当前供应商商品是否已代发
     * @param commonIdStr
     * @param shopId
     * @return
     */
    List<GoodsJingdongDropshippingEntity> selectBySupplierGoodsIdBySpuId(@Param("commonIdStr") String commonIdStr, @Param("shopId") Integer shopId);

    /**
     * 删除京东商品经销记录
     * @param commonIds
     */
    void deleteGoodsRecord(@Param("commonIds")List<Integer> commonIds);

    /**
     * 根据goodsId 查询代发商品信息
     * @param goodsId
     * @return
     */
    GoodsJingdongDropshippingVO selectDropshippingInfoByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 根据订单号查询京东商品信息
     * @param orderId
     * @return
     */
    List<JingDongGoodsVO> selectJingDongGoodsInfoByOrderId(@Param("orderId") String orderId);

    /**
     * 根据京东skuId查询所有代发商品信息
     * @param skuIds
     * @return
     */
    List<GoodsJingdongDropshippingEntity> selectAllByJingDongSkuId(@Param("skuIds") List<String> skuIds);

    /**
     *  根据京东id 查询 商城商品id
     * @param skuIds
     * @return
     */
    List<Integer> selectByJingDongSkuId(@Param("skuIds")List<String> skuIds);

    /**
     * 根据京东父订单号 和 京东skuId 查询商城goodsId
     * @param orderGoodsJdVopDTO
     * @return
     */
    String queryMallGoodsIdByJingDongOrderId(@Param("param") OrderGoodsJdVopDTO orderGoodsJdVopDTO);

    /**
     * 查询订单售后类型
     * @return
     */
    Integer queryOrderReturnMethodType();
    /**
     * 查询代发商品信息
     * @param page
     * @param platFormAllGoodsDTO
     * @return
     */
    IPage<QueryDealerGoodsVO> platFormQueryDealerGoods(IPage page, @Param("param") PlatFormAllGoodsDTO platFormAllGoodsDTO);

    /**
     * 查询代发数量
     * @param supplyCommonId
     * @return
     */
    Integer getDealerGoodsNum(@Param("supplyCommonId") String supplyCommonId);

    /**
     * 查询代发信息
     * @param page
     * @param platFormAllGoodsDTO
     * @return
     */
    IPage<QueryDealerGoodsVO> platFormQueryDealerShop(IPage page,  @Param("param") PlatFormAllGoodsDTO platFormAllGoodsDTO);
}

