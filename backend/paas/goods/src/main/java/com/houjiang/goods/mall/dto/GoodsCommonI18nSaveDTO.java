package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 保存商品多语言（单语种或批量）
 */
@Data
public class GoodsCommonI18nSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer commonId;
    private List<Item> items;

    @Data
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private String langCode;
        private String commonName;
        private String promotionTips;
        private String commonBody;
        private String specNameJson;
        private String specValueJson;
        /** manual / ai / corrected，默认 manual */
        private String source;
        /** draft / published，默认 published */
        private String status;
    }
}
