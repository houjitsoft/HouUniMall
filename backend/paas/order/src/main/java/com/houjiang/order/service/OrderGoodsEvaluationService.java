package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.order.dto.GoodsParaDTO;
import com.houjiang.order.dto.RiderEvaluationDTO;
import com.houjiang.order.dto.ShopParaDTO;
import com.houjiang.order.entity.OrderGoodsEvaluationEntity;

import java.util.Map;

/**
 * @ClassName OrderGoodsEvaluationService
 * @Description  商品评论表
 * @Author Amos
 * @Date 2023-05-26 13:28:36
 * @Version 1.0
 * @author Amos
 */
public interface OrderGoodsEvaluationService extends IService<OrderGoodsEvaluationEntity> {
     /**
      * 查询评论条数
      * @param commonId
      * @return
      */
     ResponseResult queryCommentsCount(Integer commonId);

    /**
     * 查询商品评论
     * @param map
     * @return
     */
     ResponseResult getComments(Map<String, Object> map);

    /**
     * 查询用户所有评论
     *
     * @param map
     * @return
     */
    ResponseResult queryMyEvaluation(Map<String, Object> map);

    /**
     * 买家评论
     */
    ResponseResult platFormBuyerComments(GoodsParaDTO goodsPara);

    /**
     * 店铺评价
     */
    ResponseResult platFormShopComments(ShopParaDTO goodsPara);

    /**
     *删除买家评论
     */
    ResponseResult deleteBuyerComments(Map<String,String> map);

    /**
     * 删除店铺评价
     *
     * @param
     * @return
     */
    ResponseResult deleteShopComments(Map<String,String> map);

    /**
     * 商家后台查看商品评论
     *
     * @param map
     * @return
     */
    ResponseResult getAllComments(Map<String, Object> map);

    /**
     * 商家回复买家评论
     *
     * @param map
     * @return
     */
    ResponseResult replyToComment(Map<String, Object> map);

    /**
     * 商家回复买家追加评论
     *
     * @param map
     * @return
     */
    ResponseResult add(Map<String, Object> map);

    /**
     * 定时任务-确认收货超过14天默认默认好评
     *
     * @param acquiescenceVO
     * @return
     */
    ResponseResult insertEvaluated(AcquiescenceVO acquiescenceVO);

    /**
     * @param riderEvaluationDTO
     * @return
     */
    ResponseResult riberEvaluated(RiderEvaluationDTO riderEvaluationDTO);
}
