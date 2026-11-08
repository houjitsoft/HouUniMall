package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.GoodsAttributeContentEntity;
import com.houjiang.goods.entity.GoodsAttributeEntity;
import com.houjiang.goods.vo.GoodsAttributeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsAttributeContentMapper
 * @Description  商品属性内容
 * @Author Amos
 * @Date 2023-05-25 18:10:08
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsAttributeContentMapper extends BaseMapper<GoodsAttributeContentEntity> {

  /**
   * 查属性内容
   * @param attributeContentId
   * @return
   */
  GoodsAttributeVO selectAttContentId(@Param("id")Integer attributeContentId);


    /**
     * @return 处理结果
     */
    LinkedHashSet<Map<String, Object>> selectAttContents(@Param("id") Integer id);

    /**
     * @return 处理结果
     */
    List<GoodsAttributeContentEntity> queryAttributeContentList(@Param("id")Integer id);

  /**
   * 存入属性内容
   * @param goodsParam
   */
  void insertContent(GoodsAttributeEntity goodsParam);

  /**
   * 删除 属性内容
   * @param id
   */
  void deleteContent(@Param("id")Integer id);
  /**
   * 删除 属性内容
   * @param id
   */
  void deleteAttriContents(@Param("attriConId")Integer id);
}
