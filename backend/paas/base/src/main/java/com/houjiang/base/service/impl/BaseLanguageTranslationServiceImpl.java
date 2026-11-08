package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseLanguageTranslationDTO;
import com.houjiang.base.entity.BaseLanguageTranslationEntity;
import com.houjiang.base.mapper.BaseLanguageTranslationMapper;
import com.houjiang.base.service.BaseLanguageTranslationService;
import com.houjiang.base.vo.BaseLanguageTranslationVO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.querypage.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 语言包翻译Service实现类
 *
 * @description 语言包翻译业务逻辑实现
 * @author Amos
 * @since 2026-01-07
 */
@Service
public class BaseLanguageTranslationServiceImpl extends ServiceImpl<BaseLanguageTranslationMapper, BaseLanguageTranslationEntity>
        implements BaseLanguageTranslationService {

    /**
     * 分页查询语言包翻译列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @Override
    public ResponseResult<PageUtils<BaseLanguageTranslationVO>> queryLanguageList(BaseLanguageTranslationDTO param) {
        try {
            // 初始化分页查询参数
            Page<BaseLanguageTranslationVO> page = new Page<>(param.getPage(), param.getLimit());
            // 执行分页查询
            IPage<BaseLanguageTranslationVO> result = this.baseMapper.queryLanguageList(page, param);
            // 构造并返回成功响应结果，包含分页数据
            return ResponseResult.success(PageUtils.getPage(result));
        } catch (Exception e) {
            log.error("查询语言包列表失败", e);
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR);
        }
    }

    /**
     * 修正译文
     *
     * @param param 修正参数（包含id和修正后的译文）
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult correctTranslation(BaseLanguageTranslationDTO param) {
        try {
            // 参数校验
            if (param.getId() == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "翻译ID不能为空");
            }
            if (StringUtils.isEmpty(param.getCorrectedTranslation())) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "修正后的译文不能为空");
            }

            // 查询原记录
            BaseLanguageTranslationEntity entity = this.baseMapper.selectById(param.getId());
            if (entity == null) {
                return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "翻译记录不存在");
            }

            // 更新修正译文
            entity.setCorrectedTranslation(param.getCorrectedTranslation());
            entity.setIsCorrected(1);
            entity.setTranslationSource("Manual");

            int updateResult = this.baseMapper.updateById(entity);
            if (updateResult > 0) {
                return ResponseResult.success("修正成功");
            }
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        } catch (Exception e) {
            log.error("修正译文失败", e);
            return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
        }
    }

    /**
     * 根据中文原文和语言代码获取翻译
     *
     * @param cnText 中文原文
     * @param languageCode 语言代码
     * @return 翻译结果
     */
    @Override
    public ResponseResult<BaseLanguageTranslationVO> getTranslation(String cnText, String languageCode) {
        try {
            if (StringUtils.isEmpty(cnText) || StringUtils.isEmpty(languageCode)) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER);
            }

            BaseLanguageTranslationVO vo = this.baseMapper.getTranslation(cnText, languageCode);
            if (vo != null) {
                return ResponseResult.success(vo);
            }
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR.getCode(), "未找到对应翻译");
        } catch (Exception e) {
            log.error("获取翻译失败", e);
            return ResponseResult.fail(BizCodeEnume.QUERY_ERROR);
        }
    }

    /**
     * 批量导入翻译数据
     *
     * @param translations 翻译数据列表
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult batchImport(List<BaseLanguageTranslationEntity> translations) {
        try {
            if (translations == null || translations.isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "翻译数据不能为空");
            }

            int insertCount = this.baseMapper.batchInsert(translations);
            return ResponseResult.success("成功导入" + insertCount + "条翻译数据");
        } catch (Exception e) {
            log.error("批量导入翻译数据失败", e);
            return ResponseResult.fail(BizCodeEnume.ADD_ERROR);
        }
    }

    /**
     * 删除翻译记录
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteTranslation(Integer id) {
        try {
            if (id == null) {
                return ResponseResult.fail(BizCodeEnume.MISSING_PARAMETER.getCode(), "ID不能为空");
            }

            int deleteResult = this.baseMapper.deleteById(id);
            if (deleteResult > 0) {
                return ResponseResult.success("删除成功");
            }
            return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
        } catch (Exception e) {
            log.error("删除翻译记录失败", e);
            return ResponseResult.fail(BizCodeEnume.DELETE_ERROR);
        }
    }
}
