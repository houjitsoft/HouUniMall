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
@RequestMapping("/goods/remark")
public class GoodsArticleRemarkController extends BaseController{


    /**
     * 更改社区评论状态
     * @param
     * @return
     */
    @PostMapping("/updateStatus")
    public ResponseResult updateStatus(@RequestBody Map<Object, Object> map) {
        ResponseResult result=goodsArticleRemarkService.updateStatus(map);
        return result;

    }


    /**
     * 查询社区评论详情列表
     * @param
     * @return
     */
    @PostMapping("/selectDetailsList")
    public ResponseResult selectDetailsList(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsArticleRemarkService.selectDetailsList(map);
        return result;
    }


    /**
     * 查询社区评论列表
     * @param
     * @return
     */
    @PostMapping("/selectList")
    public ResponseResult selectList(@RequestBody Map<String, Object> map) {
        ResponseResult result=goodsArticleRemarkService.selectList(map);
        return result;
    }

    /**
     * 获取评论列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/list")
    public ResponseResult queryRemarkList(@RequestBody Map<String, Object> param) {
        ResponseResult result = goodsArticleRemarkService.queryRemarkList(param);
        return result;
    }
    /**
     * 发表评论
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/add")
    public ResponseResult addRemark(@RequestBody Map<Object,Object>param) {
        ResponseResult result = goodsArticleRemarkService.addRemark(param);
        return result;
    }

}
