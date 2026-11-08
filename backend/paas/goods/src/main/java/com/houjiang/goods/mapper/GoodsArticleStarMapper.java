package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.entity.GoodsArticleStarEntity;
import com.houjiang.goods.vo.StarredArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName GoodsArticleStarMapper
 * @Description  社区心得收藏表
 * @Author Amos
 * @Date 2023-07-18 13:35:57
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsArticleStarMapper extends BaseMapper<GoodsArticleStarEntity> {

    /**
     * 查询是否收藏
     * @param tmpMap
     * @return
     */
    Integer getStarFlag(@Param("param")Map<Object, Object> tmpMap);

    /**
     * 判断当前用户是否已经收藏
     * @param param
     * @return
     */
    Integer queryExistForStar(@Param("param")Map<Object, Object> param);

    /**
     * 插入收藏记录表
     * @param param
     */
    void insertStar(@Param("param")Map<Object, Object> param);

    /**
     * 删除收藏记录表
     * @param param
     */
    void deleteStar(@Param("param")Map<Object, Object> param);

    /**
     * 查询收藏数
     * @param param
     * @return
     */
    Integer getStarNum(@Param("param")Map<Object, Object> param);

    /**
     * 收藏心得 列表
     * @param page
     * @param param
     * @return
     */
    IPage<StarredArticleVO> getStarredArticleList(IPage<String> page, @Param("param")Map<String, Object> param);
}
