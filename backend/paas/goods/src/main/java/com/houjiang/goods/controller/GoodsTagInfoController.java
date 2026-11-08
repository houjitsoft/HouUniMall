package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
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
@RequestMapping("/goods/tagInfo")
public class GoodsTagInfoController extends BaseController{

    /**
     * 编辑话题
     * @param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateCommunity(@RequestBody  Map<String, Object> map) {
        ResponseResult result=goodsTagInfoService.updateCommunity(map);
        return result;
    }

    /**
     * 增加话题
     * @param
     * @return
     */

    @PostMapping( "/insert")
    public  ResponseResult insertCommunity(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagInfoService.insertCommunity(map);
        return result;
    }


    /**
     * 删除话题
     * @param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteCommunity(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagInfoService.deleteCommunity(map);
        return result;
    }


    /**
     * 查询话题
     * @param
     * @return
     */
    @PostMapping("/select")
    public ResponseResult selectCommunity(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsTagInfoService.selectCommunity(map);
        return result;
    }
    /**
     * 查询话题 总帖数、阅读数
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/queryCount")
    public ResponseResult queryCount(@RequestBody Map<Object, Object> param) {
        ResponseResult result = goodsTagInfoService.queryCount(param);
        return result;
    }
    /**
     * 发表社区心得-查询话题
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/list")
    public ResponseResult queryTags(@RequestBody Map<Object,Object> param) {
        ResponseResult result = goodsTagInfoService.queryTags(param);
        return result;
    }


    /**
     * 查询话题
     * @param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryCommunity(@RequestBody Map<String, String> map) {
        ResponseResult result=goodsTagInfoService.queryCommunity(map);
        return result;
    }


}
