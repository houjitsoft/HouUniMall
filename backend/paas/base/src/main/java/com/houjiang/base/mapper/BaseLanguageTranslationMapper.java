package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.base.dto.BaseLanguageTranslationDTO;
import com.houjiang.base.entity.BaseLanguageTranslationEntity;
import com.houjiang.base.vo.BaseLanguageTranslationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 语言包翻译Mapper接口
 *
 * @description 语言包翻译数据访问层
 * @author Amos
 * @since 2026-01-07
 */
@Mapper
public interface BaseLanguageTranslationMapper extends BaseMapper<BaseLanguageTranslationEntity> {

    /**
     * 分页查询语言包翻译列表
     *
     * @param page 分页对象
     * @param param 查询条件
     * @return 分页结果
     */
    IPage<BaseLanguageTranslationVO> queryLanguageList(Page<BaseLanguageTranslationVO> page, @Param("param") BaseLanguageTranslationDTO param);

    /**
     * 根据中文原文和语言代码查询翻译
     *
     * @param cnText 中文原文
     * @param languageCode 语言代码
     * @return 翻译信息
     */
    BaseLanguageTranslationVO getTranslation(@Param("cnText") String cnText, @Param("languageCode") String languageCode);

    /**
     * 批量插入翻译数据
     *
     * @param translations 翻译数据列表
     * @return 插入的行数
     */
    int batchInsert(@Param("translations") List<BaseLanguageTranslationEntity> translations);
}
