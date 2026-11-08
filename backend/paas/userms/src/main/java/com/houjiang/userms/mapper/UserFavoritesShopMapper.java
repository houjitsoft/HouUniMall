package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.entity.UserFavoritesShopEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

 /**
 * @ClassName UserFavoritesShopMapper
 * @Description  收藏的店铺
 * @Author Amos
 * @Date 2023-05-26 16:35:36
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserFavoritesShopMapper extends BaseMapper<UserFavoritesShopEntity> {

     /**
      * 查看店铺收藏与否
      * @param paraMap
      * @return
      */
     String checkFavoriteShopOrNot(Map<String, String> paraMap);

     /**
      * 取消店铺收藏
      * @param paraMap
      * @return
      */
     Integer deleteFavoriteShopOrNot(Map<String, String> paraMap);

     /**
      * 收藏店铺
      * @param paraMap
      * @return
      */
     Integer addFavoriteShop(Map<String, String> paraMap);


     /**
      * 删除用户收藏的店铺
      * @param shopId
      */
     void deleteUserFavoritesShop(@Param("shopId") String shopId);
 }
