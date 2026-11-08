package com.houjiang.goods.feign;


import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.commons.dto.goods.ManjianzengzheGoodsRuleQueryDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.*;
import org.springframework.cloud.openfeign.FeignClient;
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
     * 查询秒杀活动信息 feign
     */
    @PostMapping("activity/seckillGoods/selectSeckillByGoodsId")
     AppSeckillGoodsQueryResultVO selectSeckillByGoodsId(@RequestBody Map<String, Object> map);
    /**
     * 查询限时折扣活动信息 feign
     */
    @PostMapping("activity/discountGoods/selectByGoodsId")
     AppDiscountGoodsQueryResultVO selectDiscountByGoodsId(@RequestParam("goodsId") Integer goodsId);
    /**
     * 查询秒杀活动信息 feign
     */
    @PostMapping("activity/seckillGoods/selectByGoodsId")
    AppSeckillGoodsVO selectByGoodsId(@RequestBody Map<String,Object> map);

    /**
     * 查询拼团活动信息 feign
     */
    @PostMapping("activity/piecingTogether/selectByGoodsId")
     AppPintuanGoodsQueryResultVO selectpiecingTogetherByGoodsId(@RequestParam("commonId") Integer commonId, @RequestParam("goodsId") Integer goodsId);

    /**
     * 查询满减增折活动信息 feign
     */
    @PostMapping("activity/reductionPolicyBase/selectByGoodsId")
     ManjianzengzheGoodsQueryResultVO selectReductionPolicyByGoodsId(@RequestParam("commonId") Integer commonId);
    /**
     * 查询满减增折活动规则
     * @param promotionId
     * @return
     */
    @PostMapping("activity/reductionPolicyBase/selectList")
     List<ManjianzengzheGoodsRuleQueryDTO> selectReductionPolicyRuleDetailList(@RequestParam("promotionId")Integer promotionId);

    /**
     * 查询满减增折 活动内容
     *
     */
    @PostMapping("activity/reductionPolicyBase/selectContent")
     List<ManjianzengzhePromotionContentQueryResultVO> selectReductionPolicyPromotionContent(@RequestParam("commonId") Integer commonId);

    /**
     * 查询秒杀库存
     * @param paramMap
     * @return
     */
    @PostMapping(value = "activity/seckillGoods/querySeckillNum")
    Integer querySeckillNum(@RequestBody Map<String, Object> paramMap);

    /**
     * 更新秒杀商品库存
     * @param paramMap
     */
    @PostMapping(value = "activity/seckillGoods/updateSaleForSeckill")
    ResponseResult updateSaleForSeckill(@RequestBody Map<String, Object> paramMap);

    /**
     * 查询秒杀活动信息 feign
     */
    @PostMapping("/activity/seckillGoods/selectSeckillByGoodsId2")
    AppSeckillGoodsQueryResultVO selectSeckillByGoodsId2(@RequestParam("goodsId") Integer goodsId);

    /**
     * 根据当前时间查询限时折扣活动信息 feign
     */
    @PostMapping("/activity/discountGoods/selectDiscountByGoodsIdAndTime")
    AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(@RequestParam("goodsId") Integer goodsId);

    /**
     * 千客千价：查询用户个性化价格
     */
    @PostMapping("/activity/thousandsGuestsPrice/price/query")
    ResponseResult<Map<String, Object>> queryThousandsGuestsPersonalPrice(@RequestBody Map<String, Object> param);
}
