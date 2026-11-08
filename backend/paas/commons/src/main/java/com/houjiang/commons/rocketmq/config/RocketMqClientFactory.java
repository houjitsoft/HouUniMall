package com.houjiang.commons.rocketmq.config;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.RPCHook;

/**
 * Creates native RocketMQ clients with optional ACL authentication.
 *
 * <p>Both credentials may be omitted for local/open-source installations that
 * do not enable ACL. Supplying only one credential is treated as a
 * configuration error so a production service cannot silently downgrade to an
 * unauthenticated connection.</p>
 */
public final class RocketMqClientFactory {

    private RocketMqClientFactory() {
    }

    public static DefaultMQProducer createProducer(String producerGroup, String accessKey, String secretKey) {
        RPCHook rpcHook = createRpcHook(accessKey, secretKey);
        return rpcHook == null
                ? new DefaultMQProducer(producerGroup)
                : new DefaultMQProducer(producerGroup, rpcHook);
    }

    public static DefaultMQPushConsumer createPushConsumer(String consumerGroup, String accessKey, String secretKey) {
        RPCHook rpcHook = createRpcHook(accessKey, secretKey);
        return rpcHook == null
                ? new DefaultMQPushConsumer(consumerGroup)
                : new DefaultMQPushConsumer(consumerGroup, rpcHook);
    }

    static RPCHook createRpcHook(String accessKey, String secretKey) {
        String normalizedAccessKey = normalize(accessKey);
        String normalizedSecretKey = normalize(secretKey);
        boolean hasAccessKey = normalizedAccessKey != null;
        boolean hasSecretKey = normalizedSecretKey != null;

        if (hasAccessKey != hasSecretKey) {
            throw new IllegalArgumentException(
                    "RocketMQ ACL configuration requires both access-key and secret-key");
        }
        if (!hasAccessKey) {
            return null;
        }
        return new AclClientRPCHook(new SessionCredentials(normalizedAccessKey, normalizedSecretKey));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
