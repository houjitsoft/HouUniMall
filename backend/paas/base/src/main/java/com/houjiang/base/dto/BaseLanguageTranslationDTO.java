package com.houjiang.base.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 语言包翻译DTO
 *
 * @description 语言包翻译查询和操作参数
 * @author Amos
 * @since 2026-01-07
 */
@Data
public class BaseLanguageTranslationDTO extends PageHelper {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 中文原文（支持模糊查询）
     */
    private String cnText;

    /**
     * 语言代码(en/ja/ko/fr/de)
     */
    private String languageCode;

    /**
     * 原始译文(AI翻译)
     */
    private String originalTranslation;

    /**
     * 修正后的译文(人工校正)
     */
    private String correctedTranslation;

    /**
     * 是否已修正: 0-未修正, 1-已修正
     */
    private Integer isCorrected;

    /**
     * 翻译来源: AI/Manual
     */
    private String translationSource;

    /**
     * 搜索关键词（用于前端搜索）
     */
    private String keyword;
}
