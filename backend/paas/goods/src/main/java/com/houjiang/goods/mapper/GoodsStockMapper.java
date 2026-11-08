package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.vo.goods.BaseDetailVO;
import com.houjiang.goods.dto.MerchantGoodsStockDTO;
import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.entity.GoodsStockEntity;
import com.houjiang.goods.vo.MerchantGoodsStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.goods.entity.MerchantGoodsStock
 * @author Amos
 */
@Mapper
public interface GoodsStockMapper extends BaseMapper<GoodsStockEntity> {

    /**
     * 查询商品库存分页方法
     * @param page 分页对象
     * @param param 查询条件
     * @return 分页商品库存列表
     */
    IPage<MerchantGoodsStockVO> queryGoodsStockPage(Page<MerchantGoodsStockVO> page, @Param("param") MerchantGoodsStockDTO param);

    /**
     * 查询商品的基本库存信息
     * @param param 商品库存查询参数
     * @return 商品库存对象
     */
    MerchantGoodsStockVO queryGoodsStock(@Param("param") MerchantGoodsStockDTO param);

    /**
     * 同步库存
     */
    List<Map<String,Object>> syncStock();


    /**
     * 根据商品id查询商品库存
     * @param goodsId 商品id
     * @return 商品库存
     */
    Integer selectGoodsStock(Integer goodsId);

    /**
     * 添加库存
     * @param goodsBaseEntity
     */
    void addGoodsStock(@Param("param") GoodsBaseEntity goodsBaseEntity, @Param("goodsStock") Integer goodsStock);
}




