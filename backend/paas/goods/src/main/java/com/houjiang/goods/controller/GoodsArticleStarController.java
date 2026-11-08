package com.houjiang.goods.controller;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsArticleStarController
 * @Description  社区心得收藏表
 * @Author Amos
 * @Date 2023-07-18 15:03:07
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("goods/articleStar")
public class GoodsArticleStarController extends BaseController {

    /**
     * 收藏心得
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/get")
    public ResponseResult starArticle(@RequestBody Map<Object, Object> param) {
        ResponseResult result = goodsArticleStarService.starArticle(param);
        return result;
    }
    /**
     * 收藏心得 列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/list")
    public ResponseResult queryStarList(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsArticleStarService.queryStarList(param);
        return result;
    }
}
