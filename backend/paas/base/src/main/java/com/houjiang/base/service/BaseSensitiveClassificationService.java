package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseSensitiveClassificationDTO;
import com.houjiang.base.entity.BaseSensitiveClassificationEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseSensitiveClassificationService
 * @Description  敏感词分类
 * @Author Amos
 * @Date 2024-01-30 16:48:04
 * @Version 1.0
 * @author Amos
 */
public interface BaseSensitiveClassificationService extends IService<BaseSensitiveClassificationEntity> {

    /**
     * 平台 查询敏感词分类
     * @param param
     * @return
     */
    ResponseResult querySensitiveClassification(BaseSensitiveClassificationDTO param);

    /**
     * 查询所有敏感词分类
     * @return
     */
    ResponseResult querySensitiveClassificationAll();

    /**
     * 平台添加或修改敏感词分类
     * @param param
     * @return
     */
    ResponseResult add(BaseSensitiveClassificationDTO param);

    /**
     * 平台 删除敏感词分类
     * @param param
     * @return
     */
    ResponseResult delete(BaseSensitiveClassificationDTO param);
}
