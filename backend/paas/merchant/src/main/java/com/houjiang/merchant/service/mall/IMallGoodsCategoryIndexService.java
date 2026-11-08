package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallGoodsCategoryIndexSearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryIndexDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryIndexListedVo;

import java.util.List;

/**
 * 商品分类关联服务接口类
 * @author Amos
 */
public interface IMallGoodsCategoryIndexService {

    /**
     * 商品分类关联列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsCategoryIndexListedVo>
     */
    PageResult<MallGoodsCategoryIndexListedVo> list(PageValidate pageValidate, MallGoodsCategoryIndexSearchValidate searchValidate);

    /**
     * 商品分类关联详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallGoodsCategoryIndexDetailVo
     */
    MallGoodsCategoryIndexDetailVo detail(Integer id);

    /**
     * 根据商品ID获取分类
     * @param goodsId
     * @return
     */
    List<Integer> getCateIdsByGoodsId(Integer goodsId);

    /**
     * 根据ID返回商品ID
     * @param categoryId
     * @return
     */
    List<Integer> getGoodsIdsByCid(Integer categoryId);
}
