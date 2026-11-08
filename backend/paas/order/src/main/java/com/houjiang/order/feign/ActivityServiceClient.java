package com.houjiang.order.feign;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("activity")
public interface ActivityServiceClient {

    /**
     * 查询红包情况
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/activity/redpacketBase/get/redDetail")
    public HouRedpacketBaseVO getRedpDetail(@RequestBody Map<String, String> paraMap);

    /**
     * 获取代金券信息(订单调用)
     *
     * @return
     */
    @PostMapping("activity/voucherBase/getvoucherdetail")
    VoucherBaseVO getVoucherDetail(@RequestBody Map<String, Object> map);

    /**
     * 查询秒杀活动限购信息
     *
     * @param param
     * @return
     */
    @PostMapping("activity/seckillGoods/check/limit")
    ResponseResult seckillCheckLimit(@RequestBody Map<String, Object> param);

    /**
     * 查询限时折扣 活动限购信息
     *
     * @param param
     * @return
     */
    @PostMapping("/activity/discountBase/check/limit")
    ResponseResult discountCheckLimit(@RequestBody Map<String, Object> param);

    /*
/**
     * 下单根据goodsid 查询秒杀活动详情 feign
     */
    @PostMapping("activity/seckillGoods/querySeckillInfoByGoodsId")
    CheckSeckillLimitResultVO querySeckillInfoByGoodsId(@RequestParam Integer goodsId);
    /**
     * 下单根据goodsid 查询限时折扣活动详情 feign
     */
    @PostMapping("activity/discountGoods/queryByGoodsId")
    CheckDiscountLimitResultVO queryDiscountInfoByGoodsId(@RequestParam Integer goodsId);

    /**
     * 查询拼团详情
     */
    @GetMapping("activity/piecingTogether/surplus/num/query")
    ResponseResult querySurplusNum(@RequestParam(required = false) String markId);

    /**
     * 更新 红包优惠券状态
     * @param paraMap
     */
    @PostMapping(value = "activity/voucherBase/salesprm/update")
    ResponseResult updateSalesPrm(@RequestBody Map<String, Object> paraMap);

    /**
     * 发起拼团
     * @param param
     * @return
     */
    @PostMapping(value = "activity/piecingTogether/join/pintuan/add")
    public ResponseResult addJoinPintuan(@RequestBody JoinPintuanParamVO param);

    /**
     * 拼团支付成功回调：回写已付款并按已付款人数判定成团
     * @param param markId + orderId
     */
    @PostMapping("/activity/piecingTogether/join/pintuan/paid")
    ResponseResult markPaidAndEvaluate(@RequestBody Map<String, Object> param);

    /**
     * 取消订单 删除当前用户的拼团信息
     */
    @PostMapping("/activity/piecingTogether/delete")
    ResponseResult deletePinTuanInfo(@RequestBody Map<String,Object> param);



    /**
     * 查询拼团信息 feign
     */
    @PostMapping("/activity/piecingTogetherMark/selectMarkInfo")
    ResponseResult selectPiecingTogetherMarkInfo(@RequestBody Map<String,Object> param);

    /**
     * 查询满减增折 活动内容
     *
     */
    @PostMapping("activity/reductionPolicyBase/selectContent")
    List<ManjianzengzhePromotionContentQueryResultVO> selectReductionPolicyPromotionContent(@RequestParam("commonId") Integer commonId);

    /**
     * 根据金额查询可领取代金券信息
     */
    @PostMapping("activity/voucherBase/selectavailablevcbase")
    ResponseResult selectAvailableVcBase(@RequestBody Map<String, Object> paraMap);

    /**
     * 根据组合id查询组合信息
     * @param combinationId 活动id
     * @return 结果
     */
    @GetMapping("/activityCombinationBase/queryCombinationInfoById")
    CombinationVO queryCombinationInfoById(@RequestParam("combinationId") String combinationId);

    /**
     * 根据组合id查询组合信息
     * @param combinationId 活动id
     * @param goodsId 商品id
     * @return 结果
     */
    @GetMapping("/activityCombinationBase/queryCombinationGoodsInfoByIds")
    CombinationGoodsInfoVO queryCombinationGoodsInfoByIds(@RequestParam("combinationId") Integer combinationId,
                                                          @RequestParam("goodsId") Integer goodsId);

    /**
     * 校验兑换券是否有效 feign
     * @param userId
     * @param commonId
     * @param voucherTId
     * @return
     */
    @PostMapping("/activity/voucher/template/queryProductVoucherBaseId")
    ResponseResult queryProductVoucherBaseId(@RequestParam("userId") String userId,
                                                    @RequestParam("commonId") Integer commonId,
                                                    @RequestParam("voucherTId")Integer voucherTId);

    /**
     * 更新 兑换券状态
     * @param paraMap
     */
    @PostMapping("/activity/voucherBase/updateProductVoucher")
    ResponseResult updateProductVoucher(@RequestBody Map<String, Object> paraMap);
}
