package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName GoodsDraftController
 * @Description 草稿箱
 * @Author Amos
 * @Date 2023/8/11 9:13
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/goods/draft")
public class GoodsDraftController extends BaseController {

    /**
     * 查询草稿箱列表
     * @param param
     * @return
     */
    @PostMapping("/queryDraft")
    public ResponseResult queryDraft(@RequestBody Map<String,Object> param){
        ResponseResult result = goodsDraftService.queryDraft(param);
        return result;
    }

    /**
     * 删除某个草稿
     * @param param
     * @return
     */
    @PostMapping("/deleteDraft")
    public ResponseResult deleteDraft(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsDraftService.deleteDraft(param);
        return result;
    }

    /**
     * 将商品添加到草稿箱
     * @param param
     * @return
     */
    @PostMapping("/addGoodsToDraft")
    public ResponseResult addGoodsToDraft(@RequestBody Map<String,Object> param) {
        ResponseResult result = goodsDraftService.addGoodsToDraft(param);
        return result;
    }
}
