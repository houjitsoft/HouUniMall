package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.FoodsExhibitionHallListDTO;
import com.houjiang.goods.entity.GoodsFoodExhibitionHallEntity;

/**
 * @author Amos
 */
public interface GoodsFoodExhibitionHallService extends IService<GoodsFoodExhibitionHallEntity> {


    /**
     * 查询特色馆详情
     *
     * @param param
     * @return
     */
    ResponseResult queryFoodsHallDetail(FoodsExhibitionHallListDTO param);

    /**
     * 修改特色馆
     *
     * @param param
     * @return
     */
    ResponseResult updateFoodsHallDetail(FoodsExhibitionHallListDTO param);

    /**
     * 删除特色馆
     *
     * @param param
     * @return
     */
    ResponseResult deleteFoodsHallDetail(FoodsExhibitionHallListDTO param);

    /**
     * 查询特色馆列表
     *
     * @param param
     * @return
     */
    ResponseResult queryFoodsHallList(FoodsExhibitionHallListDTO param);


    /**
     * 查询轮播图图片、特色馆信息
     *
     * @return
     */
    ResponseResult queryFoodExhibitionHall();

    /**
     * 特产馆详情页
     *
     * @param exhibitionHallId
     * @return
     */
    ResponseResult foodExhibitionHallDetail(Integer exhibitionHallId);

}
