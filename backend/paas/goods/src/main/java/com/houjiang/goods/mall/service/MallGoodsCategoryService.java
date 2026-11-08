package com.houjiang.goods.mall.service;

import com.houjiang.goods.mall.dto.MallGoodsCategoryDTO;
import com.houjiang.goods.mall.vo.MallGoodsCategoryVO;

import java.util.List;

/**
 * Mall 商品分类 Service
 * @author Amos
 */
public interface MallGoodsCategoryService {

    /**
     * 分类列表（树形）
     */
    List<MallGoodsCategoryVO> list(String name);

    /**
     * 平铺列表（用于下拉）
     */
    List<MallGoodsCategoryVO> otherList();

    /**
     * 分类详情
     */
    MallGoodsCategoryVO detail(Integer id);

    /**
     * 新增
     */
    Integer add(MallGoodsCategoryDTO dto);

    /**
     * 编辑
     */
    void edit(MallGoodsCategoryDTO dto);

    /**
     * 删除（软删）
     */
    void del(Integer id);

    /**
     * 切换显示状态
     */
    void changeStatus(Integer id);
}
