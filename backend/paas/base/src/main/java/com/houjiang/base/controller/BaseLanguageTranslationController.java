package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseLanguageTranslationDTO;
import com.houjiang.base.entity.BaseLanguageTranslationEntity;
import com.houjiang.base.service.BaseLanguageTranslationService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 语言包翻译Controller
 *
 * @description 语言包翻译管理接口
 * @author Amos
 * @since 2026-01-07
 */
@RestController
@RequestMapping("base/languageTranslation")
public class BaseLanguageTranslationController {

    @Autowired
    private BaseLanguageTranslationService languageTranslationService;

    /**
     * 分页查询语言包翻译列表
     *
     * @param param 查询参数
     * @return ResponseResult 翻译列表
     */
    @PostMapping("/list")
    public ResponseResult queryLanguageList(@RequestBody BaseLanguageTranslationDTO param) {
        return languageTranslationService.queryLanguageList(param);
    }

    /**
     * 修正译文
     *
     * @param param 修正参数（包含id和修正后的译文）
     * @return ResponseResult 操作结果
     */
    @PostMapping("/correct")
    public ResponseResult correctTranslation(@RequestBody BaseLanguageTranslationDTO param) {
        return languageTranslationService.correctTranslation(param);
    }

    /**
     * 根据中文原文和语言代码获取翻译
     *
     * @param cnText 中文原文
     * @param languageCode 语言代码
     * @return ResponseResult 翻译结果
     */
    @GetMapping("/get")
    public ResponseResult getTranslation(@RequestParam("cnText") String cnText,
                                        @RequestParam("languageCode") String languageCode) {
        return languageTranslationService.getTranslation(cnText, languageCode);
    }

    /**
     * 批量导入翻译数据
     *
     * @param translations 翻译数据列表
     * @return ResponseResult 导入结果
     */
    @PostMapping("/batchImport")
    public ResponseResult batchImport(@RequestBody List<BaseLanguageTranslationEntity> translations) {
        return languageTranslationService.batchImport(translations);
    }

    /**
     * 删除翻译记录
     *
     * @param id 记录ID
     * @return ResponseResult 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteTranslation(@RequestParam("id") Integer id) {
        return languageTranslationService.deleteTranslation(id);
    }
}
