package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.FoodsExhibitionHallListDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName GoodsFoodExhibitionHallController
 * @Description
 * @Author Amos
 * @Date 2023/7/31 13:54
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/food/exhibitionHall")
public class GoodsFoodExhibitionHallController extends BaseController{


    /**
     * 查询特色馆详情
     *
     * @param param
     * @return
     */
    @PostMapping("/queryDetail")
    public ResponseResult queryFoodsHallDetail(@RequestBody FoodsExhibitionHallListDTO param) {
        ResponseResult result = goodsFoodExhibitionHallService.queryFoodsHallDetail(param);
        return result;
    }


    /**
     * 修改特色馆
     *
     * @param param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateFoodsHallDetail(@RequestBody FoodsExhibitionHallListDTO param) {
        ResponseResult result = goodsFoodExhibitionHallService.updateFoodsHallDetail(param);
        return result;
    }

    /**
     * 删除特色馆
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteFoodsHallDetail(@RequestBody FoodsExhibitionHallListDTO param) {
        ResponseResult result = goodsFoodExhibitionHallService.deleteFoodsHallDetail(param);
        return result;
    }


    /**
     * 查询特色馆列表
     *
     * @param param
     * @return
     */
    @PostMapping("/queryList")
    public ResponseResult queryFoodsHallList(@RequestBody FoodsExhibitionHallListDTO param) {
        ResponseResult result = goodsFoodExhibitionHallService.queryFoodsHallList(param);
        return result;
    }
    /**
     * 查询轮播图图片、特色馆信息
     */
    @GetMapping(value = "/foodExhibitionHall")
    public ResponseResult queryFoodExhibitionHall() {

        return goodsFoodExhibitionHallService.queryFoodExhibitionHall();
    }

    /**
     * 特产馆详情页
     *
     * @param exhibitionHallId
     * @return
     */
    @GetMapping(value = "/foodExhibitionHallDetail")
    public ResponseResult foodExhibitionHallDetail(@RequestParam Integer exhibitionHallId) {
        return goodsFoodExhibitionHallService.foodExhibitionHallDetail(exhibitionHallId);
    }
}
