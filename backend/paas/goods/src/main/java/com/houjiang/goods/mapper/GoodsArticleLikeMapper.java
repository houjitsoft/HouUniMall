package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.entity.GoodsArticleLikeEntity;
import com.houjiang.goods.vo.CommunityLikeEntityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName GoodsArticleLikeMapper
 * @Description  社区心得点赞表
 * @Author Amos
 * @Date 2023-07-18 10:43:58
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsArticleLikeMapper extends BaseMapper<GoodsArticleLikeEntity> {

    /**
     * 删除点赞
     * @param param
     */
    void deleteLikes(Map<Object, Object> param);

    /**
     * 查询用户点赞id
     * @param param
     * @return
     */
    Integer getArticleIsLiked(@Param("param")Map<Object, Object> param);

    /**
     * 查询被点赞用户的点赞id
     * @param param
     * @return
     */
    Integer getRemarkisLiked(@Param("param")Map<Object, Object> param);

    /**
     * 点赞
     * @param param
     */
    void insertLike(@Param("param")Map<Object, Object> param);

    /**
     * 取消点赞
     * @param param
     */
    void deleteLike(@Param("param")Map<Object, Object> param);

    /**
     * 查询点赞数
     * @param param
     * @return
     */
    Integer getLikedNum(@Param("param")Map<Object, Object> param);

    /**
     * 获取点赞列表
     * @param param
     * @return
     */
    IPage<CommunityLikeEntityVO> getLikeList(IPage<CommunityLikeEntityVO> page, @Param("param")Map<String, Object> param);

}
