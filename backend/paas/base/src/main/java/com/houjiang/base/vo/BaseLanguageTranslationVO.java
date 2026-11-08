package com.houjiang.base.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语言包翻译VO
 *
 * @description 语言包翻译返回对象
 * @author Amos
 * @since 2026-01-07
 */
@Data
public class BaseLanguageTranslationVO {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 中文原文
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
     * 实际显示的译文（优先返回修正后的译文，否则返回原始译文）
     */
    private String translation;

    /**
     * 是否已修正: 0-未修正, 1-已修正
     */
    private Integer isCorrected;

    /**
     * 翻译来源: AI/Manual
     */
    private String translationSource;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
