package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.dto.CommunityArticleAddDTO;
import com.houjiang.goods.entity.GoodsArticleResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsArticleResourceMapper
 * @Description  社区心得资源表
 * @Author Amos
 * @Date 2023-07-18 10:38:59
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface GoodsArticleResourceMapper extends BaseMapper<GoodsArticleResourceEntity> {

    /**
     * 删除
     * @param param
     */
    void deleteArticleResources(@Param("param")Map<Object, Object> param);

    /**
     * 查询心得 图片
     * @param articleId
     * @return
     */
    List<String> getImageReources(@Param("param")String articleId);

    /**
     * 查询心得视频
     * @param articleId
     * @return
     */
    String getVideoReources(String articleId);

    /**
     * 添加
     * @param param
     */
    void addArticleResources(@Param("param")CommunityArticleAddDTO param);
}
