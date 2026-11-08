package com.houjiang.goods.mall.service;

import com.houjiang.goods.mall.dto.MallGoodsCommentSearchDTO;
import com.houjiang.goods.mall.vo.MallGoodsCommentVO;
import com.houjiang.goods.mall.vo.PageVO;

/**
 * Mall 商品评论 Service
 * @author Amos
 */
public interface MallGoodsCommentService {

    /**
     * 评论列表
     */
    PageVO<MallGoodsCommentVO> list(MallGoodsCommentSearchDTO search);

    /**
     * 显示/隐藏
     */
    void changeShow(Integer id);

    /**
     * 商家回复
     */
    void reply(Integer id, String content);

    /**
     * 删除
     */
    void del(Integer id);
}
