package com.houjiang.goods.mall.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.mall.dto.MallGoodsCommentSearchDTO;
import com.houjiang.goods.mall.service.MallGoodsCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商品评论 Controller
 * @author Amos
 */
@RestController
@RequestMapping("/mall/goods/comment")
public class MallGoodsCommentController {

    @Autowired
    private MallGoodsCommentService service;

    /** 评论列表 */
    @PostMapping("/lists")
    public ResponseResult lists(@RequestBody MallGoodsCommentSearchDTO dto) {
        return ResponseResult.success(service.list(dto));
    }

    /** 切换显示 */
    @PostMapping("/set_is_show")
    public ResponseResult setIsShow(@RequestBody Map<String, Object> param) {
        service.changeShow(toInt(param.get("id")));
        return ResponseResult.success();
    }

    /** 商家回复 */
    @PostMapping("/reply")
    public ResponseResult reply(@RequestBody Map<String, Object> param) {
        Object id = param.get("id");
        Object content = param.get("content");
        service.reply(toInt(id), content == null ? "" : content.toString());
        return ResponseResult.success();
    }

    /** 删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        service.del(toInt(param.get("id")));
        return ResponseResult.success();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        return v instanceof Number ? ((Number) v).intValue() : Integer.valueOf(v.toString());
    }
}
