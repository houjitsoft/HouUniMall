package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseLanguageTranslationDTO;
import com.houjiang.base.entity.BaseLanguageTranslationEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * 语言包翻译Service接口
 *
 * @description 语言包翻译业务逻辑接口
 * @author Amos
 * @since 2026-01-07
 */
public interface BaseLanguageTranslationService extends IService<BaseLanguageTranslationEntity> {

    /**
     * 分页查询语言包翻译列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    ResponseResult queryLanguageList(BaseLanguageTranslationDTO param);

    /**
     * 修正译文
     *
     * @param param 修正参数（包含id和修正后的译文）
     * @return 操作结果
     */
    ResponseResult correctTranslation(BaseLanguageTranslationDTO param);

    /**
     * 根据中文原文和语言代码获取翻译
     *
     * @param cnText 中文原文
     * @param languageCode 语言代码
     * @return 翻译结果
     */
    ResponseResult getTranslation(String cnText, String languageCode);

    /**
     * 批量导入翻译数据
     *
     * @param translations 翻译数据列表
     * @return 导入结果
     */
    ResponseResult batchImport(java.util.List<BaseLanguageTranslationEntity> translations);

    /**
     * 删除翻译记录
     *
     * @param id 记录ID
     * @return 删除结果
     */
    ResponseResult deleteTranslation(Integer id);
}
