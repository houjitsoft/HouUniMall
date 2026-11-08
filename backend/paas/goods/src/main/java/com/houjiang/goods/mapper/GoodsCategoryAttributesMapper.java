package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.commons.dto.goods.GoodsAttrDTO;
import com.houjiang.commons.vo.goods.GoodsCategoryAttributesVO;
import com.houjiang.goods.entity.GoodsCategoryAttributesEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品设置的类目属性表(GoodsCategoryAttributes)表数据库访问层
 *
 * @author Amos
 * @since 2024-11-01 10:17:55
 */
@Mapper
public interface GoodsCategoryAttributesMapper extends BaseMapper<GoodsCategoryAttributesEntity> {

    /**
     * @return 处理结果
     */
    @MapKey("attributeName")
    List<Map<String, Object>> selectGoodsAttributeGroupList(@Param("param") GoodsAttrDTO dto);

    /**
     * @return 处理结果
     */
    List<GoodsCategoryAttributesVO> selectGoodsAttributeList(@Param("attrName") String attrName);

    /**
     * @return 处理结果
     */
    List<Integer> selectCommonIdListByAttrs(@Param("attrList") List<Map<String, Object>> attrList,
                                            @Param("size") Integer size);
}
