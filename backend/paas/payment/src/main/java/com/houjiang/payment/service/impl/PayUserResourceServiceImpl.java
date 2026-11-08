package com.houjiang.payment.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.payment.CardMemberservicesVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.CardMemberDetailDTO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;
import com.houjiang.payment.entity.PayTransferMoneyEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.BaseServiceClient;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.PayUserResourceService;
import com.houjiang.payment.vo.AccountBalanceDisplayVO;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Amos
 */
@Service
public class PayUserResourceServiceImpl extends ServiceImpl<PayUserResourceMapper, PayUserResourceEntity>
    implements PayUserResourceService{

    @Autowired
    private UsermsServiceClient usermsServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Resource
    private PayUserBaseMapper payUserBaseMapper;
    @Resource
    private PayConsumeWithdrawMapper payConsumeWithdrawMapper;
    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;
    @Resource
    private PayAssetDetailsMapper payAssetDetailsMapper;
    @Resource
    private PayTransferMoneyMapper payTransferMoneyMapper;
    @Resource
    private PayConsumeDepositMapper payConsumeDepositMapper;

    final Logger log = LoggerFactory.getLogger(PayUserResourceServiceImpl.class);
    /**
     * 根据用户token查询并展示用户账户余额信息。
     *
     * @param map 包含用户token等信息的映射表。
     * @return 返回用户的账户余额展示信息，如果查询失败或无信息，则返回错误结果。
     */
    @Override
    public ResponseResult balanceDisplay(Map<String, Object> map) {
        try {
            // 从请求参数中获取用户token
            String token = map.get("token").toString();
            // 根据token解析并获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);

            // 打印用户信息，用于调试
            System.out.println(userInfo);
            // 根据用户ID查询账户余额信息
            // 查询账户余额
            AccountBalanceDisplayVO acd = this.baseMapper.queryAccountBalance(userInfo.getUserId());
            // 如果账户余额信息不存在，则返回失败结果
            if (acd == null) {
                return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
            } else {
                // 查询用户积分
                Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userInfo.getUserId());
                // 查询用户冻结积分
                Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userInfo.getUserId());
                // 计算可用积分
                userIntegral = userIntegral - frozenIntegra;
                // 确保可用积分不为负
                if(userIntegral < 0){
                    userIntegral = 0;
                }
                // 更新账户余额中的用户积分和冻结积分信息
                acd.setUserIntegral(userIntegral);
                acd.setFrozenIntegra(frozenIntegra);
                // 计算用户的总资产
                // 用户余额
                BigDecimal userbalance = acd.getUserMoney();
                // 用户充值卡金额
                BigDecimal userRechargeCard = acd.getUserRechargeCard();
                // 用户总资产
                BigDecimal totalUserAssets = userbalance.add(userRechargeCard);
                // 设置用户的总资产信息
                acd.setTotalUserAssets(totalUserAssets);
                // 返回成功结果，包含用户的账户余额信息
                return ResponseResult.success(acd);
            }
        } catch (Exception e) {
            // 如果查询过程中发生异常，则返回失败结果，包含异常信息
            return ResponseResult.fail(e.getMessage());
        }
    }

    @Override
    public ResponseResult addMoney(Map<String, Object> map) {
        // 根据卖家ID查询支付用户资源实体
        PayUserResourceEntity entity = this.baseMapper.selectById(map.get("sellerId").toString());

        // 如果实体存在
        if (null != entity) {
            // 更新用户冻结资金，如果money参数不为空
            int isUpdateSuccess = this.baseMapper.update(null, new LambdaUpdateWrapper<PayUserResourceEntity>()
                    .set(null != map.get("money"), PayUserResourceEntity::getUserMoneyFrozen,
                            entity.getUserMoneyFrozen().add(BigDecimal.valueOf(Double.parseDouble(map.get("money").toString()))))
                    .eq(PayUserResourceEntity::getUserId, map.get("sellerId").toString()));
            // 如果更新成功，返回成功响应
            if (isUpdateSuccess == CommonStatusEnum.SUCCESS.getCode()) {
                return ResponseResult.success();
            } else {
                // 如果更新失败，返回失败响应
                return ResponseResult.fail();
            }
        } else {
            // 如果实体不存在，返回成功响应
            return ResponseResult.success();
        }

    }

    @Override
    @Transactional
    public ResponseResult withdrawal(Map<Object, Object> map, HttpServletRequest request) {
        String token = (String) map.get("token");
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            try {
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                // 发送人id
                String userId = ui.getUserId();
                String userName = ui.getUserName();
                Integer judge = ui.getSellerIsAdmin();
                // 收款方
                String payee = (String) map.get("payee");
                // 银行名称
                String bankName = (String) map.get("bankName");
                // 银行卡号
                String bankNum = (String) map.get("bankNum");
                // 提现金额
                String money = (String) map.get("money");
                /** 提现说明 */
                @SuppressWarnings("unused")
                String withdrawalInstructions = (String) map.get("withdrawalInstructions");
                // 支付密码
                String passwd = (String) map.get("passwd");
                String recordDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
                String recordYear = DateUtils.getYear();
                String recordMonth = DateUtils.getMonth();
                String recordDay = DateUtils.getDay();
                String recordTitle = "提现";
                String recordDesc = "普通提现";
                String recordTime = DateUtils.getNowDateToString();
                // 交易类型
                Integer tradeTypeId = 4;
                Integer userType = 1;
                Integer recordStatus = 8;

                if (StringUtils.isBlank(money) || new BigDecimal(money).compareTo(new BigDecimal(0)) <= 0) {
                    return ResponseResult.fail(BizCodeEnume.INCORRECT_FORMAT_OF_WITHDRAWAL_AMOUNT);
                }
                // 普通用户提现
                // 验证支付密码
                String password = payUserBaseMapper.isPasswd(userId);
                if (PasswordUtils.BCryptPasswordMatches(passwd, password)) {
                    // 查询账户余额
                    PayUserResourceEntity payUserResourceEntity = this.baseMapper.selectById(userId);
                    BigDecimal accountOfBalance = payUserResourceEntity.getUserMoney();
                    // 类型转换，用于比较
                    BigDecimal userMoney = new BigDecimal(money);
                    if (userMoney.compareTo(accountOfBalance) > 0) {
                        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE.getCode(),
                                BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE.getMsg());
                    } else {
                        // 提现订单号
                        String orderId = "W" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
                        Date createTime = DateUtils.getNowDate();
                        String con = "普通提现";
                        Map<String, Object> mapWithd = new HashMap<>();
                        mapWithd.put("userId", userId);
                        mapWithd.put("userName", userName);
                        mapWithd.put("orderId", orderId);
                        mapWithd.put("money", money);
                        mapWithd.put("createTime", createTime);
                        mapWithd.put("con", con);
                        mapWithd.put("bankName", bankName);
                        mapWithd.put("bankNum", bankNum);
                        mapWithd.put("payee", payee);
                        //提现申请记录
                        boolean isInsertSuccess = payConsumeWithdrawMapper.insertConsumeWithdraw(mapWithd);
                        Map<String, Object> mapRecord = getStringObjectMap(userId, userName, money, recordTime, orderId, recordDate,
                                recordYear, recordMonth, recordDay, recordTitle, recordDesc, tradeTypeId, userType, recordStatus,
                                orderId, null, 0);
                        //用户提现记录
                        boolean isInsertRecordSuccess = payConsumeRecordMapper.insertConsumeRecord(mapRecord);
                        // 用户提现金额，扣除金额
                        boolean isUpdateUserResource = this.baseMapper.updateUserResource(userId, money);
                        if (isInsertSuccess && isInsertRecordSuccess && isUpdateUserResource) {

                            //插入我的余额明细
                            Map<Object, Object> assetDetailMap = new HashMap<>();
                            assetDetailMap.put("assetDirection", "银行账号:" + bankNum);
                            assetDetailMap.put("payType", 1);
                            assetDetailMap.put("userId", userId);
                            assetDetailMap.put("detailType", 4);
                            assetDetailMap.put("assetType", 2);
                            assetDetailMap.put("assetAmount", new BigDecimal(money));
                            assetDetailMap.put("orderId", orderId);
                            assetDetailMap.put("detailComment", withdrawalInstructions);
                            payAssetDetailsMapper.insertDetail(assetDetailMap);

                            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.CASH_WITHDRAWAL_APPLICATION.getMsg());
                        } else {
                            return ResponseResult.fail(BizCodeEnume.WITHDRAWAL_FAILED);
                        }
                    }
                } else {
                    return ResponseResult.fail(BizCodeEnume.PAYMENT_PASSWORD_ERROR);
                }
            } catch (Exception e) {
                // 手动回滚事务
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    @Override
    @Transactional
    public ResponseResult transfer(Map<Object, Object> map) {
        Map<Object, Object> assetDetailMapToUser = new HashMap<>();
        Map<Object, Object> assetDetailMapFromUser = new HashMap<>();
        String token = (String) map.get("token");
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        if (ui != null) {
            try {
                // 发送人id
                String fromUserId = ui.getUserId();
                // 发起人姓名
                String fromUserName = ui.getUserName();
                // 收款人账号
                String payeeAccount = (String) map.get("payeeAccount");
                UserInfoVO rpcUserInfo = usermsServiceClient.getUserInfoByMobile(payeeAccount);
                // 接收人id
                String toUserId = rpcUserInfo.getUserId();
                // 接收人姓名
                String toUserName = rpcUserInfo.getUserName();
                // 转账金额
                String money = (String) map.get("money");
                // 付款说明
                String paymentExplain = (String) map.get("paymentExplain");
                // 图形验证码
                String picValidation = (String) map.get("picValidation");
                // 手机验证码
                String phoneValidation = (String) map.get("phoneValidation");
                // 支付密码
                String passwd = (String) map.get("passwd");
                // 获取存储的图形验证码
                String picCode = RedisUtil.get("verification_code_" + token);
                // 获取存储手机验证码
                String phoneCode = RedisUtil.get(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
                String orderId = "ZZ" + DateUtils.dateTimeNow() + RandomUtil.randomInt(1000, 9999);
                //收款人信息封装
                assetDetailMapToUser.put("assetDirection", fromUserName);
                assetDetailMapToUser.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
                assetDetailMapToUser.put("userId", toUserId);
                assetDetailMapToUser.put("detailType", CommonType.detailType.BALANCE_TRANSFER.getCode());
                assetDetailMapToUser.put("assetType", CommonType.assetType.INCOME.getCode());
                assetDetailMapToUser.put("assetAmount", money);
                assetDetailMapToUser.put("detailComment", paymentExplain);
                assetDetailMapToUser.put("orderId", orderId);

                //发起人信息封装
                assetDetailMapFromUser.put("assetDirection", toUserName);
                assetDetailMapFromUser.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
                assetDetailMapFromUser.put("userId", fromUserId);
                assetDetailMapFromUser.put("detailType",CommonType.detailType.BALANCE_TRANSFER.getCode());
                assetDetailMapFromUser.put("assetType", CommonType.assetType.EXPENDITURE.getCode());
                assetDetailMapFromUser.put("assetAmount", money);
                assetDetailMapFromUser.put("detailComment", paymentExplain);
                assetDetailMapFromUser.put("orderId", orderId);
                if (!Objects.equals(phoneCode, phoneValidation)) {
                    return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
                }
                // 验证支付密码
                PayUserResourceEntity resourceEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<PayUserResourceEntity>()
                        .eq(PayUserResourceEntity::getUserId,fromUserId));
                String password = payUserBaseMapper.isPasswd(fromUserId);
                if (password == null) {
                    return ResponseResult.fail(BizCodeEnume.PAYMENT_PASSWORD_IS_NULL);
                }
                if (PasswordUtils.BCryptPasswordMatches(passwd, password)) {
                    if(resourceEntity.getUserMoney().compareTo(new BigDecimal(money)) < 0){
                        return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE);
                    }
                    String sendTime = DateUtils.getNowDateToString();
                    Integer transferType = 1;
                    // 转账订单号
                    String transactionNumber = "T" + DateUtils.getTimeStamp()
                            + DateUtils.randomLengthByTwo();
                    Map<String, Object> mapTransfer = new HashMap<>();
                    mapTransfer.put("fromUserName", fromUserName);
                    mapTransfer.put("toUserName", toUserName);
                    mapTransfer.put("sendTime", sendTime);
                    mapTransfer.put("receiveTime", sendTime);
                    mapTransfer.put("money", money);
                    mapTransfer.put("paymentExplain", paymentExplain);
                    mapTransfer.put("transferType", transferType);
                    mapTransfer.put("transactionNumber", transactionNumber);
                    // 插值存库
                    boolean isInsertSuccess = payTransferMoneyMapper.insertTransfer(mapTransfer);
                    // 向record表插值
                    String recordDate = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-"
                            + DateUtils.getDay();
                    String recordYear = DateUtils.getYear();
                    String recordMonth = DateUtils.getMonth();
                    String recordDay = DateUtils.getDay();
                    String recordTitle = "转账";
                    String recordDesc = "转账";
                    // 交易类型
                    Integer tradeTypeId = 2;
                    // 2-付款方
                    Integer fromUserType = 2;
                    // 1-收款方
                    Integer toUserType = 1;
                    // 付款状态
                    Integer recordStatus = 8;
                    // 实际支付单号
                    String recordPayorder = transactionNumber;
                    String recordPaytime = DateUtils.getNowDateToString();
                    Integer fromPlusOrMinus = 0;
                    Integer toPlusOrMinus = 1;
                    if (isInsertSuccess) {
                        Map<String, Object> mapRecord = getStringObjectMap(fromUserId, fromUserName, money, sendTime,
                                transactionNumber, recordDate, recordYear, recordMonth, recordDay, recordTitle, recordDesc,
                                tradeTypeId, fromUserType, recordStatus, recordPayorder, recordPaytime, fromPlusOrMinus);
                        // 转账人记录
                        boolean isFromRecord = payConsumeRecordMapper.insertConsumeRecord(mapRecord);
                        Map<String, Object> mapToRecord = getStringObjectMap(toUserId, toUserName, money, sendTime,
                                transactionNumber, recordDate, recordYear, recordMonth, recordDay, recordTitle, recordDesc,
                                tradeTypeId, toUserType, recordStatus, recordPayorder, recordPaytime, toPlusOrMinus);
                        // 收款人
                        boolean isToRecord = payConsumeRecordMapper.insertConsumeRecord(mapToRecord);
                        if (isFromRecord && isToRecord) {
                            // 更新转帐方资产信息
                            boolean isUpdateFrom = this.baseMapper.updateUserResource(fromUserId, money);
                            //更新收款方资产信息
                            boolean isUpdateTo = this.baseMapper.updateToMoney(toUserId, money);
                            if (isUpdateFrom && isUpdateTo) {
                                // 更新交易单状态
                                payTransferMoneyMapper.update(null, new LambdaUpdateWrapper<PayTransferMoneyEntity>()
                                        .eq(PayTransferMoneyEntity::getTransactionNumber, transactionNumber)
                                        .set(PayTransferMoneyEntity::getTransferStatus, CommonType.transferStatus.RECEIVED.getCode()));
                                //更新转帐方交易明细
                                payConsumeRecordMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                                        .eq(PayConsumeRecordEntity::getUserId, fromUserId)
                                        .eq(PayConsumeRecordEntity::getOrderId, transactionNumber)
                                        .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CONFIRM_RECEIPT.getCode()));
                                //更新收款方交易明细
                                payConsumeRecordMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                                        .eq(PayConsumeRecordEntity::getUserId, toUserId)
                                        .eq(PayConsumeRecordEntity::getOrderId, transactionNumber)
                                        .set(PayConsumeRecordEntity::getRecordStatus, CommonType.recordStatus.CONFIRM_RECEIPT.getCode()));
                                payAssetDetailsMapper.insertDetail(assetDetailMapFromUser);
                                payAssetDetailsMapper.insertDetail(assetDetailMapToUser);

                                return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.TRANSFER_SUCCEEDED.getMsg());
                            } else {
                                // 手动回滚事务
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                return ResponseResult.fail(BizCodeEnume.ASSET_UPDATE_FAILED);
                            }
                        } else {
                            return ResponseResult.fail(BizCodeEnume.RECORD_INPUT_FAILED);
                        }
                    } else {
                        return ResponseResult.fail(BizCodeEnume.TRANSFER_FAILED);
                    }
                } else {
                    return ResponseResult.fail(BizCodeEnume.PAYMENT_PASSWORD_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult updateCardMemberDetailPort(CardMemberDetailDTO param) throws TransactionException {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        try {
            if (null != userInfo) {
                PayUserResourceEntity userMoney = this.baseMapper.selectOne(new LambdaQueryWrapper<PayUserResourceEntity>()
                                                .eq(PayUserResourceEntity::getUserId,param.getUserId()));
                BigDecimal money = new BigDecimal(0);
                Integer plusOrMinus = 0;
                Map<String, Object> assetDetailMap = new HashMap<>();
                assetDetailMap.put("assetDirection", "平台");
                assetDetailMap.put("payType", param.getVoucherType());
                assetDetailMap.put("userId", param.getUserId());
                //1是余额 2 是充值卡
                Integer depositType = 1;
                Map<String, Object> comRecord = new HashMap<String, Object>();
                //修改的金额
                BigDecimal updateMone = new BigDecimal(param.getVoucherMoney().replaceAll("-", ""));
                if (param.getVoucherType() == 1) {
                    // 修改账户余额
                    if ((param.getVoucherMoney()).contains("-")) {
                        //扣除余额
                        money = userMoney.getUserMoney().subtract(updateMone);

                        //余额扣款
                        assetDetailMap.put("detailType", 8);
                        assetDetailMap.put("assetType", 2);
                        assetDetailMap.put("assetAmount", updateMone);

                    } else if (param.getVoucherMoney().contains("+")) {

                        money = userMoney.getUserMoney().add(updateMone);
                        plusOrMinus = 1;
                        //余额充值
                        assetDetailMap.put("detailType", 1);
                        assetDetailMap.put("assetType", 1);
                        assetDetailMap.put("assetAmount", Double.parseDouble(param.getVoucherMoney()));

                    } else {
                        return ResponseResult.fail(BizCodeEnume.UPDATE_USER_MONEY_ERROP.getCode(), BizCodeEnume.UPDATE_USER_MONEY_ERROP.getMsg());
                    }
                    if (money.compareTo(BigDecimal.ZERO) < 0) {
                        return ResponseResult.fail(BizCodeEnume.UPDATE_USER_MONEY_ERROP_TOW.getCode(), BizCodeEnume.UPDATE_USER_MONEY_ERROP_TOW.getMsg());
                    }
                    this.baseMapper.update(null,new LambdaUpdateWrapper<PayUserResourceEntity>()
                                   .eq(PayUserResourceEntity::getUserId, param.getUserId())
                                   .set(PayUserResourceEntity::getUserMoney,money));
                    //1是余额 2 是充值卡
                    depositType = 1;
                }
                if (param.getVoucherType() == 2) {
                    // 修充值卡余额
                    log.info("userMoney" + userMoney.getUserMoney());
                    if ((param.getVoucherMoney()).indexOf("-") >= 0) {
                        money = userMoney.getUserRechargeCard().subtract(updateMone);
                        //充值卡扣款
                        //余额扣款
                        assetDetailMap.put("detailType", CommonType.detailType.BALANCE_DEDUCTION.getCode());
                        assetDetailMap.put("assetType", CommonType.assetType.EXPENDITURE.getCode());
                        assetDetailMap.put("assetAmount", Double.parseDouble(param.getVoucherMoney().replaceAll("-", "")));

                    } else if (param.getVoucherMoney().indexOf("+") >= 0) {
                        money = userMoney.getUserRechargeCard().add(updateMone);
                        plusOrMinus = 1;
                        //充值卡充值
                        //余额充值
                        assetDetailMap.put("detailType", CommonType.detailType.BALANCE_RECHARGE.getCode());
                        assetDetailMap.put("assetType", CommonType.assetType.INCOME.getCode());
                        assetDetailMap.put("assetAmount", Double.parseDouble(param.getVoucherMoney()));

                    } else {
                        return ResponseResult.fail(BizCodeEnume.UPDATE_USER_MONEY_ERROP.getCode(), BizCodeEnume.UPDATE_USER_MONEY_ERROP.getMsg());
                    }
                    if (money.compareTo(BigDecimal.ZERO) < 0) {
                        return ResponseResult.fail(BizCodeEnume.UPDATE_USER_MONEY_ERROP_TOW.getCode(), BizCodeEnume.UPDATE_USER_MONEY_ERROP_TOW.getMsg());
                    }
                    this.baseMapper.update(null,new LambdaUpdateWrapper<PayUserResourceEntity>()
                                   .eq(PayUserResourceEntity::getUserId, param.getUserId())
                                   .set(PayUserResourceEntity::getUserRechargeCard,money));
                    //1是余额 2 是充值卡
                    depositType = 2;
                }
                // 充值订单号
                String depositTradeNo = "R" + DateUtils.getTimeStamp();
                comRecord.put("depositTradeNo", depositTradeNo);
                comRecord.put("depositTotalFee", param.getVoucherMoney());
                comRecord.put("depositPayChannel", 0);
                comRecord.put("depositTradeStatus", 6);
                comRecord.put("depositIsRechargePackage", 0);
                comRecord.put("depositReturnTradeNo", depositTradeNo);
                comRecord.put("userNickname", param.getUserName());
                comRecord.put("recordDate", DateUtils.getNowDateToString());
                comRecord.put("year", DateUtils.getYear());
                comRecord.put("month", DateUtils.getMonth());
                comRecord.put("day", DateUtils.getDay());
                comRecord.put("plusOrMinus", plusOrMinus);
                comRecord.put("recordTitle", "充值");
                comRecord.put("record_desc", param.getRecordDesc());
                comRecord.put("tradeTypeId", 3);
                comRecord.put("userType", 1);
                comRecord.put("depositType", depositType);
                comRecord.put("recordWithdraw", 0);
                comRecord.put("osCommisAmount", 0);
                comRecord.put("osCommisReturnAmount", 0);
                comRecord.put("money", money);
                comRecord.put("userId", param.getUserId());
                comRecord.put("operator", userInfo.getUserAccount());
                comRecord.put("orderOperator", userInfo.getUserRealname());
                assetDetailMap.put("detailComment", param.getRecordDesc());
                assetDetailMap.put("orderId", depositTradeNo);
                payConsumeDepositMapper.insertConsumeDepositport(comRecord);
                payConsumeRecordMapper.insertConsumeRecordport(comRecord);
                payAssetDetailsMapper.insertUserMoneyDetail(assetDetailMap);
                return ResponseResult.success();
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        } catch (Exception e) {
            // 回滚
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(e.getMessage());
        }
    }

    @Override
    public ResponseResult query(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 根据用户信息进行分页查询
            // 分页查询数据
            IPage<CardMemberservicesVO> page = this.baseMapper.query(new Query<CardMemberservicesVO>().getPage(map), map);
            // 获取脱敏开关的配置值
            String value = baseServiceClient.selectConfigValueByKey(CommonType.configKey.DESENSITIZATION_SWITCH.getDesc());
            // 如果脱敏开关打开，并且查询结果不为空，则对查询结果进行脱敏处理
            if (StrUtil.isNotEmpty(value) && value.equals(CommonType.commonStatus.YES.getCode().toString()) && !page.getRecords().isEmpty()) {
                for (CardMemberservicesVO res : page.getRecords()) {
                    BeanUtils.copyProperties(DesensitizationUtil.Desensitization(res, CardMemberservicesVO.class), res);
                }
            }
            // 返回查询结果
            return ResponseResult.success(PageUtils.getPage(page));
        }
        // 如果token非法，则返回失败响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);

    }


    /**
     * 转换
     *
     * @param toUserId
     * @param toUserName
     * @param money
     * @param sendTime
     * @param transactionNumber
     * @param recordDate
     * @param recordYear
     * @param recordMonth
     * @param recordDay
     * @param recordTitle
     * @param recordDesc
     * @param tradeTypeId
     * @param toUserType
     * @param recordStatus
     * @param recordPayorder
     * @param recordPaytime
     * @param toPlusOrMinus
     * @return
     */
    private Map<String, Object> getStringObjectMap(String toUserId, String toUserName, String money, String sendTime, String transactionNumber, String recordDate, String recordYear, String recordMonth, String recordDay, String recordTitle, String recordDesc, Integer tradeTypeId, Integer toUserType, Integer recordStatus, String recordPayorder, String recordPaytime, Integer toPlusOrMinus) {
        Map<String, Object> mapToRecord = new HashMap<>();

        // 插入订单号，用于标识该交易。
        mapToRecord.put("orderId", transactionNumber);
        // 插入用户ID，用于标识交易的接收方。
        mapToRecord.put("userId", toUserId);
        // 插入用户名，用于更直观地显示交易接收方的信息。
        mapToRecord.put("userName", toUserName);
        // 插入交易金额，使用BigDecimal确保精确的货币计算。
        mapToRecord.put("fee", new BigDecimal(money));
        // 插入交易记录日期，用于跟踪交易发生的时间。
        mapToRecord.put("date", recordDate);
        // 插入交易记录的年份，用于按年份筛选交易记录。
        mapToRecord.put("year", recordYear);
        // 插入交易记录的月份，用于按月份筛选交易记录。
        mapToRecord.put("month", recordMonth);
        // 插入交易记录的日期，用于按日期筛选交易记录。
        mapToRecord.put("day", recordDay);
        // 插入交易记录的标题，用于简要描述交易的性质。
        mapToRecord.put("title", recordTitle);
        // 插入交易记录的描述，用于详细描述交易的情况。
        mapToRecord.put("desc", recordDesc);
        // 插入交易的发送时间，用于记录交易发起的时间。
        mapToRecord.put("depositGmtCreate", sendTime);
        // 插入交易类型ID，用于分类交易类型。
        mapToRecord.put("typeId", tradeTypeId);
        // 插入用户的类型，用于区分不同类型的用户。
        mapToRecord.put("userType", toUserType);
        // 插入交易记录的状态，用于表示交易的当前状态。
        mapToRecord.put("recordStatus", recordStatus);
        // 插入交易记录的支付单号，用于跟踪支付订单。
        mapToRecord.put("recordPayorder", recordPayorder);
        // 插入交易的支付时间，用于记录支付的具体时间。
        mapToRecord.put("cardFetchTime", recordPaytime);
        // 插入交易的正负标识，用于区分收入和支出。
        mapToRecord.put("plusOrMinus", toPlusOrMinus);

        // 返回包含所有交易记录信息的映射。
        return mapToRecord;

    }
    /**
     * 更新用户余额
     * 该方法通过用户的ID来更新用户的余额，通常用于充值操作
     * 它直接调用baseMapper的updateUserBalance方法来执行更新操作，并返回一个成功的结果响应
     *
     * @param userId 用户的唯一标识符，用于定位哪个用户的余额需要更新
     * @param rechargeAmount 需要增加到用户余额上的金额，表示充值金额
     * @return 返回一个ResponseResult对象
     */
    @Override
    public ResponseResult updateUserBalance(String userId, BigDecimal rechargeAmount) {
        this.baseMapper.updateDeductUserMoney(userId,rechargeAmount);
        return ResponseResult.success();
    }

    /**
     * 查询账户余额
     *
     * @param userId 用户ID，用于查询账户余额
     * @return 返回账户余额信息
     */
    @Override
    public String queryAccountBalance(String userId) {
        String result = this.baseMapper.queryAccountBalanceFeign(userId);
        return result;
    }

    /**
     * 更新银行卡会员详情端口
     *
     * @param manageMoney 包含更新信息的映射，用于指定要更新的字段和值
     */
    @Override
    public void updateCardMemberDetailPortFeign(Map<String, Object> manageMoney) {
        this.baseMapper.updateCardMemberDetailPortFeign(manageMoney);
    }

    /**
     * 更新银行卡会员详情端口2
     *
     * @param manageMoney 包含更新信息的映射，用于指定要更新的字段和值
     * @return 返回更新操作的结果，通常表示受影响的行数
     */
    @Override
    public Integer updateCardMemberDetailPort2(Map<String, Object> manageMoney) {
        Integer integer = this.baseMapper.updateCardMemberDetailPort2(manageMoney);
        return integer;
    }
}




