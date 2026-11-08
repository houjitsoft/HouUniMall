package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsArticleStarEntity;

import java.util.Map;

/**
 * @ClassName GoodsArticleStarService
 * @Description  社区心得收藏表
 * @Author Amos
 * @Date 2023-07-18 15:03:07
 * @Version 1.0
 * @author Amos
 */
public interface GoodsArticleStarService extends IService<GoodsArticleStarEntity> {
    /**
     * 收藏心得
     * @param param
     * @return
     */
    ResponseResult starArticle(Map<Object, Object> param);

    /**
     * 收藏心得 列表
     * @param param
     * @return
     */
    ResponseResult queryStarList(Map<String, Object> param);
}
