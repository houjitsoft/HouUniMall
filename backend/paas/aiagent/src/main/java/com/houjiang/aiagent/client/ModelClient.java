package com.houjiang.aiagent.client;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Flux;

/**
 * 模型调用门面。
 *
 * <p>这是整个 aiagent 最重要的架构护栏：业务代码只依赖这个接口，不依赖任何具体框架。
 * 目前实现是 Spring AI 的 OpenAI 兼容客户端（base-url 指向自建 aigateway）；
 * 将来要换成 Spring AI Alibaba Graph、LangChain4j 或别的方案，只替换实现类，业务代码零改动。
 */
public interface ModelClient {

    /**
     * 非流式对话。
     */
    ModelChatResult chat(ModelChatRequest request);

    /**
     * 流式对话，按文本增量推送。
     */
    Flux<String> chatStream(ModelChatRequest request);

    /**
     * 网关与模型可用性探测。用于后台「测试连接」与启动自检。
     */
    ModelHealth health();

    /**
     * 当前生效的网关地址与逻辑模型名，供后台展示/排错。
     */
    ModelEndpoint endpoint();

    @Data
    @Accessors(chain = true)
    class ModelChatRequest {
        /** 逻辑模型名，如 mall-chat；为空时取配置默认 */
        private String model;
        private String systemPrompt;
        private String userPrompt;
        private Double temperature;
        private Integer maxTokens;
        /** 业务场景，用于用量归因（text_generate / chat …） */
        private String scene;
    }

    @Data
    @Accessors(chain = true)
    class ModelChatResult {
        private boolean success;
        private String content;
        private String model;
        private String errorCode;
        private String errorMessage;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

    @Data
    @Accessors(chain = true)
    class ModelHealth {
        private boolean available;
        private String message;
    }

    @Data
    @Accessors(chain = true)
    class ModelEndpoint {
        private String baseUrl;
        private String model;
        private boolean configured;
    }
}
