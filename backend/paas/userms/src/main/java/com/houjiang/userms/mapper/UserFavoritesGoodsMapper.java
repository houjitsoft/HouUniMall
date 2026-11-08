package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.userms.dto.UserFavoriteGoodsDTO;
import com.houjiang.userms.entity.UserFavoritesGoodsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName UserFavoritesGoodsMapper
 * @Description  收藏的商品
 * @Author Amos
 * @Date 2023-05-26 16:24:17
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UserFavoritesGoodsMapper extends BaseMapper<UserFavoritesGoodsEntity> {

     /**
      * 查看商品收藏与否
      * @param paraMap
      * @return
      */
     String checkGoodsFavoriteOrNot(Map<String, String> paraMap);

     /**
      * 收藏商品
      * @param paraMap
      * @return
      */
     Integer addFavoriteGoods(Map<String, String> paraMap);

    /**
     * 分页获取商品
     *
     * @param page
     * @param paraMap
     * @return
     */
    IPage<UserFavoriteGoodsDTO> getFavoriteGoods2(IPage<UserFavoriteGoodsDTO> page, @Param("paraMap") Map<String, Object> paraMap);


    /**
     * 查询商品
     *
     * @param userId
     * @return
     */
    List<UserFavoriteGoodsDTO> getFavoriteGoods(@Param("userId") String userId);


    /**
     * 查询收藏的店铺
     *
     * @param userId
     * @return
     */
    List<Map<String, Object>> getFavoriteShops(@Param("userId") String userId);


    /**
     * 查询用户数量
     *
     * @param paraMap
     * @return
     */
    Integer selectUserFootCount(@Param("param") Map<String, String> paraMap);
 }
