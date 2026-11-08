package com.houjiang.goods.controller;

import com.houjiang.commons.dto.goods.CommunityArticleListDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.CommunityArticleAddDTO;
import com.houjiang.goods.dto.CommunityContentReviewDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsArticleInfoController
 * @Description
 * @Author Amos
 * @Date 2023/7/17 9:12
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/articleInfo")
public class GoodsArticleInfoController extends BaseController{


    /**
     * 更改社区审核状态
     * @param
     * @return
     */
    @PostMapping("/updateAuditStatus")
    public ResponseResult updateCommunityAuditStatus(@RequestBody CommunityContentReviewDTO param) {
        ResponseResult result=goodsArticleInfoService.updateCommunityAuditStatus(param);
        return result;

    }


    /**
     * 查询内容审核列表
     * @param
     * @return
     */
    @PostMapping("/selectContentList")
    public ResponseResult selectContentList(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsArticleInfoService.selectContentList(map);
        return result;
    }

    /**
     * 删除社区心得
     *
     * @param param Map
     * @return ResultResp
     */
    @PostMapping("/article/delete")
    public ResponseResult deleteArticle (@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsArticleInfoService.deleteArticle(param);
        return result;
    }

    /**
     * 查看心得详情
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/article/detail")
    public ResponseResult getArticleDetails(@RequestBody Map<Object,Object> param) {
        ResponseResult articleDetails = goodsArticleInfoService.getArticleDetails(param);
        return articleDetails;
    }
    /**
     * 查询用户信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/user/info/query")
    public ResponseResult queryUserInfo(@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsArticleInfoService.queryUserInfo(param);
        return result;
    }
    /**
     * 搜索用户心得
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/user/search/list")
    public ResponseResult searchUserList(@RequestBody Map<Object, Object> param) {
        ResponseResult result = goodsArticleInfoService.searchUserList(param);
        return result;
    }
    /**
     * 发表社区心得-查询商品
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping(value = "/goods/query/list")
    public ResponseResult queryGoods(@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsArticleInfoService.queryGoods(param);
        return result;
    }
    /**
     * 发表社区心得
     *
     * @param param CommunityArticleAddParam
     * @return ResultResp
     */
    @PostMapping(value = "/article/add")
    public ResponseResult addArticle(@RequestBody CommunityArticleAddDTO param) {
        ResponseResult result = goodsArticleInfoService.addArticle(param);
        return result;
    }

    /**
     * 查询社区心得列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult getArticleList(@RequestBody CommunityArticleListDTO param) {
        ResponseResult result=goodsArticleInfoService.getArticleList(param);
        return result;
    }

    /**
     * 查询社区心得列表
     *
     * @param param
     * @return
     */
    @PostMapping("/queryAll/list")
    public ResponseResult getAllArticleList(@RequestBody CommunityArticleListDTO param) {
        ResponseResult result=goodsArticleInfoService.getAllArticleList(param);
        return result;
    }

    /**
     * 查询分类
     *
     * @return
     */
    @PostMapping("/queryCommunityTopics")
    public ResponseResult queryCommunityTopics() {
        ResponseResult result=goodsArticleInfoService.queryCommunityTopics();
        return result;
    }


    /**
     * 随机推荐10个用户 附带帖子
     *
     * @param param
     * @return
     */
    @PostMapping("/queryRandomInvitation")
    public ResponseResult queryRandomInvitation(@RequestBody Map<Object, Object> param) {
        ResponseResult result=goodsArticleInfoService.queryRandomInvitation(param);
        return result;
    }


    /**
     * 查询社区关注用户心得列表
     *
     * @param param
     * @return
     */
    @PostMapping("/follow/article/query/list")
    public ResponseResult getFollowArticleList(@RequestBody CommunityArticleListDTO param) {
        ResponseResult result=goodsArticleInfoService.getFollowArticleList(param);
        return result;
    }
}
