package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.goods.entity.GoodsArticleRemarkEntity;
import com.houjiang.goods.vo.CommunityArticleRemarkEntityVO;
import com.houjiang.goods.vo.CommunityCommentsDetailsVO;
import com.houjiang.goods.vo.CommunityCommentsVO;
import com.houjiang.goods.vo.CommunityRemarkVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.goods.entity.GoodsArticleRemark
 * @author Amos
 */
@Mapper
public interface GoodsArticleRemarkMapper extends BaseMapper<GoodsArticleRemarkEntity> {

    /**
     * 更改社区评论状态
     * @param
     * @return
     */
    Integer updateCommunityCommentStatus(@Param("param") Map<Object, Object> map);


    /**
     * 查询社区评论详情列表
     * @param
     * @return
     */
    List<CommunityCommentsDetailsVO> selectDetailsList(@Param("remarkId") Integer remarkId );


    /**
     * 查询社区评论列表
     * @param
     * @return
     */
    IPage<CommunityCommentsVO> selectCommunityComments(IPage<CommunityCommentsVO> page , @Param("param") Map<String, Object> param);

    /**
     * 删除
     * @param param
     */
    void deleteRemarks(@Param("param")Map<Object, Object> param);


    /**
     * 查询用户评论列表
     * @param param
     * @return
     */
    IPage<CommunityRemarkVO> getRemarkList(IPage<CommunityRemarkVO> page, @Param("param")Map<String, Object> param);

    /**
     * 查询心得用户信息
     * @param userId
     * @return
     */
    Map<String, String> getUserInfo(@Param("userId") String userId);

    /**
     * 查询评论
     * @param remarkedId
     * @return
     */
    String getRemarkContent(@Param("param")String remarkedId);

    /**
     * 获取一级评论信息
     * @param param
     * @return
     */
    List<CommunityArticleRemarkEntityVO> queryRemarks(@Param("param")Map<Object, Object> param);

    /**
     * 对评论点赞
     * @param param
     */
    void updateLikeNumForComments(@Param("param")Map<Object, Object> param);

    /**
     * 发表评论
     * @param param
     */
    void addRemark(@Param("param")Map<Object, Object> param);
}




