package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.GoodsBrandDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsBrandController
 * @Description
 * @Author Amos
 * @Date 2023/7/5 14:29
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/brand")
public class GoodsBrandController extends BaseController{


    /**
     * 查询所有品牌
     *
     * @param
     * @return
     */
    @PostMapping("/queryList")
    public ResponseResult queryGoodsBrand() {
        ResponseResult result = goodsBrandService.queryBrandGoods();
        return result;
    }

    /*
     *查询品牌名称是否重复
     * */
    @PostMapping("/queryBrandName")
    public ResponseResult selectBrandName(@RequestBody Map<String, Object> map) {
        ResponseResult result = goodsBrandService.selectBrandName(map);
        return result;
    }

    /**
     * 品牌 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteBrand(@RequestBody GoodsBrandDTO dto) {
        ResponseResult result = goodsBrandService.deleteBrand(dto);
        return result;
    }


    /**
     * 品牌 新增 或 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ResponseResult updateBrand(@RequestBody GoodsBrandDTO dto) {
        ResponseResult result = goodsBrandService.updateBrand(dto);
        return result;
    }

    /**
     * 查询品牌列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryBrandList(@RequestBody GoodsBrandDTO dto) {
        ResponseResult result = goodsBrandService.queryBrandList(dto);
        return result;
    }

    /**
     * 根据品牌id查询商品品牌
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryGoodsBrandById(@RequestBody GoodsBrandDTO param) {
        ResponseResult result = goodsBrandService.queryGoodsBrandById(param);
        return result;
    }


    /**
     * 商家中心-商品品牌查询列表
     *
     * @param param
     * @return
     */
    @PostMapping("/businrss/query/list")
    public ResponseResult queryGoodsBrandList(@RequestBody GoodsBrandDTO param) {
        ResponseResult result = goodsBrandService.queryGoodsBrandList(param);
        return result;
    }

    /**
     * 获取分类关联品牌
     *
     * @param param
     * @return
     */
    @PostMapping("/getBrandList")
    public ResponseResult findListByName(@RequestBody GoodsBrandDTO param) {
        ResponseResult result = goodsBrandService.findListByName(param);
        return result;
    }
}
