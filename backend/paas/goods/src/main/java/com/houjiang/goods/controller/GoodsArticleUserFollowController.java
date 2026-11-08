package com.houjiang.goods.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsArticleUserFollowController
 * @Description  社区用户关注表
 * @Author Amos
 * @Date 2023-07-18 16:05:00
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods/articleUserFollow")
public class GoodsArticleUserFollowController extends BaseController {
    /**
     * 关注用户列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/list")
    public ResponseResult queryfollowList(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsArticleUserFollowService.queryfollowList(param);
        return result;
    }
    /**
     * 关注用户
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/follow")
    public ResponseResult followUser(@RequestBody Map<Object, Object> param) {
        ResponseResult result = goodsArticleUserFollowService.followUser(param);
        return result;
    }
    /**
     * 批量关注用户
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/batchfollow")
    public ResponseResult batchfollowUser(@RequestBody Map<Object, Object> param) {
        ResponseResult result = goodsArticleUserFollowService.batchfollowUser(param);
        return result;
    }

    /**
     * 查询是否自己、是否关注
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/check")
    public ResponseResult checkUserInfo(@RequestBody Map<Object,Object>param) {
        ResponseResult result = goodsArticleUserFollowService.checkUserInfo(param);
        return result;
    }

}
