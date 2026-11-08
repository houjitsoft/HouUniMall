package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserFavoritesGoodsEntity;

import java.util.Map;

/**
 * @ClassName UserFavoritesGoodsService
 * @Description  收藏的商品
 * @Author Amos
 * @Date 2023-05-26 16:24:17
 * @Version 1.0
 * @author Amos
 */
public interface UserFavoritesGoodsService extends IService<UserFavoritesGoodsEntity> {
    /**
     * 查看商品收藏与否
     * @param paraMap
     * @return
     */
     ResponseResult check(Map<String, String> paraMap);

    /**
     * 收藏商品
     * @param paraMap
     * @return
     */
    ResponseResult add(Map<String, String> paraMap);

    /**
     * 我的收藏商品列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult get(Map<String, Object> paraMap);

    /**
     * 删除我的收藏商品
     *
     * @param paraMap
     * @return
     */
    ResponseResult delete(Map<String, Object> paraMap);


    /**
     * 查询收藏商品
     *
     * @param paraMap
     * @return
     */
    ResponseResult query(Map<String, String> paraMap);




}
