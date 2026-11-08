package com.houjiang.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.BaseArticleBaseDTO;
import com.houjiang.base.entity.BaseArticleBaseEntity;
import com.houjiang.commons.util.ResponseResult;

/**
 * @ClassName BaseArticleBaseService
 * @Description  网站初始化内容设置
 * @Author Amos
 * @Date 2023-06-07 15:17:52
 * @Version 1.0
 * @author Amos
 */
public interface BaseArticleBaseService extends IService<BaseArticleBaseEntity> {
    /**
     * 根据文章id查询分组列表
     * @param articleGroupId
     * @return
     */
    ResponseResult queryArticleGroupDetail(Integer articleGroupId);

    /**
     * 查询文章信息
     *
     * @param param
     * @return
     */
    ResponseResult articleQueryDetail(BaseArticleBaseDTO param);

    /**
     * 删除文章
     *
     * @param param
     * @return
     */
    ResponseResult articleDelete(BaseArticleBaseDTO param);

    /**
     * 更新或添加文章
     *
     * @param param
     * @return
     */
    ResponseResult articleUpdate(BaseArticleBaseDTO param);


    /**
     * 查询所有的文章
     *
     * @param param
     * @return
     */
    ResponseResult queryArticleBase(BaseArticleBaseDTO param);

}
