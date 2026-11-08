package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.dto.FoodsExhibitionHallListDTO;
import com.houjiang.goods.entity.GoodsFoodExhibitionHallEntity;
import com.houjiang.goods.vo.WapGoodsFoodExhibitionHallVO;
import com.houjiang.goods.vo.WebIndexSpecialtysHallVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.goods.entity.GoodsFoodExhibitionHall
 * @author Amos
 */
@Mapper
public interface GoodsFoodExhibitionHallMapper extends BaseMapper<GoodsFoodExhibitionHallEntity> {

    /**
     * 新增食品大厅详情
     *
     * @param param
     */
    void insertFoodsHallDetail(@Param("param") FoodsExhibitionHallListDTO param);
    /**
     * 查询特色馆详情
     * @param exhibitionHallId
     * @return
     */
    WapGoodsFoodExhibitionHallVO selectFoodExhibitionHallById(@Param("exhibitionHallId")Integer exhibitionHallId);
    /**
     * 查询所有特色馆
     * @return
     */
    List<WebIndexSpecialtysHallVO> selectByAll();
}




