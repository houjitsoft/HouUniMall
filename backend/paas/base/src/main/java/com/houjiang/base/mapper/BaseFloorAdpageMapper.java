package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.dto.FloorDTO;
import com.houjiang.base.entity.BaseFloorAdpageEntity;
import com.houjiang.base.vo.GoodsIdsQueryListVO;
import com.houjiang.commons.vo.goods.GoodsIdsVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.houjiang.base.entity.BaseFloorAdpage
 * @author Amos
 */
@Mapper
public interface BaseFloorAdpageMapper extends BaseMapper<BaseFloorAdpageEntity> {


    /**
     * 更新楼层广告页
     *
     * @param updateFloorAdpage
     */
    void updateFloorAdpage(@Param("param") List<FloorDTO> updateFloorAdpage);

    /**
     * 添加楼层广告页
     *
     * @param addFloorAdpage
     */
    void insertFloorAdpage(@Param("param") List<FloorDTO> addFloorAdpage);

    /**
     * 修改
     *
     * @param floorAdpageId
     * @param floorStatus
     */
    void pcIndexFloorUpdate(@Param("floorAdpageId") Integer floorAdpageId, @Param("floorStatus") Integer floorStatus);

    /**
     * 修改配置
     *
     * @param pageType
     * @param pageStart
     */
    void pcIndexConfigUpdate(@Param("pageType") Integer pageType, @Param("pageStart") Integer pageStart);
}




