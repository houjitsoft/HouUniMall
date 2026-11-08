package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayPlusServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeMapper payConsumeTradeMapper;

    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        WXPayPlusServicesImpl.usermsServiceClient = usermsServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeMapper payConsumeTradeMapper,
                          PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        WXPayPlusServicesImpl.payConfigMapper = payConfigMapper;
        WXPayPlusServicesImpl.payConsumeTradeMapper = payConsumeTradeMapper;
        WXPayPlusServicesImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;
        WXPayPlusServicesImpl.payConsumeRecordMapper = payConsumeRecordMapper;
    }

    final static Logger log = LoggerFactory.getLogger(WXPayPlusServicesImpl.class);

    /**
     * 初始化 阿里云支付参数
     */
    public void initWXPayConfig() {
        // 初始化微信支付pc、h5配置
        this.initWeChatPayConfig();

        // 初始化支付方式为"02"（微信小程序）的配置
        String paymentMode2 = "02";
        // 通过支付方式查询微信小程序的支付配置信息
        Map<String, Object> outMap2 = payConfigMapper.selectPayConfig(paymentMode2);
        // 将查询到的微信小程序支付配置信息赋值给WXPayConfig
        WXPayConfig.Applet_APP_ID = outMap2.get("appID").toString();
        WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret").toString();

    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        try {
            //查询支付配置信息
            initWXPayConfig();
            String token = (String) map.get("token");
            BigDecimal totalPrice = new BigDecimal(String.valueOf(map.get("payPrice")));
            Map<Object, Object> param = new HashMap<>();
            param.put("thaliType",map.get("thaliType"));
            param.put("payPrice",map.get("payPrice"));
            param.put("thaliEquity",map.get("thaliEquity"));

            //创建支付单号
            int i = PayToolUtil.buildRandom(6);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            StringBuffer plusorder = new StringBuffer("PLUS");
            plusorder.append(format.format(new Date()));
            plusorder.append(i);

            String goodsName = "PLUS订单编号" + plusorder.toString();

            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            String userId = userInfo.getUserId();
            // redis 存入token
            RedisUtil.set(HouConfig.HOU_PLUS_ORDER + plusorder.toString(),map.get("token").toString(),3600);
            // 将订单总金额转换为字符串格式，以便后续处理
            String payAmount = String.valueOf(totalPrice);

            // 调用工具类方法，将订单金额格式化为支付工具所需的格式
            String price = PayToolUtil.getMoney(payAmount);

            // 初始化参数映射，用于存储微信支付所需的请求参数
            Map<String, String> params = new HashMap<String, String>();

            // 添加微信支付配置中的AppID和商户号
            params.put("appid", WXPayConfig.APP_ID);
            params.put("mch_id", WXPayConfig.MCH_ID);

            // 生成随机字符串，作为请求的一部分，增加请求的唯一性
            params.put("nonce_str", PayToolUtil.create_nonce_str());

            // 添加订单描述和附加数据
            params.put("body", goodsName);
            params.put("attach", JSON.toJSONString(param));

            // 添加订单编号和订单总金额
            // 开会员需要参数
            params.put("out_trade_no", plusorder.toString());
            params.put("total_fee", price);

            // 添加终端IP地址，用于支付验证
            params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));

            // 设置通知URL，用于接收支付结果通知
            params.put("notify_url", WXPayConfig.PLUS_NOTIFY_URL);

            // 设置交易类型为Native，适用于扫码支付
            params.put("trade_type", "NATIVE");

            // 生成签名，确保请求的安全性
            String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
            params.put("sign", sign);

            // 记录请求参数，用于调试和日志记录
            log.info("param{}", JSON.toJSONString(params));

            // 调用支付API，发送支付请求，返回支付结果的XML字符串
            String xmlResult = PaymentApi.pushOrder(params);

            // 将XML格式的结果转换为Map格式，以便于后续的数据处理和提取。
            Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

            // 初始化支付参数映射表，用于构建支付请求。
            HashMap<String, Object> payMap = new HashMap<>();
            // 设置订单唯一标识。
            payMap.put("plusOrderid", plusorder.toString());
            // 设置用户ID，用于关联支付请求和用户。
            payMap.put("userId", userId);
            // 设置支付金额，确保支付金额的准确性。
            payMap.put("payPrice", totalPrice);
            // 设置支付方式，这里选择微信支付。
            payMap.put("payType", CommonType.plusPayType.PLUS_WX_PAY.getCode());
            // 设置桌型信息，用于特定场景下的支付处理。
            payMap.put("thaliType", map.get("thaliType"));
            // 设置权益信息，可能影响支付折扣或其他优惠。
            payMap.put("plusEquity", map.get("thaliEquity"));

            //插入plus预购买单
            usermsServiceClient.aLiWxCreatePlusOrder(payMap);

            // 获取返回码
            String return_code = result.get("return_code");
            // 获取返回消息
            String return_msg = result.get("return_msg");
            // 检查返回码是否为空或不是"SUCCESS"，如果是，则返回失败响应
            if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
                return ResponseResult.fail(return_msg);
            }
            // 获取业务结果码
            String result_code = result.get("result_code");
            // 检查业务结果码是否为空或不是"SUCCESS"，如果是，则返回失败响应
            if (StrKit.isBlank(result_code) || !"SUCCESS".equals(result_code)) {
                return ResponseResult.fail(return_msg);
            }

            // 获取二维码链接
            String qrCodeUrl = result.get("code_url");
            // 返回成功响应，携带二维码链接
            return ResponseResult.success(qrCodeUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }


}
