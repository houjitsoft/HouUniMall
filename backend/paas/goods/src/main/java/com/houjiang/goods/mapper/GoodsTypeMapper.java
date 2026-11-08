package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.dto.GoodsTypeDTO;
import com.houjiang.goods.dto.MerchantGoodsTypeDTO;
import com.houjiang.goods.entity.GoodsTypeEntity;
import com.houjiang.goods.vo.GoodsBrandByCatIdVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsType
 * @author Amos
 */
@Mapper
public interface GoodsTypeMapper extends BaseMapper<GoodsTypeEntity> {

    /**
     * 分页查询商品
     *
     * @param page
     * @param merchantGoodsTypeReq
     * @return
     */
    IPage<GoodsTypeEntity> queryGoodsTypePage(IPage<GoodsTypeEntity> page, @Param("param") MerchantGoodsTypeDTO merchantGoodsTypeReq);

    /**
     * 查询商品
     *
     * @param merchantGoodsTypeReq
     * @return
     */
    List<GoodsTypeEntity> queryGoodsTypeList(@Param("param") MerchantGoodsTypeDTO merchantGoodsTypeReq);

    /**
     * 查询商品
     *
     * @param typeId
     * @return
     */
    GoodsTypeEntity selectTypeById(@Param("typeId") Integer typeId);

    /**
     * 通过名称查询
     *
     * @param typeName
     * @return
     */
    Integer selectByEmptyName(@Param("typeName") String typeName);

    /**
     * 新增类型
     *
     * @param goodsTypeAddparam
     */
    void insertType(@Param("param") GoodsTypeDTO goodsTypeAddparam);

    /**
     * 更新类型
     *
     * @param goodsTypeAddparam
     */
    void updateType(@Param("param") GoodsTypeDTO goodsTypeAddparam);

    /**
     * 删除主键
     *
     * @param typeId
     */
    void deleteByPrimaryKey(@Param("typeId") Integer typeId);

    /**
     * 查询所有品牌
     *
     * @return
     */
    List<GoodsBrandByCatIdVO> selectAllBrand();
}




