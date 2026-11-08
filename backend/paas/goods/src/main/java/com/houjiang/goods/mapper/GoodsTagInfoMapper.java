package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.entity.GoodsTagInfoEntity;
import com.houjiang.goods.vo.CommunityArticleTagVO;
import com.houjiang.goods.vo.CommunityTopicVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.goods.entity.GoodsTagInfo
 * @author Amos
 */
@Mapper
public interface GoodsTagInfoMapper extends BaseMapper<GoodsTagInfoEntity> {


    /**
     * 编辑话题
     * @param
     * @return
     */
    Integer updateCommunity(@Param("param") Map<String, Object> map);



    /**
     * 增加话题
     * @param
     * @return
     */
    void insertCommunity(@Param("param")Map<String, Object> map);


    /**
     * 查询话题
     * @param
     * @return
     */
    IPage<CommunityTopicVO> selectCommunity(IPage<CommunityTopicVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询话题 总帖数、阅读数
     * @param tagId
     * @return
     */
    Map<String, Object> queryCount(@Param("tagId")Integer tagId);

    /**
     * 发表社区心得-查询话题
     * @param param
     * @return
     */
    List<CommunityArticleTagVO> queryTag(@Param("param")Map<Object, Object> param);


    /**
     * 查询社区话题列表
     * @param map
     * @return
     */
    List<GoodsTagInfoEntity> queryCommunityClassificationsLimit( @Param("param") Map<Object, Object> map);

}




