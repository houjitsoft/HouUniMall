package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.entity.*;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.PayCardInfoService;
import com.houjiang.payment.service.PayUserBaseService;
import com.houjiang.payment.service.PayUserResourceService;
import com.houjiang.payment.vo.PayCardBaseInfoVO;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class PayCardInfoServiceImpl extends ServiceImpl<PayCardInfoMapper, PayCardInfoEntity>
    implements PayCardInfoService{

    @Autowired
    private PayUserResourceService payUserResourceService;
    @Autowired
    private PayUserBaseService payUserBaseService;
    @Autowired
    private PayCardBaseMapper payCardBaseMapper;
    @Autowired
    private PayConsumeDepositMapper payConsumeDepositMapper;
    @Autowired
    private PayConsumeRecordMapper payConsumeRecordMapper;
    @Autowired
    private PayAssetDetailsMapper payAssetDetailsMapper;
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult rechargeCard(Map<Object, Object> map) throws TransactionException {
        Map<Object, Object> assetDetailMap = new HashMap<>();
        try {
            String tokenRedis = HouConfig.HOU_USER_TOKEN_USER + map.get("token");
            if (!RedisUtil.exists(tokenRedis)) {
                throw new DefinedException(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
            }
            UserInfoDTO dto = JSON.parseObject(RedisUtil.get(tokenRedis), UserInfoDTO.class);
            UserInfoVO ui = dto.getUser();
            String userId = ui.getUserId();
            String userName = ui.getUserName();
            String cardCode = (String) map.get("cardCode");
            String cardPasswd = (String) map.get("cardPasswd");
            // 1 wap 2 ios 3 Android 4 pc
            Integer type = (Integer) map.get("type");
            String from = null;
            if (type == 1) {
                from = "wap";
            } else if (type == 2) {
                from = "ios";
            } else if (type == 3) {
                from = "Android";
            } else if (type == 4) {
                from = "pc";
            }
            // 充值表所需参数
            // 交易创建时间
            String depositGmtCreate = DateUtils.getNowDateToString();
            // 充值订单号
            String depositTradeNo = "R" + DateUtils.getTimeStamp();
            // 充值的付款方式
            String payChannel = "6";
            // 交易关闭时间
            String gmtClose = DateUtils.getNowDateToString();
            // 交易状态
            Integer depositTradeStatus = 8;
            // 充值成功以后的流水
            String depositReturnTradeNo = cardCode;
            // 普通充值卡充值
            int rechargePackage = 0;

            // 交易明细表所需参数
            String orderId = depositTradeNo;
            // 日期
            String date = DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
            // 年
            String year = DateUtils.getYear();
            // 月
            String month = DateUtils.getMonth();
            // 日
            String day = DateUtils.getDay();
            // 交易类型
            String title = "充值";
            // 描述
            String desc = "充值卡充值";
            // 交易类型
            int typeId = 3;
            // 收款方
            int userType = 2;
            // 处理中
            Integer recordStatus = 8;
            // 实际支付单号
            String recordPayorder = cardCode;

            Integer isActivation = 0;
            // 判断卡片是否激活
            PayCardInfoEntity payCardInfoEntity = this.baseMapper.selectById(cardCode);
            if (payCardInfoEntity != null) {
                //判断购物卡是否失效
                PayCardBaseEntity payCardBaseEntity = payCardBaseMapper.selectById(payCardInfoEntity.getCardId());
                Date cardStartTime = payCardBaseEntity.getCardStartTime();
                Date cardEndTime = payCardBaseEntity.getCardEndTime();
                if (cardStartTime == null || new Date().compareTo(cardStartTime) < 0) {
                    return ResponseResult.fail(BizCodeEnume.CARD_NO_START);
                }
                if (cardEndTime == null || new Date().compareTo(cardEndTime) > 0) {
                    return ResponseResult.fail(BizCodeEnume.CARD_IS_END);
                }
                if (cardPasswd.equals(payCardInfoEntity.getCardPassword())) {
                    String buyersUserId = payCardInfoEntity.getUserId();
                    if (!StringUtils.isEmpty(buyersUserId) && !"0".equals(buyersUserId)) {
                        return ResponseResult.fail(BizCodeEnume.CARD_HAS_ALREADY_BEEN_USED);
                    } else {
                        // 查询当前账户是否存在
                        PayUserBaseEntity payUserBaseEntity = payUserBaseService.getById(userId);
                        if (payUserBaseEntity == null) {
                            payUserBaseEntity = new PayUserBaseEntity();
                            payUserBaseEntity.setUserId(userId);
                            payUserBaseService.save(payUserBaseEntity);
                        }
                        // 查询当前用户资产账户是否存在
                        PayUserResourceEntity payUserResourceEntity = payUserResourceService.getById(userId);
                        if (payUserResourceEntity == null) {

                            payUserResourceEntity.setUserId(userId);
                            payUserResourceService.save(payUserResourceEntity);
                        }
                        BigDecimal fee = payCardInfoEntity.getCardMoney();
                        // 充值成功 更新卡状态
                        String cardFetchTime = DateUtils.getNowDateToString();

                        Map<String, Object> mapCard = new HashMap<>();
                        mapCard.put("cardCode", cardCode);
                        mapCard.put("userId", userId);
                        mapCard.put("userName", userName);
                        mapCard.put("from", from);
                        mapCard.put("cardFetchTime", cardFetchTime);
                        boolean isUpdateSuccess = this.baseMapper.updateRechargeCard(mapCard);
                        if (isUpdateSuccess) {
                            Map<String, Object> mapDeposit = new HashMap<>();
                            mapDeposit.put("depositTradeNo", depositTradeNo);
                            mapDeposit.put("userId", userId);
                            mapDeposit.put("fee", fee);
                            mapDeposit.put("payChannel", payChannel);
                            mapDeposit.put("depositGmtCreate", depositGmtCreate);
                            mapDeposit.put("cardFetchTime", cardFetchTime);
                            mapDeposit.put("gmtClose", gmtClose);
                            mapDeposit.put("depositTradeStatus", depositTradeStatus);
                            mapDeposit.put("depositReturnTradeNo", depositReturnTradeNo);
                            mapDeposit.put("rechargePackage", rechargePackage);
                            mapDeposit.put("depositType", 2);

                            // 充值成功插入充值信息表
                            boolean isInsertByConsumeDeposit = payConsumeDepositMapper.insertByConsumeDeposit2(mapDeposit);
                            // 更新用户资产信息
                            boolean isUpdateByUserResource = payUserResourceService.update(payUserResourceEntity, new LambdaUpdateWrapper<PayUserResourceEntity>()
                                    .eq(PayUserResourceEntity::getUserId, userId)
                                    .set(PayUserResourceEntity::getUserRechargeCard, payUserResourceEntity.getUserRechargeCard().add(fee)));

                            if (isInsertByConsumeDeposit && isUpdateByUserResource) {
                                Map<String, Object> mapRecord = new HashMap<>();
                                mapRecord.put("orderId", orderId);
                                mapRecord.put("userId", userId);
                                mapRecord.put("userName", userName);
                                mapRecord.put("fee", fee);
                                mapRecord.put("date", date);
                                mapRecord.put("year", year);
                                mapRecord.put("month", month);
                                mapRecord.put("day", day);
                                mapRecord.put("title", title);
                                mapRecord.put("desc", desc);
                                mapRecord.put("depositGmtCreate", depositGmtCreate);
                                mapRecord.put("typeId", typeId);
                                mapRecord.put("userType", userType);
                                mapRecord.put("recordStatus", recordStatus);
                                mapRecord.put("recordPayorder", recordPayorder);
                                mapRecord.put("cardFetchTime", cardFetchTime);
                                mapRecord.put("plusOrMinus", 1);
                                // 插入交易明细表
                                boolean isInsertByConsumeRecord = payConsumeRecordMapper.insertConsumeRecord(mapRecord);
                                // 更改订单充值状态
                                if (isInsertByConsumeRecord) {
                                    int newRecordStatus = CommonType.orderStatus.CONFIRM_RECEIPT.getCode();
                                    // 新订单状态
                                    payConsumeRecordMapper.update(null, new LambdaUpdateWrapper<PayConsumeRecordEntity>()
                                            .eq(PayConsumeRecordEntity::getOrderId, orderId)
                                            .eq(PayConsumeRecordEntity::getRecordStatus, recordStatus)
                                            .set(PayConsumeRecordEntity::getRecordStatus, newRecordStatus));

                                }
                                //新交易明细表插入
                                assetDetailMap.put("assetDirection", "购物卡-" + cardCode);
                                assetDetailMap.put("payType", 2);
                                assetDetailMap.put("userId", userId);
                                assetDetailMap.put("detailType", 7);
                                assetDetailMap.put("assetType", 1);
                                assetDetailMap.put("assetAmount", fee);
                                assetDetailMap.put("orderId", orderId);
                                payAssetDetailsMapper.insertDetail(assetDetailMap);
                            }
                            return ResponseResult.success(BizCodeEnume.RECHARGED_SUCCESSFULLY.getMsg());
                        } else {
                            return ResponseResult.fail(BizCodeEnume.RECHARGE_FAILED);
                        }
                    }
                } else {
                    return ResponseResult.fail(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION);
                }
            } else {
                return ResponseResult.fail(BizCodeEnume.RECHARGE_CARD_NOT_ACTIVATED);
            }
        } catch (Exception e) {
            // 手动回滚事务
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            return ResponseResult.fail(BizCodeEnume.RECHARGE_FAILED);
        }
    }

    @Override
    public ResponseResult execl(Map<String, Object> paraMap, HttpServletResponse response) throws IOException {
        List<PayCardBaseInfoVO> result = this.baseMapper.execl(paraMap);

        String[] title = {"卡ID", "用户ID", "卡片密码", "卡片激活码", "领奖时间", "媒体ID", "领卡人服务器ID", "领卡人账号", "卡牌生成时间", "卡冻结金额",
                "卡片描述", "充值卡余额", "卡的有效开始时间", "卡片数量", "卡名称"};
        List<Map<String, String>> list = Lists.newArrayList();

        for (PayCardBaseInfoVO res : result) {
            HashMap<String, String> map = com.google.common.collect.Maps.newHashMap();
            map.put("卡ID", res.getCardId() + "");
            map.put("用户ID", res.getUserId() + "");
            map.put("卡片密码", res.getCardPassword());
            map.put("卡片激活码", res.getCardCode());
            map.put("领奖时间", res.getCardFetchTime());
            map.put("媒体ID", res.getCardMediaId() + "");
            map.put("领卡人服务器ID", res.getServerId() + "");
            map.put("领卡人账号", res.getUserAccount());
            map.put("卡牌生成时间", res.getCardTime());
            map.put("卡冻结金额", res.getCardFrozeMoney());
            map.put("卡片描述", res.getCardDesc());
            map.put("充值卡余额", res.getCardMoney() + "");
            map.put("卡的有效开始时间", res.getCardTime());
            map.put("卡片数量", res.getCardNum() + "");
            map.put("卡名称", res.getCardName());
            list.add(map);

        }
        // excel文件名
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("合并数据", list);
        String url = ExcelsUtil.createExcel(title, map, new int[]{16});


        return ResponseResult.success(url);
    }
}




