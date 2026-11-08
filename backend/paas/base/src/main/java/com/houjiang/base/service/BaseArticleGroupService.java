package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseArticleGroupDTO;
import com.houjiang.base.entity.BaseArticleGroupEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseArticleGroupService
 * @Description  网站初始化内容分组表
 * @Author Amos
 * @Date 2023-06-07 14:50:01
 * @Version 1.0
 * @author Amos
 */
public interface BaseArticleGroupService extends IService<BaseArticleGroupEntity> {
    /**
     * 查询文章分组
     * @return
     */
     ResponseResult queryArticleGroup();

    /**
     * 新增或修改 文章分组
     *
     * @param baseArticleGroupReq
     * @return
     */
    ResponseResult updateArticleGroup(BaseArticleGroupDTO baseArticleGroupReq);

    /**
     * 删除 文章分组
     *
     * @param baseArticleGroupReq
     * @return
     */
    ResponseResult deleteArticleGroup(BaseArticleGroupDTO baseArticleGroupReq);

 }
