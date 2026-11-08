package com.houjiang.auth.feign;

import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.third.ThirdUserShopVO;
import com.houjiang.commons.vo.third.UserAndShopInfoReqVo;
import com.houjiang.commons.vo.third.UserPointsInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import io.seata.core.exception.TransactionException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign。
 * 同机部署默认直连 8195，避免 Nacos 幽灵实例导致 Connect/Read timed out（密码/验证码登录间歇失败）。
 * @author Amos
 */
@FeignClient(name = "userms", url = "${feign.userms.url:http://127.0.0.1:8195}")
public interface UserServiceClient {

    @PostMapping("/user/ms/feign/oauth/get-or-create")
    ResponseResult getOrCreateOAuthMember(
            @org.springframework.web.bind.annotation.RequestHeader("X-Internal-OAuth-Token") String internalToken,
            @RequestBody Map<String, String> identity);


    /**
     * 根据手机号查询用户及其关联表信息
     *
     * @return
     */
    @PostMapping("/user/ms/infoByMobile")
    UserInfoVO getUserInfoByMobile(@RequestParam("userMobile") String userMobile);


    /**
     * 查询用户信息及其关联表
     *
     * @param vo
     * @return 结果
     */
    @PostMapping("/user/ms/querUserInfoByParams")
    public ResponseResult querUserInfoByParams(@RequestBody UserInfoVO vo);


    /**
     * 根据参数查用户信息
     *
     * @return
     */
    @PostMapping("/user/ms/queryUserInfo")
    ResponseResult queryUserInfo(@RequestBody UserInfoVO userInfoVO);


    /**
     * 根据根据用户名查后台系统用户信息
     *
     * @return
     */
    @PostMapping("/platform/userinfo/getPlatformUserInfo")
    ResponseResult getPlatformUserInfo(@RequestBody Map<String, String> map);

    /**
     * 根据手机号查询用户表
     *
     * @return
     */
    @PostMapping("/user/ms/userAccountByMap")
    ResponseResult getUserAccountByMap(@RequestBody Map<String, String> map);

    /**
     * 根据userId查询用户信息
     *
     * @return
     */
    @PostMapping("/user/ms/getUserInfo")
    ResponseResult getUserInfo(@RequestBody Map<String, String> map);

    /**
     * 根判断是否为未通过企业用户
     *
     * @return
     */
    @PostMapping("/user/businessInfo/infoByMap")
    ResponseResult getBusinessInfoByMap(@RequestBody Map<String, String> map);

    /**
     * 根判断是否为未通过企业用户
     *
     * @return
     */
    @PostMapping("/user/ms/updateUserInfoByMap")
    ResponseResult updateUserInfo(@RequestBody Map<String, String> map);

    /**
     * 查询用户今天是否加过积分
     */
    @PostMapping("/user/pointsLog/infoByMap")
    ResponseResult queryLoginExit(@RequestBody Map<String, String> map);



   /**
    * 修改用户积分
    *
    * @return
    */
   @PostMapping("/user/ms/updateMemberIntegral")
   ResponseResult updateMemberIntegral(@RequestBody MemberMessageListIntegralDTO param);

    /**
     * 增加用户积分
     *
     * @return
     */
    @PostMapping("/user/pointsLog/insertByMap")
    ResponseResult insertPointLogByMap(@RequestBody Map<String, Object> map);

    /**
     * 更新用户日活跃
     *
     * @return
     */
    @PostMapping("/user/liveStatistics/insertUserDayLive")
    ResponseResult insertUserDayLive(@RequestBody Map<String, Object> map);

    /**
     * 查询plus会员信息
     * @param map
     * @return
     */
    @PostMapping("/user/plusInfo/selectByUserId")
    ResponseResult selectByUserId(@RequestBody Map<String, String> map);


    /**
     * 查询会员权益
     *
     * @param id
     * @return
     */
    @PostMapping("/user/plusEquity/query")
    ResponseResult selectplusequitys(@RequestBody List<Integer> id);


