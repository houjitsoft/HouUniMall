package com.houjiang.order.rocketmq;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.commons.rocketmq.config.RocketMqClientFactory;
// import com.houjiang.order.feign.BusinessServiceClient; // TODO: business 服务已删除
import com.houjiang.order.feign.MerchantServiceClient;
import com.houjiang.order.feign.GoodServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * description 有序的进行消费 消费者
 *
 * 需要配合有序的消息进行一起使用
 *
 */
@Slf4j
@Component
public class MqOrderlyConsumer {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.consumer.access-key:}")
    private String accessKey;

    @Value("${rocketmq.consumer.secret-key:}")
    private String secretKey;

    @Resource
    private GoodServiceClient goodServiceClient;

    @Resource
    private MerchantServiceClient merchantServiceClient;

    @PostConstruct
    public void ini() {
        DefaultMQPushConsumer consumer = RocketMqClientFactory.createPushConsumer(
                JmsConfig.CONSUMER_ORDERLY_GROUP, accessKey, secretKey);
        try {
            consumer.setNamesrvAddr(nameServer);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

            //默认是集群方式，可以更改为广播，但是广播方式不支持重试
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.subscribe(JmsConfig.DEFAULT_ORDERLY_TOPIC, "*");

            consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
                MessageExt msg = msgs.getFirst();

                int times = msg.getReconsumeTimes();
                if (times > 0) {
                    log.info("重试次数="+times);
                }

                String topic = msg.getTopic();
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                String tags = msg.getTags();
                String keys = msg.getKeys();
                log.info("消费消息:" + "topic=" + topic + ", tags=" + tags + ", keys=" + keys + ", msg=" + body);
                int queueId = msg.getQueueId();
                log.info("消费消息,队列id:" +queueId + ",msg:" + body);
                Map map = JSON.parseObject(body);
                String type = (String) map.get("type");
                System.out.println("map ==========" + map);
                System.out.println("type ==========" + type);
                //做业务逻辑操作
                if("1".equals(type)){
                    String saleSum = (String) map.get("saleSum");
                    merchantServiceClient.updateShopGateStock(saleSum);
                }else if("2".equals(type)){
                    goodServiceClient.updateSaleSum(map);
                }else {
                    String listStr = (String) map.get("list");
                    List<Integer> list = JSON.parseArray(listStr, Integer.class);
                    goodServiceClient.updateGiveawayStock(list);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            });

            consumer.start();
            log.info("consumer start ...");
        } catch (Exception e) {
            log.warn("RocketMQ 消费者初始化失败，NameServer={}，服务仍正常启动，MQ功能不可用: {}", nameServer, e.getMessage());
        }
    }

}
