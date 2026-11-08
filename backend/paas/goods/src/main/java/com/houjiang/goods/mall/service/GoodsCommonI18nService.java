package com.houjiang.goods.mall.service;

import com.houjiang.goods.mall.dto.GoodsCommonI18nSaveDTO;
import com.houjiang.goods.mall.entity.GoodsCommonI18n;

import java.util.List;
import java.util.Map;

/**
 * 商品多语言内容
 */
public interface GoodsCommonI18nService {

    /** 某商品全部语种（含草稿） */
    List<GoodsCommonI18n> listByCommonId(Integer commonId);

    /** 批量保存/覆盖某商品多语种 */
    void save(GoodsCommonI18nSaveDTO dto);

    /** 已发布译文；无则 null */
    GoodsCommonI18n getPublished(Integer commonId, String langCode);

    /** 批量取已发布名称：commonId -> commonName */
    Map<Integer, String> mapPublishedNames(List<Integer> commonIds, String langCode);

    /** 中文主数据变更后标记其它语种 stale */
    void markStale(Integer commonId);
}
