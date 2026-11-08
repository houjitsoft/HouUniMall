package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayCommonUtils;
import com.houjiang.payment.entity.PayUserBaseEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayAssetDetailsMapper;
import com.houjiang.payment.mapper.PayUserBaseMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.PayUserBaseService;
import com.houjiang.payment.service.PayUserResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayUserBaseServiceImpl extends ServiceImpl<PayUserBaseMapper, PayUserBaseEntity>
    implements PayUserBaseService{

    @Autowired
    private PayUserResourceService payUserResourceService;
    @Autowired
    private PayUserResourceMapper payUserResourceMapper;
    @Autowired
    private PayAssetDetailsMapper payAssetDetailsMapper;
    @Autowired
    private UsermsServiceClient usermsServiceClient;
    @Autowired
    private PayCommonUtils payCommonUtils;


    // 错误次数
    static Integer errorNum = 0;
    // 总次数
    static Integer errorSum = 3;

    /**
     * 创建用户。
     * <p>
     * 该方法用于在系统中创建一个新的用户。它首先创建一个支付用户基础信息实体，并设置用户的ID。
     * 然后，尝试插入这个基础信息到数据库。接着，创建一个支付用户资源信息实体，并同样设置用户的ID。
     * 最后，尝试保存用户资源信息。方法根据资源信息的保存结果，返回成功或失败的响应。
     *
     * @param userId 用户的唯一标识符。
     * @return 创建用户的操作结果。如果资源信息保存成功，则返回成功结果；否则，返回失败结果。
     */
    @Override
    public ResponseResult createUser(String userId) {
        // 创建支付用户基础信息实体并设置用户ID
        PayUserBaseEntity record = new PayUserBaseEntity();
        record.setUserId(userId);

        // 尝试插入支付用户基础信息，返回影响的行数
        int isInsertByUserBaseSuccess = this.baseMapper.insert(record);

        // 创建支付用户资源信息实体并设置用户ID
        PayUserResourceEntity payUserResourceEntity = new PayUserResourceEntity();
        payUserResourceEntity.setUserId(userId);

        // 尝试保存支付用户资源信息，返回是否保存成功
        boolean isInsertByUseResourceSuccess = payUserResourceService.save(payUserResourceEntity);

        // 根据资源信息保存结果返回相应的操作结果
        return isInsertByUseResourceSuccess ? ResponseResult.success() : ResponseResult.fail();
    }

    /**
     * 设置支付密码
     * 通过传入的参数map，从中提取token、verifyCode和passwd，用于验证用户身份并设置支付密码。
     * 首先验证验证码是否正确，然后更新用户的支付密码。如果操作成功，返回相应的成功信息，否则返回失败信息。
     *
     * @param map 包含token、verifyCode和passwd等必要信息的参数映射。
     * @return 返回设置支付密码的结果，成功或失败。
     */
    @Override
    public ResponseResult setPaymentPassword(Map<String, Object> map) {
        // 从映射中提取token和验证码
        String token = (String) map.get("token");
        // 获取验证码
        String verifyCode = (String) map.get("verifyCode");
        // 尝试通过token获取用户信息
        // verify:1 设置支付密码 2：找回支付密码
        try {
            // 获取user信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            String userId = ui.getUserId();
            // 对传入的支付密码进行加密处理
            //将密码加密
            String password = PasswordUtils.BCryptPasswordEncoder((String) map.get("passwd"));
            // 从Redis中获取存储的验证码，用于与用户输入的验证码进行比对
            String code = RedisUtil.get(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
            // 验证码比对，若正确则继续操作
            if (verifyCode.equals(code)) {
                // 更新用户的支付密码，并检查操作是否成功
                boolean isChangePasswd = this.baseMapper.update(null, new LambdaUpdateWrapper<PayUserBaseEntity>()
                        .eq(PayUserBaseEntity::getUserId, userId)
                        .set(PayUserBaseEntity::getUserPayPasswd, password)) == CommonStatusEnum.SUCCESS.getCode();
                // 根据密码更新结果，返回相应的成功或失败信息
                if (isChangePasswd) {
                    return ResponseResult.success(BizCodeEnume.PASSWORD_UPDATED_SUCCESSFULLY);
                } else {
                    return ResponseResult.fail();
                }
            } else {
                // 验证码错误，返回失败信息
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
            }
        } catch (Exception e) {
            // 操作过程中发生异常，返回失败信息并附带异常消息
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 校验支付密码是否正确。
     * 该方法通过解析传入的token获取用户信息，并验证支付密码的正确性。
     *
     * @param paraMap 包含token等参数的映射表。
     * @return 返回校验结果，如果token无效，则返回失败响应。
     */
    @Override
    public ResponseResult checkPayPassword(Map<String, String> paraMap) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));

        // 检查用户信息是否有效，如果无效则返回错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        // 将用户ID添加到参数映射表中，用于后续的支付密码验证
        paraMap.put("userId", userInfo.getUserId());

        // 调用基本Mapper接口检查支付密码是否正确
        Integer flag = this.baseMapper.checkPayPassword(paraMap);

        // 返回支付密码验证的结果
        return ResponseResult.success(flag);
    }

    /**
     * 更新支付密码的方法。
     *
     * @param map 包含更新支付密码所需信息的Map对象，其中必须包含token和passwd字段。
     * @return 返回操作结果，如果成功更新支付密码，则返回成功响应；否则，返回失败响应，包括错误代码。
     */
    @Override
    public ResponseResult updatePaymentPassword(Map<String, Object> map) {
        // 通过token获取用户信息，用于验证token的有效性和后续的用户身份识别。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
        // 如果用户信息为空，说明token无效或已过期，返回相应的错误响应。
        if (null == userInfo){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        // 使用BCryptPasswordEncoder对新输入的支付密码进行加密处理。
        String passwd = PasswordUtils.BCryptPasswordEncoder((String) map.get("passwd"));
        // 更新用户的支付密码。使用LambdaUpdateWrapper来构造更新条件，设置新的支付密码，并根据用户ID进行更新。
        Integer isChangePasswd = this.baseMapper.update(null, new LambdaUpdateWrapper<PayUserBaseEntity>()
                .set(PayUserBaseEntity::getUserPayPasswd, passwd)
                .eq(PayUserBaseEntity::getUserId, userInfo.getUserId()));
        // 如果更新记录数大于0，说明支付密码更新成功，返回成功响应；否则，返回更新错误的错误响应。
        if (isChangePasswd > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);

    }

    @Override
    public ResponseResult plusMemberpay(Map<String, Object> map) {
        // 用于存储资产详情的映射
        Map<Object,Object> assetDetailMap = new HashMap<>();

        // 从传入的map中获取token字符串
        String token = (String) map.get("token");
        // 使用token解析出用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        // 从用户信息中获取用户ID
        String userId = userInfo.getUserId();
        // 从传入的map中获取密码字符串
        String passwd = String.valueOf(map.get("passwd"));

        // 生成6位随机数作为支付单号的一部分
        // 创建支付单号
        int i = PayToolUtil.buildRandom(6);
        // 使用SimpleDateFormat格式化当前日期，用于构建支付单号
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        // 构建支付单号前缀为"PLUS"，后接日期和随机数
        StringBuffer plusorder = new StringBuffer("PLUS");
        plusorder.append(format.format(new Date()));
        plusorder.append(i);

        // 1:余额支付 2:购物卡支付
        Integer type = Integer.parseInt(map.get("type") + "");

        try {
            // 1:余额支付 2:购物卡支付
            if (type == 1) {
                //输错次数
                String errorCount = RedisUtil.get(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId());
                if( null != errorCount && errorSum.equals(Integer.valueOf(errorCount))){
                    return ResponseResult.fail(BizCodeEnume.PAY_PASSWORD_ERROR.getCode(),BizCodeEnume.PAY_PASSWORD_ERROR.getMsg());
                }
                // 验证支付密码
                // 验证支付密码
                Integer isExist = payCommonUtils.getPwdExist(passwd, userInfo).getCode();
                if (isExist == 1) {
                    //支付金额
                    String payPrice = String.valueOf(map.get("payPrice"));
                    BigDecimal amountActuallyPaids = new BigDecimal(payPrice);
                    // 查询账户余额
                    BigDecimal userBalanceMoney = payUserResourceMapper.queryBalanceMoney(userId);
                    // 判断实付金额与余额
                    if (userBalanceMoney.compareTo(amountActuallyPaids) == 1
                            || userBalanceMoney.compareTo(amountActuallyPaids) == 0) {
                        // 扣除购物金额
                        boolean updateMoney = payUserResourceMapper.updateMoney(userId, amountActuallyPaids);
                        if (updateMoney) {
                            assetDetailMap.put("assetDirection", "开通plus会员");
                            assetDetailMap.put("payType",1);
                            assetDetailMap.put("userId",userId);
                            assetDetailMap.put("detailType",9);
                            assetDetailMap.put("assetType",2);
                            assetDetailMap.put("assetAmount",amountActuallyPaids);
                            assetDetailMap.put("orderId",plusorder.toString());
                            payAssetDetailsMapper.insertDetail(assetDetailMap);

                            //插入plus会员信息
                            map.put("plusOrder",plusorder.toString());
                            usermsServiceClient.plusmemberopen(map);
                            return ResponseResult.success();

                        } else {
                            return ResponseResult.fail(0, "支付失败！");
                        }
                    } else {
                        return ResponseResult.fail(0, "账户余额不足，请选择其他支付方式!");
                    }
                } else {
                    //次数统计
                    Integer numCount = null;
                    if( null == errorCount ){
                        numCount = 1;
                        RedisUtil.set(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId(),numCount.toString(),60*60);
                    }else{
                        String s1 = RedisUtil.get(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId().toString());
                        numCount = Integer.valueOf(s1) + 1;
                        RedisUtil.set(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId(),numCount.toString(),60*60);
                    }

                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),"支付密码不正确，您还有"+(errorSum - numCount)+"次输入机会！");
                }

            } else if (type == 2) {
                // 购物卡支付
                //输错次数
                String errorCount = RedisUtil.get(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId());
                if( null != errorCount && errorSum.equals(Integer.valueOf(errorCount))){
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),"支付密码不正确，您已经尝试3次，支付密码将锁定1小时，或找回密码后重试！");
                }
                Integer isExist = payCommonUtils.getPwdExist(passwd, userInfo).getCode();
                if (isExist == 1) {

                    //支付金额
                    String payPrice = String.valueOf(map.get("payPrice"));
                    BigDecimal amountActuallyPaids = new BigDecimal(payPrice);
                    // 查询账户购物卡余额
                    BigDecimal userGiftCardMoney = payUserResourceMapper.queryGiftCardMoney(userId);

                    // 判断实付金额与余额
                    if (userGiftCardMoney.compareTo(amountActuallyPaids) == 1
                            || userGiftCardMoney.compareTo(amountActuallyPaids) == 0) {
                        // 扣除购物金额
                        boolean updateMoney = payUserResourceMapper.updateGiftCardMoney(userId, amountActuallyPaids);
                        if (updateMoney) {
                            assetDetailMap.put("assetDirection", "开通plus会员");
                            assetDetailMap.put("payType",2);
                            assetDetailMap.put("userId",userId);
                            assetDetailMap.put("detailType",9);
                            assetDetailMap.put("assetType",2);
                            assetDetailMap.put("assetAmount",amountActuallyPaids);
                            assetDetailMap.put("orderId",plusorder.toString());
                            payAssetDetailsMapper.insertDetail(assetDetailMap);

                            //插入plus会员信息
                            map.put("plusOrder",plusorder.toString());
                            usermsServiceClient.plusmemberopen(map);
                            return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
                        } else {
                            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),"支付失败！");
                        }
                    } else {
                        return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),"账户余额不足，请选择其他支付方式!");
                    }
                } else {
                    //次数统计
                    Integer numCount = null;
                    if( null == errorCount ){
                        numCount = 1;
                        RedisUtil.set(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId(),numCount.toString(),60*60);
                    }else{
                        String s1 = RedisUtil.get(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId());
                        numCount = Integer.valueOf(s1) + 1;
                        RedisUtil.set(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId(),numCount.toString(),60*60);
                    }
                    return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),"支付密码不正确，您还有"+(errorSum - numCount)+"次输入机会！");
                }
            }
        } catch (Exception e) {
            return ResponseResult.fail(e.getMessage());
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }
}




