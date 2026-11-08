package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商城后台 - 商品评论
/**
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/admin/goods/comment")
public class MallAdminGoodsCommentController {

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    /** 评论列表 */
    @PostMapping("/lists")
    public ResponseResult lists(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCommentList(param);
    }

    /** 显示切换 */
    @PostMapping("/set_is_show")
    public ResponseResult setIsShow(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCommentSetIsShow(param);
    }

    /** 回复 */
    @PostMapping("/reply")
    public ResponseResult reply(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCommentReply(param);
    }

    /** 删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCommentDel(param);
    }
}
