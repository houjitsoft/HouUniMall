package com.houjiang.mall.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.shop.GoodsComment;
import com.houjiang.mall.service.shop.GoodsCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品评价 Controller
 * 路径：/mall/shop/comment/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/shop/comment")
@RequiredArgsConstructor
public class GoodsCommentController {

    private final GoodsCommentService goodsCommentService;

    /**
     * 获取评价列表（分页）
     * POST /mall/shop/comment/list
     * 
     * @param params 查询参数
     * @return 评价列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<GoodsComment>> getCommentList(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询评价列表，参数：{}", params);
            IPage<GoodsComment> page = goodsCommentService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("查询评价列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 审核评价
     * POST /mall/shop/comment/audit
     * 
     * @param params 查询参数（包含 commentId 和 isShow）
     * @return 是否成功
     */
    @PostMapping("/audit")
    public ResponseResult<Boolean> auditComment(@RequestBody Map<String, Object> params) {
        try {
            log.info("审核评价，参数：{}", params);
            
            if (!params.containsKey("commentId") || !params.containsKey("isShow")) {
                return ResponseResult.error("缺少必要参数");
            }
            
            Long commentId = Long.parseLong(params.get("commentId").toString());
            Integer isShow = Integer.parseInt(params.get("isShow").toString());
            
            boolean result = goodsCommentService.auditComment(commentId, isShow);
            
            if (!result) {
                return ResponseResult.error("审核失败，评价可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("审核评价失败", e);
            return ResponseResult.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 商家回复评价
     * POST /mall/shop/comment/reply
     * 
     * @param params 查询参数（包含 commentId 和 replyContent）
     * @return 是否成功
     */
    @PostMapping("/reply")
    public ResponseResult<Boolean> replyComment(@RequestBody Map<String, Object> params) {
        try {
            log.info("回复评价，参数：{}", params);
            
            if (!params.containsKey("commentId") || !params.containsKey("replyContent")) {
                return ResponseResult.error("缺少必要参数");
            }
            
            Long commentId = Long.parseLong(params.get("commentId").toString());
            String replyContent = params.get("replyContent").toString();
            
            boolean result = goodsCommentService.replyComment(commentId, replyContent);
            
            if (!result) {
                return ResponseResult.error("回复失败，评价可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("回复评价失败", e);
            return ResponseResult.error("回复失败：" + e.getMessage());
        }
    }
}
