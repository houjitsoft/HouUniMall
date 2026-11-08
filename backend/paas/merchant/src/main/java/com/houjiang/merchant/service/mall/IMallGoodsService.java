package com.houjiang.merchant.service.mall;


import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.*;
import com.houjiang.merchant.vo.mall.*;

/**
 *  商品表接口类
 */
public interface IMallGoodsService {

    /**
     * TODO 补充方法说明
     * @param num TODO
     * @param shopId TODO
     * @return TODO
     */
    JSONArray getGoodsSaleRank(Integer num, Integer shopId);
    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallGoods getById(Integer id);

    /**
     * @notes 是否有商品 正在使用 商家商品分类
     * @param int shopGoodsCategoryId
     * @return bool
     * @author Amos
     */
    Boolean checkShopGoodsCategoryHas(Integer shopGoodsCategoryId);

    /**
     * @notes 商家商品分类 对应商品数
     * @return array
     * @author Amos
     */
    Long getShopGoodsCategoryCountsCateId(Integer shopId, Integer shopCategoryId);


    /**
     * 商品列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallGoodsListedVo>
     */
    PageResult<MallGoodsListedVo> list(PageValidate pageValidate, MallGoodsSearchValidate searchValidate);

    /**
     * 商品详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallGoodsDetailVo
     */
    MallGoodsDetailVo detail(Integer id);

    /**
     * 商品新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    Integer add(MallGoodsCreateValidate createValidate);

    /**
     * 商品编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    String edit(MallGoodsUpdateValidate updateValidate);

    /**
     * 商品删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 其他列表
     * @return
     */
    MallGoodsOtherListVo otherList();

    /**
     * @notes 商品上下架
     * @author Amos
     */
    void status(MallShopGoodsStatusValidate statusValidate);

    /**
     * 获取优惠券商品列表
     * @param id
     * @return
     */
    MallCouponGoodsDetailVo getCouponGoods(Integer id);

    /**
     * @notes 参与/不参与折扣
     * @author Amos
     */
    void discountSet(MallShopGoodsDiscountSetValidate setValidate);

    /**
     * @notes 折扣详情
     * @author Amos
     */
    MallGoodsDiscountDetailVo discountDetail(Integer id);

    /**
     * 保存折扣
     * @param setValidate
     */
    void discountSave(MallShopGoodsDiscountSaveValidate setValidate);

    /**
     * 是否参与分销
     * @param setValidate
     */
    void distributionSet(MallShopGoodsDistributionSetValidate setValidate);

    /**
     * 商品分销详情
     * @param id
     * @return
     */
    MallGoodsDistributionDetailVo distributionDetail(Integer id);

    /**
     * 保存分销配置
     * @param setValidate
     */
    void distributionSave(MallShopGoodsDistributionSaveValidate setValidate);
}
