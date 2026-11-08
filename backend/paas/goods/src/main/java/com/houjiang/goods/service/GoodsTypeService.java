package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.GoodsTypeDTO;
import com.houjiang.goods.dto.MerchantGoodsTypeDTO;
import com.houjiang.goods.entity.GoodsTypeEntity;

/**
 * @author Amos
 */
public interface GoodsTypeService extends IService<GoodsTypeEntity> {


    /**
     * 查询类型列表
     *
     * @param merchantGoodsTypeReq
     * @return
     */
    ResponseResult queryGoodsTypeList(MerchantGoodsTypeDTO merchantGoodsTypeReq);

    /**
     * 通过类型id查询
     *
     * @param merchantGoodsTypeReq
     * @return
     */
    ResponseResult queryGoodsTypeById(MerchantGoodsTypeDTO merchantGoodsTypeReq);

    /**
     * 新增 修改 商品类型
     *
     * @param goodsTypeAddparam
     * @return
     */
    ResponseResult addGoodsType(GoodsTypeDTO goodsTypeAddparam);

    /**
     * 查询 商品类型详情
     *
     * @return
     */
    ResponseResult addTypeQuery();
}
