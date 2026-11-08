package com.houjiang.userms.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.shop.GateStoreUpdateDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.util.IpUtils;
import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.third.ThirdShopVO;
import com.houjiang.commons.vo.third.ThirdUserVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.MemberMessageListDTO;
import com.houjiang.userms.dto.SubAccountDTO;
import com.houjiang.userms.entity.UserInfoDetaileEntity;
import com.houjiang.userms.entity.UserInfoEntity;
import com.houjiang.userms.service.UserOrganizationService;
import com.houjiang.userms.vo.UserPersonalBaseVO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * @ClassName UserInfoController
 * @Description 用户基础信息
 * @Author Amos
 * @Date 2023/5/18 15:56
 * @Version 1.0
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("user/ms")
public class UserInfoController extends BaseController {

    @Autowired
    private UserOrganizationService userOrganizationService;

    /**
     * 根据手机号查用户信息
     *
     * @param userMobile 手机号
     * @return 结果
     */
    @PostMapping("/infoByMobile")
    public UserInfoVO queryPhoneNumCount2(@RequestParam("userMobile") String userMobile) {
        UserInfoVO result = userInfoService.getUserInfoByMobile(userMobile);
        return result;
    }

    /**
     * 查询用户信息及其关联表
     *
     * @param vo
     * @return 结果
     */
    @PostMapping("/querUserInfoByParams")
    public ResponseResult querUserInfoByParams(@RequestBody UserInfoVO vo) {
        ResponseResult result = userInfoService.querUserInfoByParams(vo);
        return result;
    }


    /**
     * 查询用户信息
     *
     * @param userInfoVO
     * @return 结果
     */
    @PostMapping("/queryUserInfo")
    public ResponseResult queryUserInfo(@RequestBody UserInfoVO userInfoVO) {
        ResponseResult result = userInfoService.queryUserInfo(userInfoVO);
        return result;
    }

    /**
     * 查询用户信息单表
     *
     * @param userInfoVO
     * @return 结果
     */
    @PostMapping("/queryUserVO")
    public ResponseResult queryUserInfoByUserInfoVO(@RequestBody Map<String, String> userInfoVO) {
        ResponseResult result = userInfoService.queryUserInfoByUserInfoVO(userInfoVO);
        return result;
    }


    /**
     * 根据userId获取用户信息
     *
     * @param userId
     * @return 结果
     */
    @GetMapping("/queryUserInfoByUserId")
    public ResponseResult queryUserInfoByUserId(@RequestParam("userId") String userId) {
        ResponseResult result = userInfoService.queryUserInfoByUserId(userId);
        return result;
    }


    /**
     * 修改用户表信息
     *
     * @param map
     * @return
     */
    @PostMapping("/updateUserInfoByMap")
    public ResponseResult updateUserInfoByMap(@RequestBody Map<String, String> map) {
        ResponseResult result = userInfoService.updateUserInfoByMap(map);
        return result;
    }


    /**
     * 修改用户积分
     *
     * @param param
     * @return
     */
    @PostMapping("/updateMemberIntegral")
    public ResponseResult updateMemberIntegral(@RequestBody MemberMessageListIntegralDTO param) {
        ResponseResult result = userInfoService.updateMemberIntegral(param);
        return result;
    }


    /**
     * 用户注册
     *
     * @param map
     * @return
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody Map<String, Object> map) throws IOException, TransactionException {
        // 将Map<String, Object>转换为Map<String, String>
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        ResponseResult result = userInfoService.register(stringMap);
        return result;
    }

    /**
     * 更新用户新人状态 feign
     *
     * @param paraMap
     */
    @PostMapping("/updateUser/isnew")
    public Integer updateIsNew(@RequestBody Map<String, String> paraMap) {
        Integer result = userInfoService.updateIsNew(paraMap);
        return result;
    }

