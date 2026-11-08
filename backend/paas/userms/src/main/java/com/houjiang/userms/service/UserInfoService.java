package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.shop.GateStoreUpdateDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.userms.UserPointLogDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.dto.MemberMessageListDTO;
import com.houjiang.userms.dto.SubAccountDTO;
import com.houjiang.userms.entity.UserBindConnectEntity;
import com.houjiang.userms.entity.UserInfoDetaileEntity;
import com.houjiang.userms.entity.UserInfoEntity;
import com.houjiang.userms.entity.UserPointsLogEntity;
import com.houjiang.userms.vo.UserPersonalBaseVO;
import io.seata.core.exception.TransactionException;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


/**
 * @author Amos
 */
public interface UserInfoService extends IService<UserInfoEntity> {

    /**
     * 根据手机号获取用户信息
     *
     * @param userMobile
     * @return
     */
    UserInfoVO getUserInfoByMobile(String userMobile);

    /**
     * 走品牌查询
     *
     * @param userMobile
     * @param shopId
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumber1(@Param("userMobile") String userMobile, @Param("shopId") String shopId);


    /**
     * 查询用户信息及其关联表
     *
     * @param vo
     * @return 结果
     */
    ResponseResult querUserInfoByParams(UserInfoVO vo);


    /**
     * 查询用户信息
     *
     * @param userInfoVO
     * @return
     */
    ResponseResult queryUserInfo(UserInfoVO userInfoVO);


    /**
     * 查询用户信息单表
     *
     * @param userInfoVO
     * @return 结果
     */

    /**
     * TODO 补充方法说明
     * @param userInfoVO TODO
     * @return TODO
     */
    ResponseResult queryUserInfoByUserInfoVO(Map<String, String> userInfoVO);

    /**
     * 根据userId获取用户信息
     *
     * @param userId
     * @return 结果
     */
    ResponseResult queryUserInfoByUserId(String userId);

    /**
     * 修改用户信单表信息
     *
     * @param map
     */
    ResponseResult updateUserInfoByMap(Map<String, String> map);

    /**
     * 修改用户积分
     *
     * @param param
     */
    ResponseResult updateMemberIntegral(MemberMessageListIntegralDTO param);

    /**
     * 更新用户新人状态 feign
     *
     * @param paraMap
     * @return
     */
    Integer updateIsNew(Map<String, String> paraMap);


    /**
     * 用户注册
     *
     * @param map
     */

    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    ResponseResult register(Map<String, String> map) throws UnknownHostException, TransactionException;


    /**
     * 插入用户信息
     *
     * @param userinfo
     * @param insertUserInfoDetail
     * @param userBindMap
     * @param userPointLog
     * @return
     */
    Boolean insertUserInfo(UserInfoEntity userinfo, UserInfoDetaileEntity insertUserInfoDetail,
                           UserBindConnectEntity userBindMap, UserPointsLogEntity userPointLog) throws TransactionException;

    /**
     * 根据用户id 查询当前用户有多少积分
     *
     * @param userId
     * @return
     */
    Integer queryMemberMessageListIntegral(String userId);

    /**
     * 根据用户id 查询当前用户冻结积分
     *
     * @param userId
     * @return
     */
    Integer getMemberFrozenIntegraIntegralByUserId(String userId);

    /**
     * 查询用户总积分
     *
     * @param userTokenBeanDTO
     * @return
     */
    ResponseResult queryPersonalDiscount(UserTokenBeanDTO userTokenBeanDTO);

    /**
     * 查询用户积分信息
     *
     * @param paraMap
     * @return
     */
    ResponseResult getUserMemberInformation(Map<String, String> paraMap);

    /**
     * 校验用户是否已经注册
     *
     * @param paraMap
     * @return
     */
    String checkPhoneNumber(Map<String, String> paraMap);

    /**
     * 企业用户注册
     *
     * @param map
     * @return
     */
    ResponseResult businessegInsert(Map<String, String> map) throws UnknownHostException, TransactionException;

    /**
     * 更新用户信息
     *
     * @param paraMap
     * @return
     */
    ResponseResult updateUcenterInfo(Map<String, Object> paraMap);

    /**
     * 转账确认转账方信息
     *
     * @param map
     * @return
     */
    ResponseResult checkPayTransferInfo(Map<Object, Object> map);

    /**
     * 用户退出登录
     *
     * @param paraMap
     * @return
     */
    ResponseResult userLogOut(Map<String, String> paraMap);


    /**
     * 根据token返回用户信息
     *
     * @return
     * @throws IOException
     */
    ResponseResult getUser(Map<String, String> paraMap) throws IllegalAccessException;

    /**
     * 更新用户头像
     *
     * @param token
     * @return
     */
    ResponseResult uploadImg(MultipartFile file, String token);

    /**
     * 更新用户头像
     *
     * @param paraMap
     * @return
     */
    ResponseResult uploadImgBase64(Map<String, String> paraMap);

    /**
     * 转账确认转账方信息
     *
     * @param map
     * @return
     */
    ResponseResult checkPayTransferInfoByMap(Map<Object, Object> map);

