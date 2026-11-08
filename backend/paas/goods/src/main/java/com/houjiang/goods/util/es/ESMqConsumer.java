package com.houjiang.goods.util.es;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.shop.ArticleDTO;
import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.commons.rocketmq.config.RocketMqClientFactory;
import com.houjiang.goods.mapper.GoodsBaseMapper;
import com.houjiang.goods.mapper.GoodsCommonAttributeAssociationMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/8 16:26
 */
@Slf4j
@Component
@RefreshScope
public class ESMqConsumer {
    private DefaultMQPushConsumer consumer;

    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Value("${rocketmq.consumer.access-key:}")
    private String accessKey;
    @Value("${rocketmq.consumer.secret-key:}")
    private String secretKey;
    @Autowired
    private GoodsBaseMapper goodsBaseMapper;
    @Autowired
    private GoodsCommonAttributeAssociationMapper goodsCommonAttributeAssociationMapper;

    @PostConstruct
    public void ini() throws MQClientException {

        consumer = RocketMqClientFactory.createPushConsumer(JmsConfig.DEFAULT_ES_TOPIC, accessKey, secretKey);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 默认是集群方式，可以更改为广播，但是广播方式不支持重试
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.subscribe(JmsConfig.DEFAULT_ES_TOPIC, "*");


        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            MessageExt msg = msgs.getFirst();

            int times = msg.getReconsumeTimes();
            if (times > 0) {
                log.info("重试次数=" + times);
            }
            String topic = msg.getTopic();
            String body = new String(msg.getBody(), StandardCharsets.UTF_8);
            String tags = msg.getTags();
            String keys = msg.getKeys();
            log.info("消费消息:" + "topic=" + topic + ", tags=" + tags + ", keys=" + keys + ", msg=" + body);
            Map<String, Object> map = JSON.parseObject(body);
            Integer type = (Integer) map.get("type");
            String str = map.get("commonIdS").toString().replace("[", "").replace("]", "");
            String[] list = str.split(",");
            if (CommonType.operateStatus.ADD.getCode().equals(type)) {
                List<ArticleDTO> articles = new ArrayList<>();
                for (String s : list) {
                    System.out.println(12);
                    //封装商品信息
                    ArticleDTO articleDTOS = goodsBaseMapper.selectEsGoodsbyId(s);
                    if (null != articleDTOS) {
                        String strAttributeContentIds = null;
                        List<String> attributeContentIds = goodsCommonAttributeAssociationMapper.queryGoodsAttributeContent(articleDTOS.getCommonId());
                        if (null != attributeContentIds && !attributeContentIds.isEmpty()) {
                            for (String attributeContentId : attributeContentIds) {
                                attributeContentId = "-" + attributeContentId + "-,";
                                if (null == strAttributeContentIds) {
                                    strAttributeContentIds = attributeContentId;
                                } else {
                                    strAttributeContentIds = strAttributeContentIds + attributeContentId;
                                }
                            }
                            articleDTOS.setAttributeContentId(strAttributeContentIds);
                        }
                        articles.add(articleDTOS);
                    }
                }
                //更新es的商品数据
                ESUtils.addOrUpdate(articles);
            } else {
                //删除es数据
                List<Integer> list2 = new ArrayList<>();
                for (String s : list) {
                    list2.add(Integer.valueOf(s));
                }
                ESUtils.delete(list2);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("consumer start ...");
    }

}
