package com.houjiang.commons.rocketmq.producer;

import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.commons.rocketmq.config.RocketMqClientFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Amos
 * 消息 生产者
 */
@Component
@Slf4j
@RefreshScope
@Profile("with-mq")
public class MqProducer {

    @Value("${rocketmq.name-server}")
    public String nameServer;

    @Value("${rocketmq.producer.access-key:}")
    private String accessKey;

    @Value("${rocketmq.producer.secret-key:}")
    private String secretKey;

    protected String getProducerGroup() {
        return JmsConfig.PRODUCER_GROUP;
    }

    @Getter
    private DefaultMQProducer producer;

    public String getProducerGroup(String grpop) {
        return grpop;
    }

    /**
     * 对象在使用之前必须要调用一次，只能初始化一次
     */
    @PostConstruct
    public void start() {
        producer = RocketMqClientFactory.createProducer(getProducerGroup(), accessKey, secretKey);
        producer.setNamesrvAddr(nameServer);

        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    /**
     * 一般在应用上下文，使用上下文监听器，进行关闭
     */
    @PreDestroy
    public void shutdown() {
        this.producer.shutdown();
    }


    /**
     * 同步 发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    public SendResult sendMsg(Message message) {
        try {
            SendResult sendResult = getProducer().send(message,10000);
            log.info("MQ消息发送成功!");
            return sendResult;
        } catch (Exception e) {
            throw new RuntimeException("MQ消息发送失败!", e);
        }
    }

    /**
     * 异步 发送消息
     *
     * @param message 消息
     */
    public void sendAsyMsg(Message message) {

        try {
            getProducer().send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("MQ消息发送成功!");
                }

                @Override
                public void onException(Throwable throwable) {
                    // 异步发送没有返回值，在回调线程里抛异常没人接，只会被线程池吞成一行堆栈；
                    // 这里改为落一条带 topic/keys 的 error，便于对账和人工补偿。
                    // 若业务要求「发不出去就必须拦住流程」，请改用同步 sendMsg（会抛异常）
                    log.error("MQ异步消息发送失败，需核对/补偿 topic={}, keys={}",
                            message.getTopic(), message.getKeys(), throwable);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("MQ消息发送失败!", e);
        }
    }


    /**
     * 延迟 发送消息
     *
     * @param message        消息
     * @param delayTimeLevel 延迟级别
     * @return 发送结果
     */
    public SendResult sendDelayMsg(Message message, int delayTimeLevel) {
        try {
            //xxx是级别，1表示配置里面的第一个级别，2表示第二个级别，延迟消费级别
            //"1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"
            message.setDelayTimeLevel(delayTimeLevel);

            SendResult sendResult = getProducer().send(message);
            log.info("MQ消息发送成功!");
            return sendResult;
        } catch (Exception e) {
            throw new RuntimeException("MQ消息发送失败!", e);
        }
    }

    /**
     * 延迟 发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    public SendResult sendDelayMsg(Message message) {
        return sendDelayMsg(message, 3);
    }

}
