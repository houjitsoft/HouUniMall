package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsImagesVO;
import com.houjiang.commons.vo.goods.ImageDetailVO;
import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.entity.GoodsCommonEntity;
import com.houjiang.goods.entity.GoodsImagesEntity;
import com.houjiang.goods.vo.GdsImageDetailResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName GoodsImagesMapper
 * @Description  商品图片
 * @Author Amos
 * @Date 2023-05-25 15:07:41
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsImagesMapper extends BaseMapper<GoodsImagesEntity> {

    /**
     * 查询商品图片信息
     *
     * @param goodsId
     * @param shopId
     * @return
     */
    List<GdsImageDetailResultVO> selectByCommonId(@Param("goodsId") String goodsId, @Param("shopId") Integer shopId);

    /**
     * 查询商品图片
     *
     * @param goodsId
     * @return
     */
    List<GdbMerchantGoodsImagesVO> selectByCommonIdImg(@Param("goodsId") Integer goodsId);

    /**
     * 删除商品图片
     *
     * @param commonIds
     * @return
     */
    int deleteByCommonid(@Param("commonIds") List<Integer> commonIds);

    /**
     * 插入商品图片
     *
     * @param record
     * @return
     */
    int insertSelective(GdbMerchantGoodsImagesVO record);

    /**
     * 删除主图
     *
     * @param removeGoodsImageIds
     * @param commonId
     */
    void deleteByGoodsImageIds(@Param("removeGoodsImageIds") List<Integer> removeGoodsImageIds, @Param("commonId") Integer commonId);

    /**
     * 添加商品图片
     *
     * @param goodsImages
     */
    void insertBatch(@Param("goodsImages") List<GdbMerchantGoodsImagesVO> goodsImages);

    /**
     * 修改商品图片
     *
     * @param goodsImages
     */
    void updateBatch(@Param("goodsImages") List<GdbMerchantGoodsImagesVO> goodsImages);

    /**
     * 查询图片
     *
     * @param commonId
     * @param shopId
     * @param goodsId
     * @return
     */
    List<GdbMerchantGoodsImagesVO> selectByCommonId3(@Param("commonId") Integer commonId, @Param("shopId") Integer shopId, @Param("goodsId") Integer goodsId);

    /**
     * 查询商品副图
     *
     * @param commonId
     * @return
     */
    List<GdbMerchantGoodsImagesVO> selectByCommonId2(@Param("commonId") Integer commonId);

    /**
     * 插入商品图片
     *
     * @param goodsImages
     */
    void insertBatch2(@Param("goodsImages") List<GdbMerchantGoodsImagesVO> goodsImages);

    void deleteByCommonId(@Param("commonId") Integer commonId);

    /**
     * 查询供应商商品 图片
     *
     * @param goodsId
     * @return
     */
    List<String> querySupplierGoodsImage(@Param("goodsId") Integer goodsId);

    /**
     * 查询经销商品商品信息
     *
     * @param dealerGoodsId
     * @return
     */
    List<GoodsImagesEntity> queryDealerGoodsImage(@Param("goodsId") Integer dealerGoodsId);

    /**
     * 更新经销商商品 图片
     *
     * @param merchantGoodsImagesEntity
     */
    void updateDealerGoodsImage(@Param("param") GoodsImagesEntity merchantGoodsImagesEntity);

    /**
     * 添加图片
     * @param goodsImage
     */
    void addGoodsImages(@Param("goodsImage")ImageDetailVO goodsImage,@Param("index") Integer index,  @Param("goodsBaseEntity") GoodsBaseEntity goodsBaseEntity);
}

