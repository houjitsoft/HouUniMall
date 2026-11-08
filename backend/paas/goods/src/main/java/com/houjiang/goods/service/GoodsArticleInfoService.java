package com.houjiang.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.goods.CommunityArticleListDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.CommunityArticleAddDTO;
import com.houjiang.goods.dto.CommunityContentReviewDTO;
import com.houjiang.goods.entity.GoodsArticleInfoEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface GoodsArticleInfoService extends IService<GoodsArticleInfoEntity> {

    /**
     * 更改社区审核状态
     * @param
     * @return
     */
     ResponseResult updateCommunityAuditStatus(CommunityContentReviewDTO param);


    /**
     * 查询内容审核列表
     * @param
     * @return
     */
    ResponseResult selectContentList(Map<String, Object> map);

    /**
     * 删除社区心得
     * @param param
     * @return
     */
    ResponseResult deleteArticle(Map<Object, Object> param);

    /**
     * 查看心得详情
     * @param param
     * @return
     */
    ResponseResult getArticleDetails(Map<Object, Object> param);

    /**
     * 查询用户信息
     * @param param
     * @return
     */
    ResponseResult queryUserInfo(Map<Object, Object> param);

    /**
     * 搜索用户心得
     * @param param
     * @return
     */
    ResponseResult searchUserList(Map<Object, Object> param);

    /**
     * 发表社区心得-查询商品
     * @param param
     * @return
     */
    ResponseResult queryGoods(Map<Object, Object> param);

    /**
     * 发表社区心得
     * @param param
     * @return
     */
    ResponseResult addArticle(CommunityArticleAddDTO param);


    /**
     * 查询社区心得列表
     *
     * @param param
     * @return
     */
    ResponseResult getArticleList(CommunityArticleListDTO param);
    /**
     * 查询社区心得列表
     *
     * @param param
     * @return
     */
    ResponseResult getAllArticleList(CommunityArticleListDTO param);


    /**
     * 查询分类
     *
     * @return
     */
    ResponseResult queryCommunityTopics();


    /**
     * 随机推荐10个用户 附带帖子
     *
     * @param param
     * @return
     */
    ResponseResult queryRandomInvitation(Map<Object, Object> param);


    /**
     * 查询社区关注用户心得列表
     *
     * @param param
     * @return
     */
    ResponseResult getFollowArticleList(CommunityArticleListDTO param);
}
