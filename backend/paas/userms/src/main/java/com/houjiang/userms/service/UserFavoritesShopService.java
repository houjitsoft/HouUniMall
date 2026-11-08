package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserFavoritesShopEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName UserFavoritesShopService
 * @Description 收藏的店铺
 * @Author Amos
 * @Date 2023-05-26 16:35:37
 * @Version 1.0
 * @author Amos
 */
public interface UserFavoritesShopService extends IService<UserFavoritesShopEntity> {
    /**
     * 查看店铺收藏与否
     * @param paraMap
     * @return
     */
    ResponseResult check(Map<String, String> paraMap);

    /**
     * 收藏店铺
     *
     * @param paraMap
     * @return
     */
    ResponseResult add(Map<String, String> paraMap);

    /**
     * 我收藏的店铺
     *
     * @param paraMap
     * @return
     */
    ResponseResult get(Map<String, Object> paraMap);

    /**
     * 删除收藏的店铺
     *
     * @param paraMap
     * @return
     */
    ResponseResult delete(Map<String, Object> paraMap);


    /**
     * 删除数据
     *
     * @param shopId
     * @return 删除结果
     */
    ResponseResult delFavoritesByShopId(Integer shopId);


    /**
     * 删除数据
     * @param shopId
     */
    void deleteUserFavoritesShop(String shopId);
}
