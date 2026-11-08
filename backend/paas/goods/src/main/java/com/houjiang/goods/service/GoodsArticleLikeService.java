package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsArticleLikeEntity;

import java.util.Map;

/**
 * @ClassName GoodsArticleLikeService
 * @Description  社区心得点赞表
 * @Author Amos
 * @Date 2023-07-18 14:51:33
 * @Version 1.0
 * @author Amos
 */
public interface GoodsArticleLikeService extends IService<GoodsArticleLikeEntity> {
    /**
     * 点赞心得
     * @param param
     * @return
     */
    ResponseResult likeArticle(Map<Object, Object> param);

    /**
     * 获取点赞列表
     * @param param
     * @return
     */
    ResponseResult queryLikeList(Map<String, Object> param);

    /**
     * 获取点赞跟评论消息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    ResponseResult queryLikeAndRemarkCount(Map<String, Object> param);
}