    /**
     * 获取账户信息
     *
     * @param param
     * @return
     */
    ResponseResult getUcenterSettingByUserId(Map<String, String> param);

    /**
     * 根据shopId 查询店铺信息
     *
     * @param shopId
     * @return
     */
    ResponseResult queryShopUserInfo(Integer shopId);

    /**
     * 检测验证码是否正确和更换绑定手机
     *
     * @param paraMap
     * @return
     */
    ResponseResult updateUserMobile(Map<String, String> paraMap);


    /**
     * 根据token生成带有userId的二维码和注册页面链接
     *
     * @param paraMap
     * @return
     */
    ResponseResult qrUser(Map<String, String> paraMap) throws NotFoundException, IOException, WriterException;


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
    ResponseResult myCustomer(Map<String, Object> paraMap)
            throws JsonParseException, JsonMappingException, IOException, NotFoundException, WriterException;


    /**
     * 查询个人信息
     *
     * @param paraMap
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    ResponseResult getUserInfo(Map<String, Object> paraMap)
            throws JsonParseException, JsonMappingException, IOException;


    /**
     * 修改个人信息
     *
     * @param map
     * @return
     */
    ResponseResult updateUserInfo(Map<String, String> map) throws ParseException;


    /**
     * 判断用户账号状态
     *
     * @param map
     */
    Integer checkRepeateUserAccount(Map<String, Object> map);

    /**
     * 忘记密码
     *
     * @param map
     */
    ResponseResult forgotPassword(Map<String, Object> map) throws IOException;

    /**
     * 添加修改用户积分记录 用与feign调用
     */
    ResponseResult feginAddpointsLog(UserPointLogDTO paraMap);

    /**
     * 登录名动态校验
     *
     * @param map
     * @return
     */
    ResponseResult checkPhone(Map<String, Object> map);


    /**
     * 商家中心-验证手机号
     *
     * @param map
     * @return
     */
    ResponseResult checkPhoneByBusiness(Map<String, Object> map);

    /**
     * 平台 查询待审核企业用户信息
     *
     * @param
     * @return
     */
    ResponseResult queryAuditList(Map<String, Object> param);

    /**
     * 企业用户信息审核
     *
     * @param
     * @return
     */
    ResponseResult auditPass(Map<String, Object> param);

    /**
     * 用户数量
     *
     * @param map
     * @return
     */
    ResponseResult userCount(Map<String, Object> map);

    /**
     * 用户当日数量
     *
     * @param map
     * @return
     */
    ResponseResult userTodayNumber(Map<String, Object> map);

    /**
     * 修改会员登录密码
     */
    ResponseResult updateMemberLoginPassword(Map<String, Object> param);

    /**
     * 更新用户信息UserInfo单表
     */
    ResponseResult updateUser(Map<String, Object> paraMap);

    /**
     * 查询会员
     *
     * @param paraMap
     * @return
     */
    ResponseResult queryUserTurePort(Map<String, Object> paraMap);


    /**
     * 查询用户下家列表用户列表
     *
     * @param param
     * @return
     */
    ResponseResult queryShareUserListPort(Map<String, Object> param);

    /**
     * 修改会员信息详情
     *
     * @param param
     * @return
     */
    ResponseResult updateMemberMessageDetailPort(Map<String, Object> param);

    /**
     * 修改社区用户信息
     *
     * @param param
     * @return
     */
    ResponseResult updateArticleUserInfo(Map<Object, Object> param);

    /**
     * 平台用户 编辑个人信息
     *
     * @param param
     * @return
     */
    ResponseResult updatePersonalBase(UserPersonalBaseVO param);

    /**
     * 查询平台用户或者品牌店铺用户信息
     *
     * @param checkPhoneMap
     * @return
     */
    UserInfoVO getUserInfoByPhoneNum(Map<String, String> checkPhoneMap);

    /**
     * 小程序注册成功 头像 昵称 覆盖pc
     *
     * @param updateUserParam
     * @return
     */
    ResponseResult updateUserNickNameavAtarUrl(Map<String, Object> updateUserParam);

    /**
     * 第三方 授权注册  微信小程序 app 授权注册
     *
     * @param map
     * @return
     */
    ResponseResult thirDregister(Map<String, String> map) throws UnknownHostException, TransactionException;

    /**
     * 根据userId更改是否商家状态
     *
     * @param userId
     * @param shopId
     * @param isShop
     * @return
     */
    ResponseResult updateUserIsShop(String userId, Integer shopId, Integer isShop);

    /**
     * 商户注册
     *
     * @param paraMap
     * @return
     */
    ResponseResult addSeller(Map<String, Object> paraMap);


    /**
     * 平台查询 活跃用户列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult queryUserTrueListPort(Map<String, Object> paraMap);


    /**
     * 平台  查看 个人资料
     *
     * @param dto
     * @return
     */
    ResponseResult queryMemberMessageDetailPort(MemberMessageListIntegralDTO dto);

