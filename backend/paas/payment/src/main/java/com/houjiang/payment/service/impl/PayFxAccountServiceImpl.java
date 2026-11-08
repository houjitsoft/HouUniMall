package com.houjiang.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.vo.user.UserVkVO;
import com.houjiang.payment.dto.FXCommonDTO;
import com.houjiang.payment.dto.WithdrawRequestDTO;
import com.houjiang.payment.entity.PayFxAccountEntity;
import com.houjiang.payment.entity.PayFxRecordEntity;
import com.houjiang.payment.entity.PayFxTradeEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.PayFxAccountService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayFxAccountServiceImpl extends ServiceImpl<PayFxAccountMapper, PayFxAccountEntity>
        implements PayFxAccountService {

    private static final Logger logger = LoggerFactory.getLogger(PayFxAccountServiceImpl.class);

    /** 商户证书编号 */
    @Value("${wechatPayserialNo}")
    private String wechatPayserialNum;

    /** 商户证书路径（在你本机测试时放你本机路径中的就可以） */
    @Value("${privatekeypath}")
    private String wechatkeyPem;

    private static String wechatPayserialNo;

    private static String privatekeypath;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private PayUserResourceMapper payUserResourceMapper;
    @Autowired
    private PayAssetDetailsMapper payAssetDetailsMapper;

    @PostConstruct
    public void getWechatPayserialNo() {
        wechatPayserialNo = this.wechatPayserialNum;
    }

    @PostConstruct
    public void getPrivatekeypath() {
        privatekeypath = this.wechatkeyPem;
    }


    @Autowired
    private PayFxRecordMapper payFxRecordMapper;

    @Autowired
    private PayFxTradeMapper payFxTradeMapper;

    @Autowired
    private PayConfigMapper payConfigMapper;

    @Transactional
    @Override
    public ResponseResult withdraw(WithdrawRequestDTO param, HttpServletRequest request) {
        logger.info("GameGroupWithdrawServiceImpl withdraw{}", JSON.toJSONString(param));
        ResponseResult resultResp;
        Map<String, Object> map = new HashMap<>();
        Boolean tiXianStart = false;
        String resStr = null;
        //1.获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        if (null != userInfo) {
            // 2.防重复请求
            Boolean flag = this.verifyWithdraw(userInfo);
            if (flag) {
                try {
                    // 3.验证用户提现金额
                    resultResp = this.checkWithdrawAmount(param, userInfo);
                    if (resultResp.getCode() == 1) {
                        UserVkVO payFxAccount = JSON.parseObject(JSON.toJSONString(resultResp.getData()), UserVkVO.class);
                        // 4.生成充值交易记录
                        String recordId = "R" + DateUtils.getTimeStamp() + RandomStringUtils.randomNumeric(8);
                        // 5.生成第三方请求记录
                        String thirdPayId = "T" + DateUtils.getTimeStamp() + RandomStringUtils.randomNumeric(8);
                        // 6.初始化请求参数
                        this.initWithdrawRequest(payFxAccount, param, thirdPayId, recordId);
                        // 7.添加交易记录数据
                        this.insertPayRecord(param, userInfo);
                        // 8.添加第三方请求数据
                        this.insertThirdPayInfo(param, userInfo);
                        // 9.请求提现
                        if (param.getPayCode().equals(CommonType.payType.BALANCE.getCode())) {
                            // 余额
                            resultResp = this.withdrawNotify(recordId, thirdPayId, "", userInfo.getUserId(), payFxAccount, param);
                            return resultResp;
                        } else {
                            // 微信
                            //resultResp = wxTransferServiceImpl.wxTransfer(thirdPayId, withdrawRequest.getWxOpenId(), withdrawRequest.getWxUserName(), withdrawRequest.getTotalMoney(), withdrawRequest.getPlatFrom(), request);
                            Map<String, Object> tixianMap = new HashMap<>();
                            tixianMap.put("transferAmount", param.getTotalMoney().multiply(new BigDecimal(100)));
                            tixianMap.put("openId", param.getWxOpenId());
                            tixianMap.put("desc", "分销提现");
                            map = weixinTransferBat(tixianMap);
                            if (null != map) {
                                tiXianStart = (Boolean) map.get("start");
                                resStr = (String) map.get("resStr");
                            }
                            // 11.提现成功更新提现交易记录
                            if (tiXianStart) {
                                String thirdTradeNo = (String) map.get("out_batch_no");
                                resultResp = this.withdrawNotify(recordId, thirdPayId, thirdTradeNo, userInfo.getUserId(), payFxAccount, param);
                                return resultResp;
                            } else {
                                // 12.提现失败保存第三方请求失败码
                                if (null != resStr) {
                                    this.updateThirdTrade(thirdPayId, recordId, userInfo.getUserId(), param.getTotalMoney(), payFxAccount, resultResp);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return ResponseResult.fail(0, "提现异常请联系客服");
                } finally {
                    RedisUtil.remove(HouConfig.HOU_FX_WITHDRAW_ + userInfo.getUserId());
                }
            } else {
                return ResponseResult.fail(0, "您已申请提现,请勿重复操作");
            }
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        return resultResp;
    }

    public void updateThirdTrade(String thirdPayId, String recordId, String userId, BigDecimal totalMoney, UserVkVO payFxAccount, ResponseResult thirdErrorData) {
        PayFxTradeEntity record = new PayFxTradeEntity(null, null);
        // 第三方交易请求单号
        record.setThirdPayId(thirdPayId);
        // 错误码
        record.setErrorCode(String.valueOf(thirdErrorData.getCode()));
        // 错误说明
        record.setErrorMsg(thirdErrorData.getMessage());
        // 回调时间
        record.setNotifyTime(DateUtils.getNowDate());
        // 更新时间
        record.setUpdateTime(DateUtils.getNowDate());

        if (thirdErrorData.getCode() != CommonType.commonStatus.NO.getCode()) {
            // 更新用户佣金提现余额
            UserVkVO userAccountAccount = usermsServiceClient.queryUserAccount(userId);
            BigDecimal pendingWithdrawnCommission = userAccountAccount.getPendingWithdrawnCommission().subtract(totalMoney).setScale(2, RoundingMode.HALF_UP);
            BigDecimal withdrawnCommission = userAccountAccount.getWithdrawnCommission().add(totalMoney).setScale(2, RoundingMode.HALF_UP);
            PayFxAccountEntity updateUserAccount = new PayFxAccountEntity(userId, withdrawnCommission, pendingWithdrawnCommission);
            Map<String, String> paraMap=new HashMap<>();
            paraMap.put("vkAccount",updateUserAccount.getVkAccount().toString());
            paraMap.put("withdrawFreezeAccount",updateUserAccount.getWithdrawFreezeAccount().toString());
            usermsServiceClient.subUserAccount(paraMap);
            // 更新交易金额更新记录
            PayFxRecordEntity fxRecord = new PayFxRecordEntity(recordId, payFxAccount.getPendingWithdrawnCommission(), payFxAccount.getPendingWithdrawnCommission().subtract(totalMoney));
            payFxRecordMapper.updateById(fxRecord);
        } else {
            //1. 如果是其他错误  则更新状态为提现失败
            record.setTradeStatus(2);
            // 2.更新佣金交易记录状态为提现失败
            PayFxRecordEntity fxRecord = new PayFxRecordEntity(recordId, CommonType.recordStatus2.WITHDRAW_CANCEL.getCode(), CommonType.recordStatus2.WITHDRAW_CANCEL.getDesc());
            payFxRecordMapper.updateById(fxRecord);
        }
        payFxTradeMapper.updateById(record);
    }


    @Transactional(rollbackFor = Exception.class)
    public ResponseResult withdrawNotify(String recordId, String thirdPayId, String thirdTradeNo, String userId, UserVkVO payFxAccount, WithdrawRequestDTO withdrawRequest) {
        try {
            // 1.更新用户佣金提现余额
            UserVkVO userAccountAccount = usermsServiceClient.queryUserAccount(userId);
            BigDecimal pendingWithdrawnCommission = userAccountAccount.getPendingWithdrawnCommission().subtract(withdrawRequest.getTotalMoney()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal withdrawnCommission = userAccountAccount.getWithdrawnCommission().add(withdrawRequest.getTotalMoney()).setScale(2, RoundingMode.HALF_UP);
            PayFxAccountEntity updateUserAccount = new PayFxAccountEntity(userId, withdrawnCommission, pendingWithdrawnCommission);
            Map<String, String> paraMap=new HashMap<>();
            paraMap.put("vkAccount",updateUserAccount.getVkAccount().toString());
            paraMap.put("userId",userId);
            paraMap.put("withdrawFreezeAccount",updateUserAccount.getWithdrawFreezeAccount().toString());
            int result= usermsServiceClient.subUserAccount(paraMap);
            // 2.更新交易记录
            PayFxRecordEntity gameGroupRecord = new PayFxRecordEntity(recordId, payFxAccount.getPendingWithdrawnCommission(), payFxAccount.getPendingWithdrawnCommission().subtract(withdrawRequest.getTotalMoney()), CommonType.recordStatus2.DEAL_FINISHED.getCode(), "提现成功", DateUtils.getNowDate());
            int recordResult = payFxRecordMapper.updateById(gameGroupRecord);
            // 3.更新第三方提现记录
            PayFxTradeEntity record = new PayFxTradeEntity();
            // 第三方交易请求单号
            record.setThirdPayId(thirdPayId);
            // 第三方交易单号
            record.setThirdTradeNo(thirdTradeNo);
            // 支付状态
            record.setTradeStatus(CommonType.commonStatus.YES.getCode());
            // 回调时间
            record.setNotifyTime(DateUtils.getNowDate());
            // 更新时间
            record.setUpdateTime(DateUtils.getNowDate());
            // 交易单号
            record.setRecordId(recordId);
            int tradeResult = payFxTradeMapper.updateById(record);
            if (StringUtils.isEmpty(thirdTradeNo)) {
                payUserResourceMapper.update(null, new UpdateWrapper<PayUserResourceEntity>()
                        .setSql("user_money = user_money + " + withdrawRequest.getTotalMoney())
                        .eq("user_id", userId));
                Map<Object, Object> assetDetailMap = new HashMap<>();
                assetDetailMap.put("assetDirection", "分销佣金提现余额");
                assetDetailMap.put("payType", 1);
                assetDetailMap.put("userId", userId);
                assetDetailMap.put("detailType", 13);
                assetDetailMap.put("assetType", 1);
                assetDetailMap.put("assetAmount", withdrawRequest.getTotalMoney());
                assetDetailMap.put("orderId", recordId);
                payAssetDetailsMapper.insertDetail(assetDetailMap);
            }
            if (result == recordResult && result == tradeResult && result == 1) {
                return ResponseResult.success("提现成功");
            } else {
                // 操作失败手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseResult.fail(0, "更新用户账户失败");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logger.error(e.getMessage());
            // 操作失败手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseResult.fail(0, "更新用户账户失败");
        }
    }

    /**
     * 验证用户当前是否已存在提现请求
     * 使用Redis的锁机制来检查用户是否已经有提现请求正在处理中。如果存在锁，则表示有请求正在处理，应该阻止新的请求。
     * 这种机制可以防止并发问题，确保同一时间只有一个提现请求被处理。
     *
     * @param userInfo 用户信息，包含用户ID，用于生成Redis锁的键。
     * @return 如果存在锁（即用户已有提现请求在处理中），返回true；否则返回false，表示可以处理新的提现请求。
     */
    private Boolean verifyWithdraw(UserInfoVO userInfo) {
        // 根据用户ID生成Redis锁的键，确保每个用户的提现请求互不干扰。
        String key = HouConfig.HOU_FX_WITHDRAW_ + userInfo.getUserId();
        // 尝试获取Redis锁，如果获取成功，则表示没有其他提现请求正在处理，可以继续；否则，表示有请求正在处理，应该返回false。
        return RedisUtil.getLock(key, RedisUtil.EXRP_DAY);
    }


    public Map<String, Object> weixinTransferBat(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>();
        if (null != map && null != map.get("openId") && null != map.get("transferAmount")) {
            //查询支付配置信息
            selectYFPayConfig();
            //商户号
            String mchid = WXPayConfig.MCH_ID;
            //申请商户号的appid或商户号绑定的appid（企业号corpid即为此appid）
            String appId = WXPayConfig.Applet_APP_ID;
            //用户在直连商户应用下的用户标示
            String openId = map.get("openId").toString();
            System.out.println(WXPayConfig.Applet_APP_ID);

            Map<String, Object> postMap = new HashMap<String, Object>();

            //商家批次单号 长度 1~32
            String outNo = IdUtil.getSnowflake(0, 0).nextIdStr();

            postMap.put("appid", appId);
            postMap.put("out_batch_no", outNo);
            //该笔批量转账的名称
            postMap.put("batch_name", map.get("desc").toString());
            //转账说明，UTF8编码，最多允许32个字符
            postMap.put("batch_remark", map.get("desc").toString());
            //转账金额单位为“分”。 总金额
            postMap.put("total_amount", map.get("transferAmount"));
            //。转账总笔数
            postMap.put("total_num", 1);


            List<Map> list = new ArrayList<>();
            Map<String, Object> subMap = new HashMap<>(4);
            //商家明细单号
            subMap.put("out_detail_no", outNo);
            //转账金额
            subMap.put("transfer_amount", map.get("transferAmount"));
            //转账备注
            subMap.put("transfer_remark", map.get("desc").toString());
            //用户在直连商户应用下的用户标示
            subMap.put("openid", openId);
            list.add(subMap);
            postMap.put("transfer_detail_list", list);
            logger.info(wechatPayserialNo);
            logger.info(privatekeypath);
            //发起转账操作
            String resStr = WeiXinHttpUtil.postTransBatRequest(
                    "https://api.mch.weixin.qq.com/v3/transfer/batches",
                    JsonUtils.toString(postMap),
                    wechatPayserialNo,
                    mchid,
                    privatekeypath);
            resultMap = JsonUtils.string2Obj(resStr, Map.class);
            System.out.println(resStr);
            if (null != resultMap && null != resultMap.get("out_batch_no") && null != resultMap.get("batch_id")) {
                //提现成功
                resultMap.put("start", true);
                resultMap.put("resStr", resStr);
                return resultMap;
            }
            resultMap = resultMap == null ? new HashMap<>() : resultMap;
            resultMap.put("resStr", resStr);
            resultMap.put("start", false);
            return resultMap;
        }
        resultMap.put("start", false);
        return resultMap;
    }


    public void selectYFPayConfig() {

        // 初始化支付方式配置，用于设置微信支付的相关参数
        //微信 pc  h5
        String paymentMode = "00";
        // 根据支付方式查询支付配置信息
        Map<String, String> outMap = payConfigMapper.selectYfPayConfig(paymentMode);
        // 设置微信支付的API密钥
        WXPayConfig.API_KEY = outMap.get("appAPIKey");
        // 设置微信支付的APP密钥
        WXPayConfig.APP_SECRET = outMap.get("appSecret");
        // 设置微信支付的商户号
        WXPayConfig.MCH_ID = outMap.get("appMCHId");
        // 设置微信支付的APPID
        WXPayConfig.APP_ID = outMap.get("appID");
        // 设置微信支付的回调通知URL
        WXPayConfig.NOTIFY_URL = outMap.get("notifyUrl");
        // 设置微信支付的重定向URL
        WXPayConfig.REDIRECT_URL = outMap.get("redirectUrl");
        // 设置微信PC支付的路径
        WXPayConfig.weChatWapPath = outMap.get("weChatPcPath");
        // 设置微信补充通知URL
        WXPayConfig.PLUS_NOTIFY_URL = outMap.get("plusNotifyUrl");

        // 初始化微信APP支付配置
        // 微信app
        WXConfigUtil.KEY = outMap.get("appAPIKey");
        WXConfigUtil.APP_SECRET = outMap.get("appSecret");
        WXConfigUtil.MCH_ID = outMap.get("appMCHId");
        WXConfigUtil.APP_ID = outMap.get("appID");
        WXConfigUtil.NOTIFY_URL_APP = outMap.get("notifyUrl");

        // 初始化微信小程序支付配置
        //微信小程序
        String paymentMode2 = "02";
        Map<String, String> outMap2 = payConfigMapper.selectYfPayConfig(paymentMode2);
        // 设置微信小程序的APPID
        WXPayConfig.Applet_APP_ID = outMap2.get("appID");
        // 设置微信小程序的密钥
        WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret");

        // 初始化支付宝支付配置
        //支付宝
        String paymentMode3 = "03";
        outMap = payConfigMapper.selectYfPayConfig(paymentMode3);
        // 检查支付宝配置信息是否存在
        if (null != outMap && !outMap.isEmpty()) {
            // 设置支付宝的网关URL
            ALiPayConfig.URL = outMap.get("gatewayURL");
            // 设置支付宝的APPID
            ALiPayConfig.APPID = outMap.get("appID");
            // 设置支付宝的商户号
            ALiPayConfig.sellerId = outMap.get("appMCHId");
            // 设置支付宝的私钥
            ALiPayConfig.RSA_PRIVATE_KEY = outMap.get("appAPIKey");
            // 设置支付宝的公钥证书路径
            ALiPayConfig.CERT_PATH = outMap.get("cert_public_key_res2");
            // 设置支付宝的根证书路径
            ALiPayConfig.ROOT_CERT_PATH = outMap.get("cert_root_key");
            // 设置支付宝的应用公钥证书路径
            ALiPayConfig.ALIPAY_PUBLIC_CERT_PATH = outMap.get("cert_app_public_key");
            // 设置支付宝交易成功的回调通知URL
            ALiPayConfig.shopping_notify_url = outMap.get("shoppingNotifyUrl");
            // 设置支付宝H5支付成功的回调通知URL
            ALiPayConfig.shopping_notify_url_h5 = outMap.get("rechargeNotifyUrl2");
            // 设置支付宝PC支付返回的URL
            ALiPayConfig.shopping_return_url_pc = outMap.get("shoppingReturnUrlPc");
            // 设置支付宝补充通知URL
            ALiPayConfig.plus_notify_url = outMap.get("plusNotifyUrl");
        }

    }


    public void insertPayRecord(FXCommonDTO fxCommon, UserInfoVO userInfo) {
        PayFxRecordEntity gameGroupRecord = new PayFxRecordEntity(null, 0, null);
        // 交易单号
        gameGroupRecord.setRecordId(fxCommon.getRecordId());
        // 用户ID
        gameGroupRecord.setUserId(userInfo.getUserId());
        // 用户名
        gameGroupRecord.setUserName(userInfo.getUserName());
        // 交易金额
        gameGroupRecord.setRecordAmount(fxCommon.getTotalMoney());
        // 交易状态 1:待处理 2交易完成 3交易取消
        gameGroupRecord.setRecordStatus(fxCommon.getRecordStatus());
        // 账户类型 1微客佣金  2员工佣金
        gameGroupRecord.setRecordType(fxCommon.getRecordType());
        // 交易类型
        gameGroupRecord.setTradeTypeId(fxCommon.getTradeTypeId());
        // 金额正负判断(1:+0:-)
        gameGroupRecord.setPlusOrMinus(fxCommon.getPlusOrMinus());
        // 支付方式1支付宝 2微信
        gameGroupRecord.setPayCode(fxCommon.getPayCode());
        // 提现的微信账户信息
        gameGroupRecord.setWxOpenId(StringUtils.isBlank(fxCommon.getWxOpenId()) ? null : fxCommon.getWxOpenId());
        gameGroupRecord.setWxUserName(StringUtils.isBlank(fxCommon.getWxUserName()) ? null : fxCommon.getWxUserName());
        // 支付来源
        gameGroupRecord.setPayFrom(fxCommon.getPlatFrom());
        // 描述
        String payName = CommonType.payCode.getByCode(fxCommon.getPayCode());
        gameGroupRecord.setRecordName("提现至" + payName);
        // 状态说明
        gameGroupRecord.setRecordStatusName("提现待处理");
        // 操作日期
        gameGroupRecord.setRecordDate(DateUtils.parseDateSDF(DateUtils.getNowDay()));
        // 创建时间
        gameGroupRecord.setCreateTime(DateUtils.getNowDate());
        // 更新时间
        gameGroupRecord.setUpdateTime(DateUtils.getNowDate());
        payFxRecordMapper.insert(gameGroupRecord);
    }


    public void insertThirdPayInfo(FXCommonDTO fxCommon, UserInfoVO userInfo) {
        PayFxTradeEntity gameGroupTrade = new PayFxTradeEntity(0, fxCommon.getPayCode());
        // 第三方交易请求单号
        gameGroupTrade.setThirdPayId(fxCommon.getThirdPayId());
        // 交易单号
        gameGroupTrade.setRecordId(fxCommon.getRecordId());
        // 用户id
        gameGroupTrade.setUserId(userInfo.getUserId());
        // 用户名
        gameGroupTrade.setUserName(userInfo.getUserName());
        // 用户名
        gameGroupTrade.setTotalMoney(fxCommon.getTotalMoney());
        // 交易手续费
        gameGroupTrade.setTradeRate(fxCommon.getTaxAmount());
        // 交易描述
        gameGroupTrade.setTradeDesc(fxCommon.getTradeDesc());
        // 交易类型
        gameGroupTrade.setTradeTypeId(fxCommon.getTradeTypeId());
        // 创建时间
        gameGroupTrade.setCreateTime(DateUtils.getNowDate());
        // 更新时间
        gameGroupTrade.setUpdateTime(DateUtils.getNowDate());
        payFxTradeMapper.insert(gameGroupTrade);
    }


    /**
     * 初始化请求参数
     *
     * @param withdrawRequest
     * @param thirdPayId
     * @param recordId
     */
    private void initWithdrawRequest(UserVkVO gameGroupAccount, WithdrawRequestDTO withdrawRequest, String thirdPayId, String recordId) {
        // 设置第三方支付ID，用于标识此次提现操作所关联的支付平台订单ID
        withdrawRequest.setThirdPayId(thirdPayId);
        // 设置记录ID，用于唯一标识此次提现操作
        withdrawRequest.setRecordId(recordId);
        // 设置记录类型为1，表示这是一次提现操作
        withdrawRequest.setRecordType(1);
        // 设置记录状态为待处理，表示此次提现操作尚未完成
        withdrawRequest.setRecordStatus(CommonType.recordStatus2.WAITING_FOR_DISPOSE.getCode());
        // 设置交易描述为“提现”，用于直观说明此次交易的性质
        withdrawRequest.setTradeDesc("提现");
        // 设置交易类型为2，表示这是一次提现交易
        withdrawRequest.setTradeTypeId(2);
        // 设置交易金额的增减标志为0，表示此次交易为减少账户余额的操作
        withdrawRequest.setPlusOrMinus(0);

        // 余额提现账户信息
        if (withdrawRequest.getPayCode().equals(CommonType.payType.BALANCE.getCode())) {
            withdrawRequest.setAliUserId(gameGroupAccount.getUserId());
            withdrawRequest.setAliUserName(gameGroupAccount.getUserName());
        } else {
            // 微信提现账户信息
            withdrawRequest.setWxOpenId(gameGroupAccount.getOpenId());
            withdrawRequest.setWxUserName(gameGroupAccount.getUserName());
        }
    }

    /**
     * 验证提现金额
     *
     * @param withdrawRequest
     * @param userInfo
     */
    private ResponseResult checkWithdrawAmount(WithdrawRequestDTO withdrawRequest, UserInfoVO userInfo) {
        ResponseResult resultResp;
        if (null != withdrawRequest.getTotalMoney() && withdrawRequest.getTotalMoney().compareTo(BigDecimal.ZERO) > 0) {
            if (withdrawRequest.getPayCode().equals(CommonType.payType.BALANCE.getCode())) {
                if (withdrawRequest.getTotalMoney().compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseResult.fail(0, "支付宝提现金额不可低于0.1元");
                }
            } else {
                if (withdrawRequest.getTotalMoney().compareTo(WXPayConfig.MIN_AMOUNT) < 0) {
                    return ResponseResult.fail(0, "微信提现金额不可低于0.3元");
                }
            }
            // 1.查询用户账户
            UserVkVO userAccountAccount = usermsServiceClient.queryUserAccount(userInfo.getUserId());
            if (null == userAccountAccount || userAccountAccount.getPendingWithdrawnCommission().compareTo(BigDecimal.ZERO) <= 0) {
                // 2.微客余额不足不可提现
                return ResponseResult.fail(0, "用户余额不足,无法提现!");
            } else if (withdrawRequest.getTotalMoney().compareTo(userAccountAccount.getPendingWithdrawnCommission()) > 0) {
                // 3.提现金额大于微客余额  不可提现
                return ResponseResult.fail(0, "您的账户余额不足,请确认提现金额后重试");
            } else {
                // 4.金额验证成功后,验证用户提现授权信息
                if (withdrawRequest.getPayCode().equals(CommonType.payType.BALANCE.getCode())) {
                    // 验证成功
                    resultResp = ResponseResult.success(userAccountAccount);
                } else if (withdrawRequest.getPayCode().equals(CommonType.payType.WX.getCode())) {
                    if (StringUtils.isBlank(userAccountAccount.getOpenId())) {
                        // 用户未授权
                        resultResp = ResponseResult.fail(0, "用户未授权");
                    } else {
                        // 验证成功
                        resultResp = ResponseResult.success(userAccountAccount);
                    }
                } else {
                    // 验证成功
                    resultResp = ResponseResult.fail(0, "提现金额不可小于0,请确认后重试");
                }
            }
        } else {
            // 提现金额不可小于等于0
            resultResp = ResponseResult.fail(0, "提现金额不可小于0,请确认后重试");
        }
        return resultResp;
    }


}




