package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 一键翻译请求（基于中文主数据生成草稿/已发布译文）
 */
@Data
public class GoodsCommonI18nAutoTranslateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer commonId;
    /** 目标语种，如 en/ja */
    private List<String> langCodes;
    /** 是否直接 published，默认 true */
    private Boolean publish;
}
