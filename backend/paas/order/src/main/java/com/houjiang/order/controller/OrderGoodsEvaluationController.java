package com.houjiang.order.controller;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.order.dto.GoodsParaDTO;
import com.houjiang.order.dto.RiderEvaluationDTO;
import com.houjiang.order.dto.ShopParaDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderGoodsEvaluationController
 * @Description 商品评论表
 * @Author Amos
 * @Date 2023-05-26 13:28:35
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/goodsEvaluation")
public class OrderGoodsEvaluationController extends BaseController {

    /**
     * 查询评论条数
     */
    @PostMapping("/queryCommentsCount")
    public ResponseResult queryCommentsCount(@RequestBody Map<String,Integer> param){
        ResponseResult result = orderGoodsEvaluationService.queryCommentsCount(param.get("commonId"));
        return result;
    }
    /**
     * 查询商品评论
     *
     * @param map
     * @return
     */
    @PostMapping("/getComments")
    public ResponseResult getComments(@RequestBody Map<String, Object> map){
        ResponseResult comments = orderGoodsEvaluationService.getComments(map);
        return comments;
    }

    /**
     * 查询用户所有评论
     *
     * @param map
     * @return
     */
    @PostMapping("/queryMyEvaluation")
    public ResponseResult queryMyEvaluation(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderGoodsEvaluationService.queryMyEvaluation(map);
        return result;
    }

    /**
     * 平台查询 买家评论 查询
     *
     */
    @PostMapping("/platForm/buyerComments")
    public ResponseResult platFormBuyerComments(@RequestBody GoodsParaDTO goodsPara) {
        ResponseResult result = orderGoodsEvaluationService.platFormBuyerComments(goodsPara);
        return result;
    }

    /**
     * 店铺评价
     *
     * @param
     * @return
     */
    @PostMapping("/platForm/shopComments")
    public ResponseResult platFormShopComments(@RequestBody ShopParaDTO shopPara) {
        ResponseResult result = orderGoodsEvaluationService.platFormShopComments(shopPara);
        return result;
    }

    /**
     * 删除卖家评论
     * @param
     * @return
     */
    @PostMapping("/platForm/deleteBuyerComments")
    public ResponseResult deleteBuyerComments(@RequestBody Map<String,String> map) {
        ResponseResult result = orderGoodsEvaluationService.deleteBuyerComments(map);
        return result;
    }

    /**
     * 删除店铺评价
     *
     * @param
     * @return
     */
    @PostMapping("/platForm/deleteShopComments")
    public ResponseResult deleteShopComments(@RequestBody Map<String,String> map) {
        ResponseResult result = orderGoodsEvaluationService.deleteShopComments(map);
        return result;
    }

    /**
     * 商家后台查看商品评论
     *
     * @param map
     * @return
     */
    @PostMapping("/getAllComments")
    public ResponseResult getAllComments(@RequestBody Map<String, Object> map) {
        ResponseResult allComments = orderGoodsEvaluationService.getAllComments(map);
        return allComments;
    }

    /**
     * 商家回复买家评论
     *
     * @param map
     * @return
     */
    @PostMapping("/replyToComment")
    public ResponseResult replyToComment(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderGoodsEvaluationService.replyToComment(map);
        return result;
    }

    /**
     * 商家回复买家追加评论
     *
     * @param map
     * @return
     */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, Object> map) {
        ResponseResult add = orderGoodsEvaluationService.add(map);
        return add;
    }

    /**
     * 定时任务-确认收货超过14天默认默认好评
     *
     * @param acquiescenceVO
     * @return
     */
    @PostMapping("/job/insertEvaluated")
    public ResponseResult insertEvaluated(@RequestBody AcquiescenceVO acquiescenceVO) {
        ResponseResult result = orderGoodsEvaluationService.insertEvaluated(acquiescenceVO);
        return result;
    }

    /**
     * 骑手评价
     */
    @PostMapping("/riberEvaluated")
    public ResponseResult riberEvaluated(@RequestBody RiderEvaluationDTO riderEvaluationDTO) {
        ResponseResult result = orderGoodsEvaluationService.riberEvaluated(riderEvaluationDTO);
        return result;
    }
}
