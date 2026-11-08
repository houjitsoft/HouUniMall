package com.houjiang.goods.mall.service;

import com.houjiang.goods.mall.dto.MallGoodsAuditDTO;
import com.houjiang.goods.mall.dto.MallGoodsSearchDTO;
import com.houjiang.goods.mall.dto.MallGoodsUpdateDTO;
import com.houjiang.goods.mall.vo.MallGoodsDetailVO;
import com.houjiang.goods.mall.vo.MallGoodsListedVO;
import com.houjiang.goods.mall.vo.PageVO;
import com.houjiang.commons.dto.mall.MallSkuSnapshot;

/**
 * Mall 商品 Service
 * @author Amos
 */
public interface MallGoodsService {

    /**
     * @param skuId SKU主键
     * @return 处理结果
     */
    MallSkuSnapshot requireSaleableSku(Long skuId);

    /**
     * 商品列表（分页 + 多条件搜索）
     */
    PageVO<MallGoodsListedVO> list(MallGoodsSearchDTO search);

    /**
     * 商品详情
     */
    MallGoodsDetailVO detail(Integer id);

    /**
     * 商品详情（按语种覆盖名称/卖点/详情）
     */
    MallGoodsDetailVO detail(Integer id, String lang);

    /**
     * 商品编辑（id 为空走新增，否则更新）
     */
    Integer set(MallGoodsUpdateDTO dto);

    /**
     * 商品审核
     */
    void audit(MallGoodsAuditDTO dto);

    /**
     * 商品重审（强制将拒绝商品改回待审）
     */
    void reAudit(MallGoodsAuditDTO dto);

    /**
     * 商品删除（软删 delete_time）
     */
    void del(Integer id);

    /**
     * 上下架
     */
    void changeStatus(Integer id, Integer status);

    /**
     * 设为店内推荐 / 取消
     */
    void changeRecommend(Integer id, Integer shopRecommend);
}
