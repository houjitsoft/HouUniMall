package com.houjiang.payment.feign;

import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.third.ConsumePointsReqVO;
import com.houjiang.commons.vo.third.ThirdUserShopVO;
import com.houjiang.commons.vo.third.UserAndShopInfoReqVo;
import com.houjiang.commons.vo.third.UserPointsInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.user.UserVkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("userms")
public interface UsermsServiceClient {

    /**
     * feign 根据用户id 查询当前用户有多少积分
     *
     * @param userId
     * @return
     */
    @PostMapping("/user/ms/getMemberIntegralByUserId")
    Integer getMemberIntegralByUserId(@RequestParam("userId") String userId);

    /**
     * feign 根据用户id 查询当前用户冻结积分
     * @param userId
     * @return
     */
    @PostMapping("/user/ms/getMemberFrozenIntegraIntegralByUserId")
    Integer getMemberFrozenIntegraIntegralByUserId(@RequestParam("userId") String userId);

    /**
     * 开通plus会员
     * @param map
     * @return
     */
    @PostMapping(value = "user/plusInfo/plusMember/openMember")
    public ResponseResult plusmemberopen(@RequestBody Map<String,Object> map);

    /**
     * 修改用户积分
     *
     * @param param
     * @return
     */
    @PostMapping("/user/ms/updateMemberIntegral")
    ResponseResult updateMemberIntegral(@RequestBody MemberMessageListIntegralDTO param);

    /**
     * 修改用户积分
     *
     * @param param
     * @return
     */
    @PostMapping("/user/ms/fegin/addPointsLog")
    ResponseResult feginAddpointsLog(@RequestBody UserPointLogDTO param);

    /**
     * 修改用户积分
     *
     * @param map
     * @return
     */
    @PostMapping("/user/pointsLog/insertByMap")
    ResponseResult insertPointLogByMap(@RequestBody Map<String, Object> map);

    /**
     * 支付宝 微信 支付plus会员时创建 预支付订单  fegin
     */
    @PostMapping("/user/plusInfoOrder/aLiWxCreatePlusOrder")
    ResponseResult aLiWxCreatePlusOrder(@RequestBody Map<String, Object> map);

    /**
     * @return 处理结果
     */
    @PostMapping("/userVk/setVKInfo")
    ResponseResult setVKInfo(@RequestParam("orderId") String orderId);
    /**
     * 根据unionId插入openId
     *
     * @param openId
     * @param unionId
     */
    @PostMapping("/user/bindConnect/insertOpenIdByUnionId")
    void insertOpenIdByUnionId(@RequestParam("openId") String openId, @RequestParam("unionId") String unionId);

    /**
     * 查询用户信息
     *
     * @return
     */
    @GetMapping("/user/ms/queryUserInfoByUserId")
    ResponseResult queryUserInfo(@RequestParam("userId") String userId);


    /**
     * 查询plus 会员订单是否支付成功
     * @param userId
     * @param plusOrder
     * @return
     */
    @GetMapping("/user/plusInfoOrder/feign/queryplusorder")
    Integer queryplusorder(@RequestParam("userId")String userId, @RequestParam("plusOrder")String plusOrder);

    /**
     * 根据用户ID查询用户的Plus会员状态。
     *
     * @param userId 用户的唯一标识，用于查询用户的Plus会员状态。
     * @return 返回用户的Plus会员状态。具体状态码的含义需要参考相关文档或枚举定义。
     */
    @GetMapping("/user/plusInfoOrder/feign/queryStatusByPlus")
    Integer queryStatusByPlus(@RequestParam("userId") String userId);


    /**
     * 查询用户Id
     *
     * @param shopId
     * @return
     */
    @GetMapping("/user/sellerBase/feign/getUserIDByShopId")
    String getUserIDByShopId(@RequestParam("shopId") String shopId);


    /**
     * 查询账号信息
     * @param userId
     * @return
     */
    @GetMapping("/userVk/feign/queryUserAccount")
     UserVkVO queryUserAccount(@RequestParam("userId") String userId);


    /**
     * 更新用户分销佣金账户表
     * @param paraMap
     * @return
     */
    @PostMapping("/userVk/feign/subUserAccount")
    int subUserAccount(@RequestBody  Map<String, String> paraMap);


    /**
     * 获取用户信息
     *
     * @param userMobile
     * @return
     */
    @GetMapping("/user/ms/feign/getUserInfoByMobile")
    UserInfoVO getUserInfoByMobile(@RequestParam("userMobile") String userMobile);

    /**
     * @param req 请求
     * @return 处理结果
     */
    @PostMapping("/from518/consumePoints")
    ResponseResult consumePoints(@Valid @RequestBody ConsumePointsReqVO req);

    /**
     * @param req 请求
     * @return 处理结果
     */
    @PostMapping("/from518/getUserAndShopInfo")
    ResponseResult<ThirdUserShopVO> getUserAndShopInfo(@RequestBody UserAndShopInfoReqVo req);

    /**
     * @param req 请求
     * @return 处理结果
     */
    @PostMapping("/from518/getUserAndShopDetail")
    ResponseResult<ThirdUserShopVO> getUserAndShopDetail(@Valid @RequestBody UserAndShopInfoReqVo req);

    /**
     * @param req 请求
     * @return 处理结果
     */
    @PostMapping("/from518/getUserPointsInfo")
    ResponseResult<UserPointsInfoVO> getUserPointsInfo(@Valid @RequestBody UserAndShopInfoReqVo req);

}
