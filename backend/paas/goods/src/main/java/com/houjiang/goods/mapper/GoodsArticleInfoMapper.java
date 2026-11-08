package com.houjiang.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.goods.CommunityArticleListDTO;
import com.houjiang.commons.vo.goods.CommunityArticleVO;
import com.houjiang.goods.dto.CommunityArticleAddDTO;
import com.houjiang.goods.dto.CommunityContentReviewDTO;
import com.houjiang.goods.entity.GoodsArticleInfoEntity;
import com.houjiang.goods.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.goods.entity.GoodsArticleInfo
 * @author Amos
 */
@Mapper
public interface GoodsArticleInfoMapper extends BaseMapper<GoodsArticleInfoEntity> {

    /**
     * 更改社区审核状态
     * @param
     * @return
     */
    void updateCommunityAuditStatus(@Param("param") CommunityContentReviewDTO param);

    /**
     * 更新评论数量
     * @param map
     */
    void updateRemarkNum(@Param("param") Map<Object, Object> map);



    /**
     * 查询内容审核列表
     * @param
     * @return
     */
    IPage<CommunityContentReviewVO> selectCommunityContentReview2(IPage<CommunityContentReviewVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询社区资源链接
     * @param articleId
     * @return
     */
    List<String> getImageReources(@Param("param") String articleId);

    /**
     * 查询心得视频
     * @param articleId
     * @return
     */
    String getVideoReources(@Param("param") String articleId);

    /**
     * 删除心得
     * @param param
     */
    void deleteArticle(@Param("param")Map<Object, Object> param);

    /**
     * 查询心得信息
     * @param remarkedId
     * @return
     */
    String getArticleContent(@Param("remarkId")String remarkedId);

    /**
     * 更新阅读量
     * @param param
     */
    void updateReadNum(@Param("param")Map<Object, Object> param);

    /**
     * 查询信息
     * @param param
     * @return
     */
    CommunityArticleDetailVO queryArticleInfo(@Param("param")Map<Object, Object> param);

    /**
     * 获取作者基本信息
     * @param userId
     * @return
     */
    Map<String, String> getUserInfo(@Param("param")String userId);

    /**
     * 对心得点赞
     * @param param
     */
    void updateLikeNum(@Param("param")Map<Object, Object> param);

    /**
     * 更新帖子收藏数
     * @param param
     */
    void updateStarNum(@Param("param")Map<Object, Object> param);

    /**
     * 查询用户信息
     * @param param
     * @return
     */
    CommunityUserInfoVO getUserInfoEntity(@Param("param")Map<Object, Object> param);

    /**
     * 全部用户id列表
     * @param param
     * @return
     */
    List<String> getAllUserIds(@Param("param")Map<Object, Object> param);

    /**
     * 发表过心得且审核通过的用户id列表
     * @param param
     * @return
     */
    List<String> getHasArticleUserIds(@Param("param")Map<Object, Object> param);

    /**
     * 查询用户社区心得数
     * @param queryMap
     * @return
     */
    Integer getArticleNum(@Param("param")Map<Object, Object> queryMap);

    /**
     * 查询用户社区心得列表
     * @param uid
     * @return
     */
    List<CommunityArticleSmallEntityVO> getArticleSmallEntity(@Param("uid")String uid);

    /**
     * 查询订单id
     * @param param
     * @return
     */
    List<String> queryOrder(@Param("param")Map<Object, Object> param);

    /**
     * 发表社区心得-查询商品
     * @param orderId
     * @return
     */
    List<GoodsDetailVO> queryGoods(@Param("param")String orderId);

    /**
     * 新增社区心得
     * @param param
     */
    void addArticleInfo(@Param("param")CommunityArticleAddDTO param);

    /**
     * 查询心得信息
     * @param param
     * @return
     */
    StarredArticleVO queryStarArticleInfo(@Param("param")Map<String, Object> param);

    /**
     * 查询心得标题
     * @param articleId
     * @return
     */
    String getArticleTitle(@Param("param")String articleId);


    /**
     * 分页获取文章列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<CommunityArticleVO> getArticleList(Page<CommunityArticleVO> page, @Param("param") CommunityArticleListDTO param);


    /**
     * 查询商品分类
     * @return
     */
    List<CommunityArticleTagClassVO> queryGoodsCats();

    /**
     * 查询随机邀请
     *
     * @param userId
     * @return
     */
    List<CommunityArticleVO> queryRandomInvitation(@Param("userId") String userId);


    /**
     * 获取关注数量
     *
     * @param param
     * @return
     */
    Integer getFollowedNum(@Param("param") Map<Object, Object> param);


    /**
     * 获取关注用户id
     *
     * @param param
     * @return
     */
    List<String> getFollowUserIds(@Param("param") Map<Object, Object> param);


    /**
     * 分页获取以下文章列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<CommunityArticleVO> getFollowArticleList(Page<CommunityArticleVO> page, @Param("param") CommunityArticleListDTO param);
}