    /**
     * 平台  查看 个人资料
     *
     * @param dto
     * @return
     */
    ResponseResult queryMemberMessageDetailPortEncoded(MemberMessageListIntegralDTO dto);


    /**
     * 平台修改 用户积分
     *
     * @param dto
     * @return
     */
    ResponseResult updateMemberMessageIntegral(MemberMessageListIntegralDTO dto);


    /**
     * 平台 查询会员列表 导出
     *
     * @param param
     * @return
     */
    ResponseResult queryMemberMessageListPortExcel(MemberMessageListDTO param);


    /**
     * 平台 查询会员列表
     *
     * @param dto
     * @return
     */
    ResponseResult queryMemberMessageListPort(MemberMessageListDTO dto);


    /**
     * 门店注册接口
     *
     * @param gateAddStoreDTO
     * @return
     */
    ResponseResult addGateStore(GateStoreUpdateDTO gateAddStoreDTO);


    /**
     * @param paraMap
     * @return
     */
    ResponseResult registerCreateqr(Map<String, String> paraMap) throws NotFoundException, IOException, WriterException;


    /**
     * 品牌店铺查询会员
     *
     * @param param
     * @return
     */
    ResponseResult queryMember(Map<String, Object> param);


    /**
     * 会员信息导出
     *
     * @param param
     * @return
     */
    ResponseResult memberMessageListExp(Map<String, Object> param);

    /**
     * 品牌店铺更改会员状态
     *
     * @param param
     * @return
     */
    ResponseResult updatememberstatus(Map<String, Object> param);

    /**
     * 校验手机号是否审核通过
     *
     * @param userMobile
     * @return
     */
    ResponseResult checkphone(String userMobile);

    /**
     * 更新最后登录时间 登录次数
     *
     * @param userId
     * @return
     */
    ResponseResult updateLoginTimeCount(String userId);

    /**
     * 更新生日和性别
     *
     * @param paraMap
     * @return
     */
    ResponseResult updateUserBirthAndGender(Map<String, String> paraMap);

    /**
     * 更新用户实名认证信息
     */
    ResponseResult updateVerifiedInfo(Map<String, Object> paraMap);


    /**
     * 平台-校验该手机号是否注册
     *
     * @param map
     * @return
     */
    ResponseResult checkPhoneByPlatform(Map<String, Object> map);

    /**
     * 定时任务-根据userId获取用户信息
     *
     * @param userId
     * @return
     */
    AcquiescenceVO queryUserInfoByjob(String userId);

    /**
     * 查询店铺会员 所需用户信息
     * @param shopUserDTO
     * @return
     */
    ResponseResult shopUserQueryUserInfo(ShopUserDTO shopUserDTO);

    /**
     * 根据会员id 查询会员账号 feign
     * @param userId
     * @return
     */
    Map<String,String> queryUserMobileByuserId(String userId);

    /**
     * im 查询用户信息
     * 该方法用于处理IM（即时通讯）系统查询用户信息的请求
     * 它接收一个参数映射，其中包含查询所需的参数，并返回查询结果
     *
     * @param paraMap 包含查询参数的映射，如用户ID或其他标识信息
     * @return 返回一个ResponseResult对象，其中包含查询结果或错误信息
     */
    ResponseResult imQueryUserInfo(Map<String, String> paraMap);

    /**
     * 查询用户信息
     *
     * @param map
     * @return
     */
    ResponseResult getUserInfoByMapStr(Map<String, String> map);

    /**
     * 商家创建子账号时校验账号
     *
     * @param param
     * @return
     */
    ResponseResult shopCheckUserName(SubAccountDTO param);


    /**
     * 更新是否是新人
     * @param paraMap
     */
    void updateIsNewFeign(Map<String, String> paraMap);


    /**
     * 更新用户密码
     *
     * @param paraMap
     * @return
     */
    Integer forgotPasswordFeign(Map<String, Object> paraMap);


    /**
     * 查询用户id
     *
     * @param userMobile
     * @return
     */
    String queryUserInfoUserId(String userMobile);


    /**
     * 根据userid查询未锁定状态的用户
     * @param userId
     * @return
     */
    UserInfoVO getUserInfoByPhoneNumber(String userId);




    /**
     * 校验手机号是否已注册
     *
     * @param param
     * @return
     */
    Integer checkPhoneNumberFeign(Map<String, Object> param);


    /**
     * 查询创建人ID
     * @param userId
     * @return
     */
    String getPrimaryAccountId(String userId);


    /**
     * 查询用户手机号
     * @param userId
     * @return
     */
    String selectuserphone(String userId);

    /**
     * 获取用户信息
     *
     * @param userMobile
     * @return
     */
    UserInfoVO getUserInfoByMobileFeign(String userMobile);

    /**
     * 根据用户 ID 查找用户名称
     *
     * @param userId 用户 Id
     * @return 用户名称
     */
    String queryUserNameByUserId(String userId);

    /**
     * 根据用户 ID 获取用户的组织列表
     *
     * @param userId 用户 ID
     * @return 组织列表
     */
    List<Map<String, Object>> getUserOrgList(String userId);
}
