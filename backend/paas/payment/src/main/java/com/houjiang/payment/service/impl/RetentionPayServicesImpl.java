package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayCommonUtils;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.MerchantServiceClient;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.service.PayUserResourceService;
import io.seata.core.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 质保金充值
 *
 * @author Amos
 * @date 2023/4/27 10:34
 */
@Service
public class RetentionPayServicesImpl implements PayStrategyHandleService {


    private static PayConsumeTradeService payConsumeTradeService;


    private static PayCommonUtils payCommonUtils;


    private static PayUserResourceService payUserResourceService;

    private static MerchantServiceClient merchantServiceClient;


    final Logger log = LoggerFactory.getLogger(RetentionPayServicesImpl.class);

    // 总次数
    static Integer errorSum = 3;

    @Autowired
    public void setMerchantServiceClient(MerchantServiceClient merchantServiceClient) {
        RetentionPayServicesImpl.merchantServiceClient = merchantServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setChatService(PayUserResourceService payUserResourceService) {
        RetentionPayServicesImpl.payUserResourceService = payUserResourceService;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayCommonUtils(PayCommonUtils payCommonUtils) {
        RetentionPayServicesImpl.payCommonUtils = payCommonUtils;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayConsumeTradeService(PayConsumeTradeService payConsumeTradeService) {
        RetentionPayServicesImpl.payConsumeTradeService = payConsumeTradeService;
    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        Map<Object,Object> assetDetailMap = new HashMap<>();
        String token = (String) map.get("token");
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(token);
        String userId = userInfo.getUserId();

        String passwd = String.valueOf(map.get("passwd"));

        //生成充值单号
        StringBuffer id = new StringBuffer("C");
        DateFormat dFormat3 = new SimpleDateFormat("yyyyMMdd");

        String format = dFormat3.format(new Date());

        Random random = new Random();
        String verificationCode = String.valueOf(random.nextInt(9) + 1);
        for (int i = 0; i < 5; i++) {
            verificationCode += random.nextInt(10);
        }
        id.append(format).append(verificationCode);

        map.put("orderId",id.toString());

        //余额支付
        if (map.get("rechargeType").equals(1)){
            //输错次数
            String errorCount = RedisUtil.get(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId().toString());
            if( null != errorCount && errorSum.equals(Integer.valueOf(errorCount))){
                return ResponseResult.fail(BizCodeEnume.PAY_PASSWORD_ERROR.getCode(),BizCodeEnume.PAY_PASSWORD_ERROR.getMsg());
            }
            // 验证支付密码
            Integer isExist = payCommonUtils.getPwdExist(passwd, userInfo).getCode();
            if (isExist == 1) {

                //支付金额
                String payPrice = String.valueOf(map.get("payPrice"));
                BigDecimal amountActuallyPaids = new BigDecimal(payPrice);

                // 查询账户余额
                PayUserResourceEntity payUserResourceEntity = payUserResourceService.getById(userInfo.getUserId());
                BigDecimal userBalanceMoney = payUserResourceEntity.getUserMoney();

                // 判断实付金额与余额
                if (userBalanceMoney.compareTo(amountActuallyPaids) == 1
                        || userBalanceMoney.compareTo(amountActuallyPaids) == 0) {
                    // 扣除购物金额
                    boolean updateMoney = payUserResourceService.update(new LambdaUpdateWrapper<PayUserResourceEntity>()
                            .eq(PayUserResourceEntity::getUserId, userInfo.getUserId())
                            .set(PayUserResourceEntity::getUserMoney, payUserResourceEntity.getUserMoney().subtract(amountActuallyPaids)));

                    if (updateMoney) {
                        assetDetailMap.put("assetDirection", "质保金账户");
                        assetDetailMap.put("payType",1);
                        assetDetailMap.put("userId",userId);
                        assetDetailMap.put("detailType",10);
                        assetDetailMap.put("assetType",2);
                        assetDetailMap.put("assetAmount",amountActuallyPaids);
                        assetDetailMap.put("orderId",id.toString());
                        //插入记录信息
                        ResponseResult recharge = payCommonUtils.getetentionPay(assetDetailMap, map);
                        if (recharge.getCode() != 1){
                            // 回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        }
                        return ResponseResult.success("支付成功！");

                    } else {
                        return ResponseResult.fail(0, "支付失败！");
                    }
                } else {
                    return ResponseResult.fail(0, "账户余额不足，请选择其他支付方式!");
                }
            } else {
                if(null == errorCount){
                    errorCount = "1";
                }else{
                    Integer count = Integer.valueOf(errorCount) + 1;
                    errorCount = count.toString();
                }
                //输错次数
                RedisUtil.set(HouConfig.HOU_USERID_SHOPID + userInfo.getUserId().toString(),errorCount);
                if (errorCount.equals("3")) {
                    //密码错误三次！
                    return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_THREE_TIMES);
                }
                return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_TWO);
            }
        }else{
            //插入记录信息
            ResponseResult recharge = merchantServiceClient.recharge(map);
            if (recharge.getCode() != 1){
                // 回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            return recharge;
        }
    }
}
