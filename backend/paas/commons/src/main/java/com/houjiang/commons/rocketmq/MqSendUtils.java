package com.houjiang.commons.rocketmq;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.commons.rocketmq.producer.MqProducer;
import com.houjiang.commons.util.IpUtils;
import jakarta.annotation.PostConstruct;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ 消息发送工具类
 * <p>
 * 封装各类消息的异步发送逻辑，所有方法均使用 try-catch 包裹，
 * 发送失败仅打印警告日志，不影响业务流程。
 * 本类通过 {@code @Profile("with-mq")} 控制，无 MQ 环境时自动跳过。
 * </p>
 *
 * @author Amos
 * @since 2023/4/10
 */
@Component
@Profile("with-mq")
public class MqSendUtils {

    private static final Logger log = LoggerFactory.getLogger(MqSendUtils.class);


    @Autowired
    private MqProducer mqProducer;

    private static MqSendUtils mqSendUtils;

    /** 通过@PostConstruct实现初始化bean之前进行的操作 */
    @PostConstruct
    public void init() {
        mqSendUtils = this;
        mqSendUtils.mqProducer = this.mqProducer;

    }
    /**
     * mq发送商品消息
     * @param map
     */
    public static void mqSendGoods(Map<String,Object> map){
        try {
            map.put("ip", IpUtils.getIpAddress());
            Message message = JmsConfig.Utils.buildDefaultMessage (JSON.toJSONString(map));
            mqSendUtils.mqProducer.sendMsg(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mq发送更新ES商品消息
     * @param map
     */
    public static void mqSendGoodsByEs(Map<String,Object> map){
        try {
            map.put("ip", IpUtils.getIpAddress());
            Message message = JmsConfig.Utils.buildEsMessage (JSON.toJSONString(map));
            mqSendUtils.mqProducer.getProducerGroup(JmsConfig.PRODUCER_GOODS_ES_GROUP);
            mqSendUtils.mqProducer.sendMsg(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mq发送用户操作日志消息
     *
     * @param map
     */
    public static void mqSendSysLog(Map<String, Object> map) {
        try {
            map.put("ip", IpUtils.getIpAddress());
            Message message = JmsConfig.Utils.buildSysLogMessage(JSON.toJSONString(map));
            mqSendUtils.mqProducer.getProducerGroup(JmsConfig.PRODUCER_SYS_LOG_GROUP);
            mqSendUtils.mqProducer.sendAsyMsg(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mq发送更新下单商品库存消息
     *
     * @param map 商品库存消息内容
     */
    public static void mqSendGoodsStockByOrder(Map<String, String> map) {
        try {
            map.put("ip", IpUtils.getIpAddress());
            Message message = JmsConfig.Utils.buildOrderlyMessage(JSON.toJSONString(map));
            mqSendUtils.mqProducer.getProducerGroup(JmsConfig.ORDERLY_PRODUCER_GROUP);
            mqSendUtils.mqProducer.sendAsyMsg(message);
            log.info("发送下单商品库存消息成功");
        } catch (Exception e) {
            log.warn("发送下单商品库存消息失败: {}", e.getMessage());
        }
    }

    /**
     * 发送统一格式的日志消息
     * <p>
     * 消息体格式：
     * <pre>
     * {
     *   "logType": "OPERATION",
     *   "module": "pm",
     *   "data": { ... }
     * }
     * </pre>
     * 根据 logType 自动选择对应的 RocketMQ Tag，便于消费端按类型过滤。
     * 所有操作均在 try-catch 内执行，发送失败仅打印 warn 日志，不影响业务。
     * </p>
     *
     * @param logType 日志类型（OPERATION/BUSINESS/SYSTEM/ACCESS/LOGIN/ERROR）
     * @param module  所属模块名称（如 pm、oa、crm 等，可为空）
     * @param data    日志数据对象（将被序列化为 JSON）
     */
    public static void sendLogMessage(String logType, String module, Object data) {
        if (mqSendUtils == null) {
            // 非 with-mq 环境，直接跳过
            return;
        }
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("logType", logType);
            message.put("module", module != null ? module : "");
            message.put("data", data);

            String jsonMsg = JSON.toJSONString(message);
            // 根据 logType 选择对应 Tag
            Message mqMessage = resolveLogMessage(logType, jsonMsg);

            mqSendUtils.mqProducer.getProducerGroup(JmsConfig.PRODUCER_SYS_LOG_GROUP);
            mqSendUtils.mqProducer.sendAsyMsg(mqMessage);
        } catch (Exception e) {
            log.warn("发送日志消息失败: logType={}, module={}, error={}", logType, module, e.getMessage());
        }
    }

    /**
     * 根据日志类型解析对应的 RocketMQ 消息（携带不同 Tag）
     *
     * @param logType 日志类型字符串
     * @param jsonMsg 已序列化的消息 JSON 字符串
     * @return 带有对应 Tag 的 RocketMQ Message
     */
    private static Message resolveLogMessage(String logType, String jsonMsg) {
        if (logType == null) {
            return JmsConfig.Utils.buildOperationLogMessage(jsonMsg);
        }
        switch (logType.toUpperCase()) {
            case "ACCESS":
                return JmsConfig.Utils.buildAccessLogMessage(jsonMsg);
            case "LOGIN":
                return JmsConfig.Utils.buildLoginLogMessage(jsonMsg);
            case "ERROR":
                return JmsConfig.Utils.buildErrorLogMessage(jsonMsg);
            case "BUSINESS":
                return JmsConfig.Utils.buildBusinessLogMessage(jsonMsg);
            case "SYSTEM":
                return JmsConfig.Utils.buildSystemLogMessage(jsonMsg);
            case "OPERATION":
            default:
                return JmsConfig.Utils.buildOperationLogMessage(jsonMsg);
        }
    }

}
