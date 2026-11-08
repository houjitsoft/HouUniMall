package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.GoodsTypeDTO;
import com.houjiang.goods.dto.MerchantGoodsTypeDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName GoodsTypeController
 * @Description
 * @Author Amos
 * @Date 2023/7/28 9:18
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/type")
public class GoodsTypeController extends BaseController{

    /**
     * 查询类型列表
     *
     * @param merchantGoodsTypeReq
     * @return
     */
    @RequestMapping("query/list")
    public ResponseResult queryGoodsTypeList(@RequestBody MerchantGoodsTypeDTO merchantGoodsTypeReq) {
        return goodsTypeService.queryGoodsTypeList(merchantGoodsTypeReq);
    }

    /**
     * 通过类型id查询
     *
     * @param merchantGoodsTypeReq
     * @return
     */
    @RequestMapping("id/query")
    public ResponseResult queryGoodsTypeById(@RequestBody MerchantGoodsTypeDTO merchantGoodsTypeReq) {
        return goodsTypeService.queryGoodsTypeById(merchantGoodsTypeReq);
    }

    /**
     * 新增 修改 商品类型
     *
     * @param goodsTypeAddparam
     * @return
     */
    @RequestMapping("/add")
    public ResponseResult addGoodsType(@RequestBody GoodsTypeDTO goodsTypeAddparam) {
        return goodsTypeService.addGoodsType(goodsTypeAddparam);
    }

    /**
     * 查询 商品类型详情
     *
     * @return
     */
    @RequestMapping("/addTypeQuery")
    public ResponseResult addTypeQuery() {
        return goodsTypeService.addTypeQuery();
    }
}
