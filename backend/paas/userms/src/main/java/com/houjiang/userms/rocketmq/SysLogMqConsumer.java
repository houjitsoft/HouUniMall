package com.houjiang.userms.rocketmq;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.commons.rocketmq.config.RocketMqClientFactory;
import com.houjiang.userms.entity.UserLogActionEntity;
import com.houjiang.userms.service.UserLogActionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/8 16:26
 */
@Slf4j
@Component
public class SysLogMqConsumer {
    private DefaultMQPushConsumer consumer;

    @Value("${rocketmq.name-server:}")
    private String nameServer;
    @Value("${rocketmq.consumer.access-key:}")
    private String accessKey;
    @Value("${rocketmq.consumer.secret-key:}")
    private String secretKey;
    @Autowired
    private UserLogActionService ucenterLogActionService;

    @PostConstruct
    public void ini() throws MQClientException {
        // 如果 RocketMQ 配置缺失，跳过消费者初始化
        log.info("SysLogMqConsumer 初始化，nameServer值: [{}]", nameServer);
        if (nameServer == null || nameServer.trim().isEmpty() || "null".equalsIgnoreCase(nameServer.trim())) {
            log.warn("RocketMQ name-server 配置缺失或无效，跳过 SysLogMqConsumer 初始化");
            return;
        }

        consumer = RocketMqClientFactory.createPushConsumer(
                JmsConfig.CONSUMER_SYS_LOG_GROUP, accessKey, secretKey);
        try {
            consumer.setNamesrvAddr(nameServer);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

            consumer.subscribe(JmsConfig.DEFAULT_SYSLOG_TOPIC, "*");

            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                MessageExt msg = msgs.get(0);

                int times = msg.getReconsumeTimes();
                if (times > 0) {
                    log.info("重试次数=" + times);
                }
                try {
                    String topic = msg.getTopic();
                    String body = new String(msg.getBody(), "utf-8");
                    String tags = msg.getTags();
                    String keys = msg.getKeys();
                    log.info("消费消息:" + "topic=" + topic + ", tags=" + tags + ", keys=" + keys + ", msg=" + body);
                    Map<String, Object> map = JSON.parseObject(body);
                    UserLogActionEntity entity= JSON.parseObject(map.get("log").toString(),UserLogActionEntity.class);
                    entity.setLogDate(Date.from(LocalDateTime.now().atZone( ZoneId.systemDefault()).toInstant()));
                    //插入操作日志
                    ucenterLogActionService.save(entity);

                    log.info("操作日志保存成功");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (UnsupportedEncodingException e) {
                    log.error("消费异常" + e.getMessage());
                    //如果重试2次不成功，则记录，人工介入
                    if (times >= 2) {
                        log.error("重试次数大于2，记录数据库，发短信通知开发人员或者运营人员");
                        //TODO 记录数据库，发短信通知开发人员或者运营人员
                        //告诉broker，消息成功
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            });

            consumer.start();
            log.info("consumer start ...");
        } catch (Exception e) {
            log.error("RocketMQ 消费者启动失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响服务启动
            log.warn("跳过 RocketMQ 消费者初始化，服务将继续启动");
        }
    }

}
