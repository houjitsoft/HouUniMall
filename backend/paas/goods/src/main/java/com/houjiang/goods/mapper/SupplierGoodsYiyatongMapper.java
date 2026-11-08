package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.dto.PlatFormAllGoodsDTO;
import com.houjiang.goods.entity.SupplierGoodsYiyatongEntity;
import com.houjiang.goods.vo.QueryDealerGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.houjiang.goods.entity.SupplierGoodsYiyatong
 * @author Amos
 */
@Mapper
public interface SupplierGoodsYiyatongMapper extends BaseMapper<SupplierGoodsYiyatongEntity> {
    /**
     * 查询怡亚通商品skuId
     * @param goodsId
     * @return
     */
    String queryYiYaTongSkuId(@Param("goodsId") Integer goodsId);

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
    IPage<QueryDealerGoodsVO> platFormQueryDealerShop(IPage page,  @Param("param")PlatFormAllGoodsDTO platFormAllGoodsDTO);
}




