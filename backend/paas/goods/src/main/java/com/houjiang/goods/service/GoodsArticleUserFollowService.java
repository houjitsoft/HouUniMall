package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.GoodsArticleUserFollowEntity;

import java.util.Map;

/**
 * @ClassName GoodsArticleUserFollowService
 * @Description  社区用户关注表
 * @Author Amos
 * @Date 2023-07-18 16:05:00
 * @Version 1.0
 * @author Amos
 */
public interface GoodsArticleUserFollowService extends IService<GoodsArticleUserFollowEntity> {
    /**
     * 关注用户列表
     * @param param
     * @return
     */
    ResponseResult queryfollowList(Map<String, Object> param);

    /**
     * 关注用户
     * @param param
     * @return
     */
    ResponseResult followUser(Map<Object, Object> param);

    /**
     * 批量关注用户
     * @param param
     * @return
     */
    ResponseResult batchfollowUser(Map<Object, Object> param);

    /**
     * 查询是否自己、是否关注
     * @param param
     * @return
     */
    ResponseResult checkUserInfo(Map<Object, Object> param);
}