    /**
     * feign 根据用户id 查询当前用户有多少积分
     *
     * @param userId
     * @return
     */
    @PostMapping("/getMemberIntegralByUserId")
    public Integer getMemberIntegralByUserId(@RequestParam("userId") String userId) {
        Integer result = userInfoService.queryMemberMessageListIntegral(userId);
        return result;
    }

    /**
     * feign 根据用户id 查询当前用户冻结积分
     *
     * @param userId
     * @return
     */
    @PostMapping("/getMemberFrozenIntegraIntegralByUserId")
    public Integer getMemberFrozenIntegraIntegralByUserId(@RequestParam("userId") String userId) {
        Integer result = userInfoService.getMemberFrozenIntegraIntegralByUserId(userId);
        return result;
    }

    /**
     * 查询用户总积分
     */
    @PostMapping("/queryPersonalDiscount")
    public ResponseResult queryPersonalDiscount(@RequestBody UserTokenBeanDTO userTokenBeanDTO) {
        ResponseResult result = userInfoService.queryPersonalDiscount(userTokenBeanDTO);
        return result;
    }

    /**
     * 查询用户积分信息-获取会员等级
     */
    @PostMapping("/getuserMemberInformation")
    public ResponseResult getUserMemberInformation(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.getUserMemberInformation(paraMap);
        return result;
    }

    /**
     * 校验用户是否已经注册
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/checkPhoneNumberExist")
    public String checkPhoneNumber(@RequestBody Map<String, String> paraMap) {
        String result = userInfoService.checkPhoneNumber(paraMap);
        return result;
    }

    /**
     * 校验用户是否已经注册
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/checkUserMobileExist")
    public ResponseResult checkUserMobileExist(@RequestBody Map<String, String> paraMap) {
        String result = userInfoService.checkPhoneNumber(paraMap);
        return StringUtils.isEmpty(result) ? ResponseResult.success() : ResponseResult.fail(0, "手机号已经注册");
    }

    /**
     * 企业用户注册
     */
    @PostMapping("/business/register")
    public ResponseResult businessegInsert(@RequestBody Map<String, String> map) throws IOException, TransactionException {
        ResponseResult result = userInfoService.businessegInsert(map);
        return result;
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/info/update")
    public ResponseResult updateUcenterInfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.updateUcenterInfo(paraMap);
        return result;
    }

