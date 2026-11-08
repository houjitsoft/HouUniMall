package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/goods/tagClassInfo")
public class GoodsTagClassInfoController extends BaseController{



    /**
     * 删除分类
     * @param
     * @return
     */
    @PostMapping("/deleteCommunityTopic")
    public ResponseResult deleteCommunityTopic(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagClassInfoService.deleteCommunityTopic(map);
        return result;
    }


    /**
     * 增加分类
     * @param
     * @return
     */
    @PostMapping("/insertCommunityTopic")
    @ExceptionHandler({RuntimeException.class})
    public  ResponseResult insertCommunityTopic(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagClassInfoService.insertCommunityTopic(map);
        return result;
    }


    /**
     * 查询分类
     * @param
     * @return
     */
    @PostMapping("/selectCommunityTopic")
    public ResponseResult selectCommunityTopic(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagClassInfoService.selectCommunityTopic(map);
        return result;
    }

    /**
     * 发表社区心得-查询话题分类
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/list")
    public ResponseResult queryTagClass(@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsTagClassInfoService.queryTagClass(param);
        return result;
    }
}
