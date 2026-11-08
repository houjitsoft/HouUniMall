package com.houjiang.goods.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsArticleLikeController
 * @Description  社区心得点赞表
 * @Author Amos
 * @Date 2023-07-18 14:51:33
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods/articleLike")
public class GoodsArticleLikeController extends BaseController {

    /**
     * 点赞心得
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/article/like")
    public ResponseResult likeArticle(@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsArticleLikeService.likeArticle(param);
        return result;
    }

    /**
     * 获取点赞列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/list")
    public ResponseResult queryLikeList(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsArticleLikeService.queryLikeList(param);
        return result;
    }

    /**
     * 获取点赞跟评论消息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/queryLikeAndRemarkCount")
    public ResponseResult queryLikeAndRemarkCount(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsArticleLikeService.queryLikeAndRemarkCount(param);
        return result;
    }
}
