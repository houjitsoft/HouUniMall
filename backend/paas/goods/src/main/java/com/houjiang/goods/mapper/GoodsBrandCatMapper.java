package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.dto.GoodsBrandDTO;
import com.houjiang.goods.entity.GoodsBrandCatEntity;
import com.houjiang.goods.entity.GoodsBrandEntity;
import com.houjiang.goods.vo.GoodsBrandCatParamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsBrandCatMapper
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:10
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsBrandCatMapper extends BaseMapper<GoodsBrandCatEntity> {

    /**
     * 查询品牌分类关联信息
     * @param objectMap
     * @return
     */
    List<GoodsBrandCatParamVO> selectBrandCatList(@Param("param") Map<String, Object> objectMap);

    /**
     * 修改品牌分类关联信息
     * @param param
     */
    void updateBatchBrandCat(@Param("param") GoodsBrandDTO param);

}