    /**
     * 查询会员权益临时表
     *
     * @param id
     * @return
     */
    @PostMapping("/user/plusEquityTemp/query/selectPlusEquityTemps")
    ResponseResult selectplusequityTemps(@RequestBody List<Integer> id);

    /**
     * 判断用户是否授权登录过
     * @param bindOpenid
     * @return
     */
    @PostMapping("user/bindConnect/selectUserIdByOpenid")
    String selectUserIdByOpenid(@RequestParam("bindOpenid") String bindOpenid);

    /**
     * 根据授权id 查询用户信息
     * @param bindId
     * @return
     */
    @PostMapping("user/bindConnect/getUserInfoByUserId")
    UserInfoVO getUserInfoByUserId(@RequestParam("bindOpenid")String bindId);

    /**
     * 修改当前用户 是否自动登录
     * @param paramMap
     */
    @PostMapping("user/bindConnect/updateAotuLogin")
    void updateAotuLogin(@RequestBody Map<String, Object> paramMap);

    /**
     * 第三方 授权注册  微信小程序 app 授权注册
     *
     * @param map
     * @return
     */
    @PostMapping("user/ms/thirDregister")
    ResponseResult thirDregister(@RequestBody Map<String, String> map) throws IOException, TransactionException;

    /**
     * 查询用户是否自动登录
     */
    @PostMapping("user/bindConnect/queryAutoLogin")
    Integer queryAutoLogin(@RequestParam("openId") String openId);

    /**
     * 写入第三方绑定关系（统一登录扫码后绑定已有账号时使用）
     *
     * <p>bindId 必须带渠道前缀（如 weixin_ / wecom_ / dingtalk_ / qq_ / feishu_），
     * 与 selectUserIdByOpenid 的查询口径保持一致；重复绑定由服务侧按 bindId 幂等处理。
     *
     * @param userThreeBindMap 含 bindId / bind_type / user_id / bind_openid / bind_unionid 等
     */
    @PostMapping("user/bindConnect/insertUserThreeBindMap")
    ResponseResult insertUserThreeBindMap(@RequestBody Map<String, Object> userThreeBindMap);

    /**
     * 更新最后登录时间 登录次数
     * @param userId
     */
    @PostMapping("user/ms/updateLoginTimeCount")
    ResponseResult updateLoginTimeCount(@RequestParam("userId")String userId);

    /**
     * 删除用户信息
     * @param userId
     * @return
     */
    @PostMapping("/user/detail/h5/delUserInfo")
    ResponseResult delUserInfo(@RequestParam String userId);

    /**
     * 查询是否为商家子账号
     * @param map
     * @return
     */
    @PostMapping("/user/sellerBase/queryShopSellerBase")
    ResponseResult queryShopSellerBase(@RequestParam("userId") String userId);


    /**
     * TODO 补充方法说明
     * @param req TODO
     * @return TODO
     */
    @PostMapping("/from518/getUserAndShopInfo")
    ResponseResult<ThirdUserShopVO> getUserAndShopInfo(@RequestBody UserAndShopInfoReqVo req);

    /**
     * TODO 补充方法说明
     * @param req TODO
     * @return TODO
     */
    @PostMapping("/from518/getUserAndShopDetail")
    ResponseResult<ThirdUserShopVO> getUserAndShopDetail(@Valid @RequestBody UserAndShopInfoReqVo req);

    /**
     * TODO 补充方法说明
     * @param req TODO
     * @return TODO
     */
    @PostMapping("/from518/getUserPointsInfo")
    ResponseResult<UserPointsInfoVO> getUserPointsInfo(@Valid @RequestBody UserAndShopInfoReqVo req);

    /**
     * 根据第三方用户ID更新用户信息
     * @param userInfoVO
     */
    @PostMapping("user/ms/feign/updateUserInfoByThird")
    void updateUserInfoByThird(@RequestBody UserInfoVO userInfoVO);

    /**
     * 查询用户所属组织（登录写入 Redis 供财务等服务做组织隔离）
     *
     * @param userId 用户 ID
     * @return 组织列表
     */
    @GetMapping("/user/ms/organization/getUserOrganizations")
    ResponseResult getUserOrganizations(@RequestParam("userId") String userId);

}
