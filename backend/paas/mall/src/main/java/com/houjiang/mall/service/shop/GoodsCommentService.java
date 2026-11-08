package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.GoodsComment;
import com.houjiang.mall.mapper.shop.GoodsCommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品评价 Service
 * 
 * @author Amos
 */
@Service
public class GoodsCommentService extends ServiceImpl<GoodsCommentMapper, GoodsComment> {
    
    /**
     * 分页查询评价列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<GoodsComment> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer goodsId = params.get("goodsId") != null ? Integer.parseInt(params.get("goodsId").toString()) : null;
        Integer star = params.get("star") != null ? Integer.parseInt(params.get("star").toString()) : null;
        
        LambdaQueryWrapper<GoodsComment> wrapper = new LambdaQueryWrapper<>();
        
        // 按商品 ID 筛选
        wrapper.eq(goodsId != null, GoodsComment::getGoodsId, goodsId);
        
        // 按星级筛选
        wrapper.eq(star != null, GoodsComment::getStar, star);
        
        // 默认查询显示的评价
        wrapper.eq(GoodsComment::getIsShow, 1);
        
        // 按创建时间降序排序
        wrapper.orderByDesc(GoodsComment::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 审核评价（通过/拒绝）
     * @param commentId 评价 ID
     * @param isShow 是否显示：0-不显示 1-显示
     * @return 是否成功
     */
    public boolean auditComment(Long commentId, Integer isShow) {
        GoodsComment comment = this.getById(commentId);
        if (comment == null) {
            return false;
        }
        
        comment.setIsShow(isShow);
        return this.updateById(comment);
    }
    
    /**
     * 商家回复评价
     * @param commentId 评价 ID
     * @param replyContent 回复内容
     * @return 是否成功
     */
    public boolean replyComment(Long commentId, String replyContent) {
        GoodsComment comment = this.getById(commentId);
        if (comment == null) {
            return false;
        }
        
        comment.setReplyContent(replyContent);
        comment.setReplyTime(LocalDateTime.now());
        return this.updateById(comment);
    }
}
