package com.houjiang.commons.rocketmq.config;

import org.apache.rocketmq.common.message.Message;

/**
 * RocketMQ 消息配置类
 * <p>
 * 集中管理 Topic、Tag、ProducerGroup、ConsumerGroup 常量，
 * 以及各类消息的构建工具方法。
 * </p>
 *
 * @author Amos
 * @since 2023/4/10
 */
public class JmsConfig {

    /**
     * 指定NameServer地址，多个地址以 ; 隔开
     * public static final String NAME_SERVER = "192.168.100.141:9876;192.168.100.142:9876;192.168.100.149:9876";
     */

    public static final String DEFAULT_TOPIC = "default_topic_110";

    public static final String DEFAULT_ORDERLY_TOPIC = "default_orderly_topic_110";

    public static final String DEFAULT_TRANSACTION_TOPIC = "default_transaction_topic_110";

    public static final String DEFAULT_TAGS = "default_tags";


    public static final String DEFAULT_ES_TOPIC = "default_es_topic";

    public static final String DEFAULT_SYSLOG_TOPIC = "default_syslog_topic";

    /** producerGroup */

    public static String PRODUCER_GROUP = "simple_producer_group";

    public static String ORDERLY_PRODUCER_GROUP = "orderly_producer_group";

    public static String TRANSACTION_PRODUCER_GROUP = "transaction_producer_group";

    public static String PRODUCER_GOODS_ES_GROUP = "goods_es_group";

    public static String PRODUCER_SYS_LOG_GROUP = "sys_log_group";

    /** consumerGroup */
    public static String CONSUMER_GOODS_ES_GROUP = "consumer_goods_es_group";

    public static String CONSUMER_SYS_LOG_GROUP = "consumer_log_group";

    public static String CONSUMER_GROUP = "simple_consumer_group";

    public static String CONSUMER_ORDERLY_GROUP = "orderly_consumer_group";

    public static String CONSUMER_TRANSACTION_GROUP = "transaction_consumer_group";

    // ====== 日志 Tag 常量 ======

    /** 操作日志 Tag */
    public static final String LOG_TAG_OPERATION = "LOG_OPERATION";

    /** 访问日志 Tag */
    public static final String LOG_TAG_ACCESS = "LOG_ACCESS";

    /** 登录日志 Tag */
    public static final String LOG_TAG_LOGIN = "LOG_LOGIN";

    /** 错误日志 Tag */
    public static final String LOG_TAG_ERROR = "LOG_ERROR";

    /** 业务日志 Tag */
    public static final String LOG_TAG_BUSINESS = "LOG_BUSINESS";

    /** 系统日志 Tag */
    public static final String LOG_TAG_SYSTEM = "LOG_SYSTEM";

    public static class Utils {
        /**
         * 普通消息
         * @param msg
         * @return
         */
        public static Message buildDefaultMessage (String msg) {
            Message message = new Message(JmsConfig.DEFAULT_ES_TOPIC,DEFAULT_TAGS, msg.getBytes());
            return message;
        }

        /**
         * 队列消息
         * @param msg
         * @return
         */
        public static Message buildOrderlyMessage(String msg) {
            Message message = new Message(JmsConfig.DEFAULT_ORDERLY_TOPIC,DEFAULT_TAGS, msg.getBytes());
            return message;
        }

        /**
         * 事务消息
         * @param msg
         * @return
         */
        public static Message buildTransactionMessage(String msg) {
            Message message = new Message(JmsConfig.DEFAULT_TRANSACTION_TOPIC,DEFAULT_TAGS, msg.getBytes());
            return message;
        }

        /**
         * es消息
         * @param msg
         * @return
         */
        public static Message buildEsMessage(String msg) {
            Message message = new Message(JmsConfig.DEFAULT_ES_TOPIC,DEFAULT_TAGS, msg.getBytes());
            return message;
        }

        /**
         * 操作日志消息（兼容旧版，Tag 使用 DEFAULT_TAGS）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildSysLogMessage(String msg) {
            Message message = new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, DEFAULT_TAGS, msg.getBytes());
            return message;
        }

        /**
         * 构建带指定 Tag 的日志消息
         * <p>使用日志类型对应的 Tag，便于消费端按类型过滤</p>
         *
         * @param msg    消息内容（JSON 字符串）
         * @param logTag 日志 Tag（使用 JmsConfig.LOG_TAG_* 常量）
         * @return RocketMQ Message 对象
         */
        public static Message buildLogMessageWithTag(String msg, String logTag) {
            Message message = new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, logTag, msg.getBytes());
            return message;
        }

        /**
         * 构建操作日志消息（LOG_OPERATION Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildOperationLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_OPERATION, msg.getBytes());
        }

        /**
         * 构建访问日志消息（LOG_ACCESS Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildAccessLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_ACCESS, msg.getBytes());
        }

        /**
         * 构建登录日志消息（LOG_LOGIN Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildLoginLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_LOGIN, msg.getBytes());
        }

        /**
         * 构建错误日志消息（LOG_ERROR Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildErrorLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_ERROR, msg.getBytes());
        }

        /**
         * 构建业务日志消息（LOG_BUSINESS Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildBusinessLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_BUSINESS, msg.getBytes());
        }

        /**
         * 构建系统日志消息（LOG_SYSTEM Tag）
         *
         * @param msg 消息内容（JSON 字符串）
         * @return RocketMQ Message 对象
         */
        public static Message buildSystemLogMessage(String msg) {
            return new Message(JmsConfig.DEFAULT_SYSLOG_TOPIC, LOG_TAG_SYSTEM, msg.getBytes());
        }
    }
}