    /**
     * 转账确认转账方信息
     */
    @PostMapping("/checkPay/transferInfo")
    public ResponseResult checkPayTransferInfo(@RequestBody Map<Object, Object> map) {
        ResponseResult result = userInfoService.checkPayTransferInfo(map);
        return result;
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/userLogOut")
    public ResponseResult userLogOut(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.userLogOut(paraMap);
        return result;
    }


    /**
     * 根据token返回用户信息
     *
     * @return
     */
    @PostMapping("/getuser")
    public ResponseResult getUser(@RequestBody Map<String, String> paraMap) throws IllegalAccessException {
        ResponseResult result = userInfoService.getUser(paraMap);
        return result;
    }

    /**
     * 更新用户头像
     *
     * @param file
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @PostMapping("/uploadPhoto")
    @SysLog("更新用户头像")
    public ResponseResult uploadImg(@RequestParam("file") MultipartFile file, @RequestParam("token") String token) throws IOException {
        ResponseResult result = userInfoService.uploadImg(file, token);
        return result;
    }

    /**
     * 更新用户头像  base64 传输
     *
     * @param paraMap
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @PostMapping("/uploadPhotoBase64")
    @SysLog("更新用户头像")
    public ResponseResult uploadImgBase64(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.uploadImgBase64(paraMap);
        return result;
    }


    /**
     * 转账确认转账方信息
     *
     * @param map
     * @return
     */
    @PostMapping("/checkPay/transferInfoByMap")
    public ResponseResult checkPayTransferInfoByMap(@RequestBody Map<Object, Object> map) {
        ResponseResult result = userInfoService.checkPayTransferInfoByMap(map);
        return result;
    }

    /**
     * 获取账户信息
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/userSetting/getInfo")
    public ResponseResult getUcenterSettingByUserId(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.getUcenterSettingByUserId(paraMap);
        return result;
    }

    /**
     * 根据shopId 查询店铺信息
     */
    @GetMapping("/queryShopUserInfo")
    public ResponseResult queryShopUserInfo(@RequestParam("shopId") Integer shopId) {
        ResponseResult result = userInfoService.queryShopUserInfo(shopId);
        return result;
    }

    /**
     * 检测验证码是否正确和更换绑定手机
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/updateUserMobile")
    public ResponseResult updateUserMobile(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.updateUserMobile(paraMap);
        return result;
    }


    /**
     * 根据token生成带有userId的二维码和注册页面链接
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/register/createqr/user")
    public ResponseResult qrUser(@RequestBody Map<String, String> paraMap) throws IOException, NotFoundException, WriterException {
        ResponseResult result = userInfoService.qrUser(paraMap);
        return result;

    }


    /**
     * 查询商家客户，根据shopid查询
     *
     * @param paraMap
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @throws NotFoundException
     * @throws WriterException
     */
    @PostMapping("/share/myCustomer")
    public ResponseResult myCustomer(@RequestBody Map<String, Object> paraMap)
            throws JsonParseException, JsonMappingException, IOException, NotFoundException, WriterException {
        ResponseResult result = userInfoService.myCustomer(paraMap);
        return result;

    }

    /**
     * 查询个人信息
     *
     * @param paraMap
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @PostMapping("/information/getUserInfo")
    public ResponseResult getUserInfo(@RequestBody Map<String, Object> paraMap)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseResult result = userInfoService.getUserInfo(paraMap);
        return result;

    }

    /**
     * 修改个人信息
     *
     * @param paraMap
     * @return
     */
    @SensitiveCheck(field = {"userNickname", "userTruename"})
    @PostMapping("/updateUserInfo")
    @SysLog("修改个人信息")
    public ResponseResult updateUserInfo(@RequestBody Map<String, String> paraMap) throws ParseException {
        ResponseResult result = userInfoService.updateUserInfo(paraMap);
        return result;
    }

    /**
     * 根据用户 ID 获取用户的组织列表
     *
     * @param params 包含 userId 的参数
     * @return ResponseResult
     */
    @PostMapping("/getUserOrgList")
    public ResponseResult getUserOrgList(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        if (userId == null || userId.isEmpty()) {
            return ResponseResult.fail(400, "userId 不能为空");
        }
        
        try {
            java.util.List<Map<String, Object>> orgList = userInfoService.getUserOrgList(userId);
            return ResponseResult.success(orgList);
        } catch (Exception e) {
            log.error("查询用户组织失败，userId={}", userId, e);
            return ResponseResult.fail(500, "查询用户组织失败：" + e.getMessage());
        }
    }

    /**
     * 根据手机号查询用户是否存在
     *
     * @param map
     * @return
     */
    @PostMapping("/userAccountByMap")
    public ResponseResult getUserAccountByMap(@RequestBody Map<String, Object> map) {
        Integer type = userInfoService.checkRepeateUserAccount(map);

        return type == CommonStatusEnum.FAIL.getCode() ? ResponseResult.fail(type) : ResponseResult.success(type);
    }


    /**
     * 忘记密码
     *
     * @param map
     * @return
     */
    @PostMapping({ "/forgot-password", "/forgotPassWord" })
    public ResponseResult forgotPassword(@RequestBody Map<String, Object> map) throws IOException {
        ResponseResult result = userInfoService.forgotPassword(map);
        return result;
    }


    /**
     * 添加修改用户积分记录 用与feign调用
     *
     * @param param
     * @return
     */
    @PostMapping("/fegin/addPointsLog")
    public ResponseResult feginAddpointsLog(@RequestBody UserPointLogDTO param) {
        ResponseResult result = userInfoService.feginAddpointsLog(param);
        return result;
    }

    /**
     * 登录名动态校验
     *
     * @param map
     * @return
     */
    @PostMapping("/checkPhone")
    public ResponseResult checkPhone(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoService.checkPhone(map);
        return result;
    }


    /**
     * 商家中心-验证手机号
     *
     * @param map
     * @return
     */
    @PostMapping("/business/checkPhone")
    public ResponseResult checkPhoneByBusiness(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoService.checkPhoneByBusiness(map);
        return result;
    }


    /**
     * 平台-校验该手机号是否注册
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/checkPhone")
    public ResponseResult checkPhoneByPlatform(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoService.checkPhoneByPlatform(map);
        return result;
    }

    /**
     * 平台 查询待审核企业用户信息
     *
     * @param
     * @return
     */
    @PostMapping("/platform/businessUserList")
    public ResponseResult queryAuditList(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.queryAuditList(param);
        return result;
    }

    /**
     * 企业用户信息审核
     *
     * @param
     * @return
     */
    @PostMapping("/platform/updateBusinessUser")
    public ResponseResult auditPass(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.auditPass(param);
        return result;
    }

    /**
     * 用户数量
     *
     * @param map
     * @return
     */
    @PostMapping("/userCount")
    public ResponseResult userCount(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoService.userCount(map);
        return result;
    }

    /**
     * 用户当日数量
     *
     * @param map
     * @return
     */
    @PostMapping("/userTodayNumber")
    public ResponseResult userTodayNumber(@RequestBody Map<String, Object> map) {
        ResponseResult result = userInfoService.userTodayNumber(map);
        return result;
    }


    /**
     * 修改会员登录密码
     */
    @PostMapping("/updateMemberLoginPassword")
    public ResponseResult updateMemberLoginPassword(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.updateMemberLoginPassword(param);
        return result;
    }

    /**
     * 更新用户信息UserInfo单表
     */
    @PostMapping("/updateUser")
    public ResponseResult updateUser(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.updateUser(paraMap);
        return result;
    }

    /**
     * 查询会员
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/queryUserTurePort")
    public ResponseResult queryUserTurePort(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.queryUserTurePort(paraMap);
        return result;
    }

    /**
     * 修改社区用户信息
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/update/articleUserInfo")
    public ResponseResult updateArticleUserInfo(@RequestBody Map<Object, Object> param) {
        ResponseResult result = userInfoService.updateArticleUserInfo(param);
        return result;
    }


    /**
     * 查询用户下家列表用户列表
     *
     * @param param
     * @return
     */
    @PostMapping("/queryShareUserListPort")
    public ResponseResult queryShareUserListPort(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.queryShareUserListPort(param);
        return result;
    }


    /**
     * 修改会员信息详情
     *
     * @param param
     * @return
     */
    @PostMapping("/updateMemberMessageDetailPort")
    public ResponseResult updateMemberMessageDetailPort(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.updateMemberMessageDetailPort(param);
        return result;
    }

    /**
     * 平台用户 编辑个人信息
     *
     * @param param
     * @return
     */
    @PostMapping("/limit/updatePersonalBase")
    public ResponseResult updatePersonalBase(@RequestBody UserPersonalBaseVO param) {
        ResponseResult result = userInfoService.updatePersonalBase(param);
        return result;
    }

    /**
     * 查询平台用户或者品牌店铺用户信息
     *
     * @param checkPhoneMap
     * @return
     */
    @PostMapping("/getUserInfoByPhoneNum")
    public UserInfoVO getUserInfoByPhoneNum(@RequestBody Map<String, String> checkPhoneMap) {
        UserInfoVO result = userInfoService.getUserInfoByPhoneNum(checkPhoneMap);
        return result;
    }


    /**
     * 小程序注册成功 头像 昵称 覆盖pc
     *
     * @param updateUserParam
     */
    @PostMapping("/updateUserNickNameavAtarUrl")
    public ResponseResult updateUserNickNameavAtarUrl(@RequestBody Map<String, Object> updateUserParam) {
        ResponseResult result = userInfoService.updateUserNickNameavAtarUrl(updateUserParam);
        return result;
    }

    /**
     * 第三方 授权注册  微信小程序 app 授权注册
     *
     * @param map
     * @return
     */
    @GlobalTransactional
    @PostMapping("/thirDregister")
    public ResponseResult thirDregister(@RequestBody Map<String, String> map) throws IOException, TransactionException {
        ResponseResult result = userInfoService.thirDregister(map);
        return result;
    }

    /**
     * 根据userId更改是否商家状态
     *
     * @param userId
     * @param shopId
     * @param isShop
     * @return
     */
    @GetMapping("/updateUserIsShop")
    public ResponseResult updateUserIsShop(@RequestParam("userId") String userId, @RequestParam("shopId") Integer shopId,
                                           @RequestParam("isShop") Integer isShop) {
        ResponseResult result = userInfoService.updateUserIsShop(userId, shopId, isShop);
        return result;
    }


    /**
     * 商户注册
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/addseller")
    @SysLog("商户注册")
    public ResponseResult addSeller(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.addSeller(paraMap);
        return result;
    }

    /**
     * 平台查询 活跃用户列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/queryUserTrueList")
    public ResponseResult queryUserTrueListPort(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.queryUserTrueListPort(paraMap);
        return result;
    }

    /**
     * 平台  查看 个人资料
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/queryMemberDetail")
    public ResponseResult queryMemberMessageDetailPort(@RequestBody MemberMessageListIntegralDTO dto) {
        ResponseResult result = userInfoService.queryMemberMessageDetailPort(dto);
        return result;
    }

    /**
     * 平台  查看 个人资料
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/queryMemberEncoded")
    public ResponseResult queryMemberMessageDetailPortEncoded(@RequestBody MemberMessageListIntegralDTO dto) {
        ResponseResult result = userInfoService.queryMemberMessageDetailPortEncoded(dto);
        return result;
    }

    /**
     * 平台修改 用户积分
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/updateMembeIntegral")
    public ResponseResult updateMemberMessageIntegral(@RequestBody MemberMessageListIntegralDTO dto) {
        ResponseResult result = userInfoService.updateMemberMessageIntegral(dto);
        return result;
    }

    /**
     * 平台 查询会员列表 导出
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/queryMemberExcel")
    public ResponseResult queryMemberMessageListPortExcel(@RequestBody MemberMessageListDTO dto) {
        ResponseResult result = userInfoService.queryMemberMessageListPortExcel(dto);
        return result;
    }


    /**
     * 平台 查询会员列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/platform/queryMemberMessageList")
    public ResponseResult queryMemberMessageListPort(@RequestBody MemberMessageListDTO dto) {
        ResponseResult result = userInfoService.queryMemberMessageListPort(dto);
        return result;
    }


    /**
     * 门店注册接口
     *
     * @param gateAddStoreDTO
     * @return
     */
    @PostMapping("/addGateStore")
    public ResponseResult addGateStore(@RequestBody(required = true) GateStoreUpdateDTO gateAddStoreDTO) {
        ResponseResult result = userInfoService.addGateStore(gateAddStoreDTO);
        return result;
    }

    /**
     * @param paraMap
     * @return
     */
    @PostMapping("/register/createqr")
    public ResponseResult registerCreateqr(@RequestBody Map<String, String> paraMap) throws IOException, NotFoundException, WriterException {
        ResponseResult result = userInfoService.registerCreateqr(paraMap);
        return result;

    }


    /**
     * 品牌店铺查询会员
     *
     * @param param
     * @return
     */
    @PostMapping("/business/queryMember")
    public ResponseResult queryMember(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.queryMember(param);
        return result;
    }

    /**
     * 会员信息导出
     *
     * @param param
     * @return
     */
    @PostMapping("/memberMessageListExp")
    public ResponseResult memberMessageListExp(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.memberMessageListExp(param);
        return result;
    }

    /**
     * 品牌店铺更改会员状态
     *
     * @param param
     * @return
     */
    @PostMapping("/updateMemberStatus")
    public ResponseResult updatememberstatus(@RequestBody Map<String, Object> param) {
        ResponseResult result = userInfoService.updatememberstatus(param);
        return result;
    }

    /**
     * 校验手机号是否审核通过
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/checkPhoneReview")
    public ResponseResult checkphone(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.checkphone(paraMap.get("userMobile"));
        return result;
    }

    /**
     * 更新最后登录时间 登录次数
     *
     * @param userId
     */
    @PostMapping("/updateLoginTimeCount")
    public ResponseResult updateLoginTimeCount(@RequestParam("userId") String userId) {
        ResponseResult result = userInfoService.updateLoginTimeCount(userId);
        return result;
    }

    /**
     * 更新生日和性别
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/h5/updateUserBirthAndGender")
    public ResponseResult updateUserBirthAndGender(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = userInfoService.updateUserBirthAndGender(paraMap);
        return result;

    }

    /**
     * 定时任务-根据userId获取用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/job/queryUserInfoByjob")
    public AcquiescenceVO queryUserInfoByjob(@RequestParam("userId") String userId) {
        AcquiescenceVO result = userInfoService.queryUserInfoByjob(userId);
        return result;
    }

    /**
     * 更新用户实名认证信息
     */
    @PostMapping("/verified/update")
    public ResponseResult updateVerifiedInfo(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = userInfoService.updateVerifiedInfo(paraMap);
        return result;
    }

    /**
     * 查询店铺会员 所需用户信息
     *
     * @param shopUserDTO
     * @return
     */
    @PostMapping("/shopUser/queryUserInfo")
    public ResponseResult shopUserQueryUserInfo(@RequestBody ShopUserDTO shopUserDTO) {
        ResponseResult result = userInfoService.shopUserQueryUserInfo(shopUserDTO);
        return result;
    }

    /**
     * 根据会员id 查询会员账号 feign
     *
     * @param userId
     * @return
     */
    @PostMapping("queryUserMobileByuserId")
    public Map<String, String> queryUserMobileByuserId(@RequestParam String userId) {
        Map<String, String> result = userInfoService.queryUserMobileByuserId(userId);
        return result;
    }

    /**
     * im 查询用户信息
     * 该方法用于处理IM（即时通讯）系统查询用户信息的请求
     * 它接收一个参数映射，其中包含查询所需的参数，并返回查询结果
     *
     * @param paraMap 包含查询参数的映射，如用户ID或其他标识信息
     * @return 返回一个ResponseResult对象，其中包含查询结果或错误信息
     */
    @PostMapping("imQueryUserInfo")
    public ResponseResult imQueryUserInfo(@RequestBody Map<String, String> paraMap) {
        // 调用userInfoService的imQueryUserInfo方法进行用户信息查询
        ResponseResult result = userInfoService.imQueryUserInfo(paraMap);
        // 返回查询结果
        return result;
    }

    /**
     * 查询用户信息
     *
     * @param map
     * @return
     */
    @PostMapping("/getUserInfo")
    public ResponseResult getUserInfoByMapStr(@RequestBody Map<String, String> map) {
        ResponseResult result = userInfoService.getUserInfoByMapStr(map);
        return result;
    }

    /**
     * 商家创建子账号时校验账号
     *
     * @param param
     * @return
     */
    @PostMapping("/shop/checkUserName")
    public ResponseResult shopCheckUserName(@Validated({Select.class}) @RequestBody SubAccountDTO param) {
        return userInfoService.shopCheckUserName(param);
    }

    /**
     * 更新是否是新人
     *
     * @param paraMap
     */
    @PostMapping("/feign/updateIsNew")
    public void updateIsNewFeign(@RequestBody Map<String, String> paraMap) {
        userInfoService.updateIsNewFeign(paraMap);
    }

    /**
     * 更新用户密码
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/feign/forgotPassword")
    public Integer forgotPasswordFeign(@RequestBody Map<String, Object> paraMap) {
        Integer result = userInfoService.forgotPasswordFeign(paraMap);
        return result;
    }

    /**
     * 获取用户组织信息
     * 用于前端在登记公司后更新本地缓存
     *
     * @return 用户组织信息
     */
    @PostMapping("/getUserOrganizations")
    public ResponseResult getUserOrganizations() {
        try {
            // 从token中获取用户ID
            String userId = RequestTokenUtils.getUserIdFromRequest();
            if (userId == null) {
                return ResponseResult.fail("用户未登录");
            }
            
            // 调用用户组织关联服务获取组织信息
            ResponseResult result = userOrganizationService.getUserOrganizations(userId);
            return result;
            
        } catch (Exception e) {
            log.error("获取用户组织信息异常", e);
            return ResponseResult.fail("系统异常，请稍后重试");
        }
    }

    /**
     * 查询用户id
     *
     * @param userMobile
     * @return
     */
    @GetMapping("/feign/queryUserInfoUserId")
    public String queryUserInfoUserId(@RequestParam("userMobile") String userMobile) {
        String result = userInfoService.queryUserInfoUserId(userMobile);
        return result;
    }

    /**
     * 根据userid查询未锁定状态的用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/feign/getUserInfoByPhoneNumber")
    public UserInfoVO getUserInfoByPhoneNumber(@RequestParam("userId") String userId) {
        UserInfoVO result = userInfoService.getUserInfoByPhoneNumber(userId);
        return result;
    }

    /**
     * 校验手机号是否已注册
     *
     * @param param
     * @return
     */
    @PostMapping("/feign/checkPhoneNumber")
    public Integer checkPhoneNumberFeign(@RequestBody Map<String, Object> param) {
        Integer result = userInfoService.checkPhoneNumberFeign(param);
        return result;

    }

    /**
     * 查询创建人ID
     *
     * @param userId
     * @return
     */
    @GetMapping("/feign/getPrimaryAccountId")
    public String getPrimaryAccountId(@RequestParam("userId") String userId) {
        String result = userInfoService.getPrimaryAccountId(userId);
        return result;
    }

    /**
     * 查询联系方式
     *
     * @param userId
     * @return
     */
    @GetMapping("/feign/selectuserphone")
    public String selectuserphone(@RequestParam("userId") String userId) {
        String result = userInfoService.selectuserphone(userId);
        return result;
    }

    /**
     * 查询会员等级下会员
     *
     * @param id
     * @return
     */
    @GetMapping("/feign/queryMemberMessageListIntegral")
    public Integer queryMemberMessageListIntegral(@RequestParam("id") String id) {
        Integer result = userInfoService.queryMemberMessageListIntegral(id);
        return result;
    }

    /**
     * 获取用户信息
     *
     * @param userMobile
     * @return
     */
    @GetMapping("/feign/getUserInfoByMobile")
    public UserInfoVO getUserInfoByMobile(@RequestParam("userMobile") String userMobile) {
        UserInfoVO result = userInfoService.getUserInfoByMobileFeign(userMobile);
        return result;
    }

    /**
     * 根据用户ID查找用户名称
     *
     * @param userId 用户Id
     * @return 用户名称
     */
    @GetMapping("/feign/queryUserNameByUserId")
    public String queryUserNameByUserId(@RequestParam("userId") String userId) {
        return userInfoService.queryUserNameByUserId(userId);
    }


    /**
     * 根据第三方用户ID更新用户信息
     *
     * @param userInfoVO
     */
    @PostMapping("/feign/updateUserInfoByThird")
    public void updateUserInfoByThird(@RequestBody UserInfoVO userInfoVO) throws UnknownHostException {

        log.info("从518平台获取用户信息:{}", userInfoVO);
        ThirdUserVO userVO = userInfoVO.getThirdUserVO();

        String userId = String.valueOf(userVO.getId());
        UserInfoEntity userInfo = userInfoService.getById(userId);
        if (BeanUtil.isEmpty(userInfo)) {
            userInfo = new UserInfoEntity();
            BeanUtil.copyProperties(userInfoVO, userInfo);
            userInfo.setUserId(userId);
            userInfo.setUserId(String.valueOf(userVO.getId()));
            userInfo.setUserName(userVO.getUserNickName());
            userInfo.setUserMobile(userVO.getUserPhone());
            userInfo.setUserNickname(userVO.getUserNickName());
            userInfo.setUserState(userVO.getStatus());
            userInfo.setUserIsNew(1);
            userInfo.setTs(new Date());
            userInfo.setPassword(RandomUtil.randomString(8));
            userInfo.setUserLastloginTime(new Date());
            userInfo.setUserLastloginIp(IpUtils.getIpAddress());
            userInfo.setCreateTime(new Date());
            userInfoService.save(userInfo);
        } else {
            BeanUtil.copyProperties(userInfoVO, userInfo);
            userInfoService.updateById(userInfo);
        }

        UserInfoDetaileEntity infoDetaile = userInfoDetailService.getOne(new LambdaQueryWrapper<UserInfoDetaileEntity>()
                .eq(UserInfoDetaileEntity::getUserId, userId));

        ThirdShopVO vo = userInfoVO.getThirdShopVO();

        if (BeanUtil.isEmpty(infoDetaile)) {
            infoDetaile = new UserInfoDetaileEntity();
            infoDetaile.setUserId(userId);
            infoDetaile.setUserBirth(userInfoVO.getUserBirth());
            infoDetaile.setUserTruename(userInfoVO.getUserRealname());
            infoDetaile.setUserMobileVerify(1);
            infoDetaile.setUserPlatform(2);
            infoDetaile.setUserRegTime(new Date().getTime() / 1000);
            infoDetaile.setIsBlack(userVO.getIsBlack());
            infoDetaile.setOutShopId(vo.getId());
            infoDetaile.setOutShopName(vo.getShopName());
            infoDetaile.setOutShopType(vo.getShopType());
            infoDetaile.setOutShopCode(vo.getOutShopCode());
            infoDetaile.setRegionName(vo.getRegionName());
            infoDetaile.setOfficeName(vo.getOfficeName());
            infoDetaile.setZoneName(vo.getZoneName());
            infoDetaile.setProvince(vo.getProvince());
            infoDetaile.setCity(vo.getCity());
            infoDetaile.setArea(vo.getArea());
            infoDetaile.setRelationSaleName(vo.getRelationSaleName());
            infoDetaile.setBizShopCode(vo.getShopCode());
            infoDetaile.setShopDetailAddress(vo.getShopDetailAddress());
            infoDetaile.setBusinessLicenseRegNum(vo.getBusinessLicenseRegNum());

            infoDetaile.setUserLevel(userInfoVO.getUserLevel());
            infoDetaile.setPlaceOrderStatus(userVO.getPlaceOrderStatus());
            userInfoDetailService.save(infoDetaile);
        } else {
            BeanUtil.copyProperties(vo, infoDetaile);
            infoDetaile.setUserId(userId);
            infoDetaile.setUserBirth(userInfoVO.getUserBirth());
            infoDetaile.setUserTruename(userInfoVO.getUserRealname());
            infoDetaile.setOutShopId(vo.getId());
            infoDetaile.setOutShopName(vo.getShopName());
            infoDetaile.setOutShopType(vo.getShopType());
            infoDetaile.setOutShopCode(vo.getOutShopCode());
            infoDetaile.setIsBlack(userVO.getIsBlack());
            infoDetaile.setUserLevel(userInfoVO.getUserLevel());
            infoDetaile.setPlaceOrderStatus(userVO.getPlaceOrderStatus());
            userInfoDetailService.updateById(infoDetaile);
        }
    }
}

