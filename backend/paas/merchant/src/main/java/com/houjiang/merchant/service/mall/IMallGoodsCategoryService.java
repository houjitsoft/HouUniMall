package com.houjiang.merchant.service.mall;


import com.houjiang.merchant.validate.mall.MallGoodsCategorySearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryListedVo;

import java.util.List;

/**
 * 商品分类服务接口类
 * @author Amos
 */
public interface IMallGoodsCategoryService {

    /**
     * 商品分类列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCategoryListedVo>
     */
    List<MallGoodsCategoryListedVo> list(MallGoodsCategorySearchValidate searchValidate);

    /**
     * 商品分类详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallGoodsCategoryDetailVo
     */
    MallGoodsCategoryDetailVo detail(Integer id);

    /**
     * 根据商品ID获取分类
     * @param goodsId
     * @return
     */
    List<String> getCateNamesByGoodsId(Integer goodsId);
}
