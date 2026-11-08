package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.goods.entity.GoodsAttributeEntity;
import com.houjiang.goods.vo.GoodsAttributeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsAttributeMapper
 * @Description 商品属性
 * @Author Amos
 * @Date 2023-05-25 18:08:18
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsAttributeMapper extends BaseMapper<GoodsAttributeEntity> {

    /**
     * 查询属性名
     * @param attributeId
     * @return
     */
    GoodsAttributeVO selectAttributeId(@Param("id")Integer attributeId);

    /**
     * 查询商品属性
     * @param webCatMenus
     * @return
     */
    LinkedHashSet<Map<String, Object>> selectAttributes(@Param("list")List<WebCatMenuVO> webCatMenus);

    /**
     * 查询当前分类 已经添加的分类属性
     * @param catId
     * @return
     */
    List<GoodsAttributeEntity> selectAttribute(Integer catId);

    /**
     * 删除分类属性名
     * @param id
     */
    void deleteAttris(Integer id);

    /**
     * 存入属性名
     * @param goodsParam
     */
    void insertAttribute(GoodsAttributeEntity goodsParam);
}
