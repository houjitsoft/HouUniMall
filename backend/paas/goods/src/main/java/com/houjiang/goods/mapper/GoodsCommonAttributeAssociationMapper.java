package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.goods.GdbAttributeAssionParamVO;
import com.houjiang.commons.dto.goods.ManageGdsCommonPublishDTO;
import com.houjiang.commons.dto.goods.ManageGdsCommonUpdateDTO;
import com.houjiang.goods.entity.GoodsCommonAttributeAssociationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * (MerchantGoodsCommonAttributeAssociation)
 *
 * @since 2023-05-15 16:25:25
 * @author Amos
 */
@Mapper
public interface GoodsCommonAttributeAssociationMapper extends BaseMapper<GoodsCommonAttributeAssociationEntity> {

    /**
     * 根据commonId 查询商品的属性名
     * @param commonId
     * @return
     */
    List<String> queryGoodsAttributeContent(@Param("commonId") Integer commonId);

    /**
     * 查询当前分类下的属性名
     * @param catIds
     * @return
     */
    LinkedHashSet<Map<String,Object>> esSelectAttributes(@Param("catIds")LinkedHashSet<String> catIds);

    /**
     * 查询当前分类下的属性值
     * @param id
     * @return
     */
    LinkedHashSet<Map<String, Object>> esSelectAttContents(@Param("id") Integer id);

    /**
     * 查询商品属性
     * @param commonsId
     * @return
     */
    List<GdbAttributeAssionParamVO> selectCommonIds(Integer commonsId);

    /**
     * 属性id和商品id关联表插入
     * @param param
     */
    void insertAss(ManageGdsCommonPublishDTO param);

    /**
     * 删除商品属性关联表里的数据
     * @param commonId
     */
    void deleteAsscon(Integer commonId);

    /**
     * 添加对应的属性内容
     * @param param
     */
    void insertAssContent(ManageGdsCommonUpdateDTO param);

}

