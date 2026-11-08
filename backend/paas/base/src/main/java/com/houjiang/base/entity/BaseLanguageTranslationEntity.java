package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 语言包翻译实体类
 *
 * @description 用于管理多语言翻译数据，支持AI翻译和人工校正
 * @author Amos
 * @since 2026-01-07
 */
@Data
@TableName("hou_base_language_translation")
public class BaseLanguageTranslationEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 中文原文
     */
    @TableField("cn_text")
    private String cnText;

    /**
     * 语言代码(en/ja/ko/fr/de)
     */
    @TableField("language_code")
    private String languageCode;

    /**
     * 原始译文(AI翻译)
     */
    @TableField("original_translation")
    private String originalTranslation;

    /**
     * 修正后的译文(人工校正)
     */
    @TableField("corrected_translation")
    private String correctedTranslation;

    /**
     * 是否已修正: 0-未修正, 1-已修正
     */
    @TableField("is_corrected")
    private Integer isCorrected;

    /**
     * 翻译来源: AI/Manual
     */
    @TableField("translation_source")
    private String translationSource;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
