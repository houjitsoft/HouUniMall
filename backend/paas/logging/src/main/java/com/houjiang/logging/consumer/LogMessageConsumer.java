package com.houjiang.logging.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.rocketmq.config.JmsConfig;
import com.houjiang.logging.entity.LogAccessEntity;
import com.houjiang.logging.entity.LogBusinessEntity;
import com.houjiang.logging.entity.LogErrorEntity;
import com.houjiang.logging.entity.LogLoginEntity;
import com.houjiang.logging.entity.LogOperationEntity;
import com.houjiang.logging.service.LogAccessService;
import com.houjiang.logging.service.LogAlertService;
import com.houjiang.logging.service.LogBusinessService;
import com.houjiang.logging.service.LogErrorService;
import com.houjiang.logging.service.LogLoginService;
import com.houjiang.logging.service.LogOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 日志消息消费者
 *
 * <p>订阅 RocketMQ 的 {@code default_syslog_topic} 主题，消费各服务发送的日志消息，
 * 根据消息体中的 {@code logType} 字段将消息分发到对应的日志 Service 持久化到数据库。</p>
 *
 * <p>支持的 logType 值：
 * <ul>
 *   <li>OPERATION - 操作日志，存入 hou_log_operation 表</li>
 *   <li>ACCESS    - 访问日志，存入 hou_log_access 表</li>
 *   <li>LOGIN     - 登录日志，存入 hou_log_login 表</li>
 *   <li>ERROR     - 错误日志，存入 hou_log_error 表</li>
 *   <li>BUSINESS  - 业务日志，存入 hou_log_business 表</li>
 *   <li>SYSTEM    - 系统日志，复用操作日志表，module 字段标记为 "system"</li>
 * </ul>
 * 对于旧格式消息（无 logType 字段），默认按 OPERATION 类型处理，确保向后兼容。</p>
 *
 * <p>所有消费逻辑均包裹在 try-catch 中，消费失败仅记录错误日志，不抛异常，
 * 避免因单条消息异常导致消费者停止。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = JmsConfig.DEFAULT_SYSLOG_TOPIC,
        consumerGroup = "consumer_log_group",
        selectorExpression = "*"
)
public class LogMessageConsumer implements RocketMQListener<String> {

    private final LogOperationService operationService;
    private final LogAccessService accessService;
    private final LogLoginService loginService;
    private final LogErrorService errorService;
    private final LogBusinessService businessService;
    private final LogAlertService alertService;

    /**
     * 消费日志消息
     *
     * <p>解析消息体中的 JSON 数据，读取 logType 字段后分发到对应 Service 进行持久化。
     * 若 logType 不存在则默认当作 OPERATION 类型处理（向后兼容）。</p>
     *
     * @param message 消息体（JSON 字符串）
     */
    @Override
    public void onMessage(String message) {
        try {
            log.debug("[LogConsumer] 收到日志消息：{}", message);
            JSONObject json = JSON.parseObject(message);
            String logType = json.getString("logType");

            // 旧格式消息无 logType 字段，默认按操作日志处理（向后兼容）
            if (logType == null || logType.isBlank()) {
                log.debug("[LogConsumer] 消息无 logType 字段，按 OPERATION 处理");
                logType = "OPERATION";
            }

            // 新格式消息的日志数据在 data 字段中，旧格式数据在顶层，兼容两种格式
            JSONObject dataJson = json.getJSONObject("data");
            // 如果 data 字段存在且是对象，则将 logType/module 合并进去，再解析
            JSONObject parseTarget;
            if (dataJson != null && !dataJson.isEmpty()) {
                parseTarget = new JSONObject(dataJson);
                // 将 logType/module 补充进 data，确保实体字段能被填充
                if (!parseTarget.containsKey("logType")) {
                    parseTarget.put("logType", logType);
                }
                String module = json.getString("module");
                if (module != null && !parseTarget.containsKey("module")) {
                    parseTarget.put("module", module);
                }
            } else {
                // 旧格式，数据直接在顶层
                parseTarget = json;
            }

            switch (logType.toUpperCase()) {
                case "OPERATION":
                    handleOperation(parseTarget);
                    break;
                case "ACCESS":
                    handleAccess(parseTarget);
                    break;
                case "LOGIN":
                    handleLogin(parseTarget);
                    break;
                case "ERROR":
                    handleError(parseTarget);
                    break;
                case "BUSINESS":
                    handleBusiness(parseTarget);
                    break;
                case "SYSTEM":
                    handleSystem(parseTarget);
                    break;
                default:
                    log.warn("[LogConsumer] 未知的 logType={}，消息将被忽略", logType);
            }
        } catch (Exception e) {
            log.error("[LogConsumer] 消费日志消息异常，message={}，error={}", message, e.getMessage(), e);
        }
    }

    /**
     * 处理操作日志消息
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleOperation(JSONObject json) {
        try {
            LogOperationEntity entity = json.toJavaObject(LogOperationEntity.class);
            operationService.saveLog(entity);
            log.debug("[LogConsumer] 操作日志保存成功，userId={}", entity.getUserId());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存操作日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理访问日志消息
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleAccess(JSONObject json) {
        try {
            LogAccessEntity entity = json.toJavaObject(LogAccessEntity.class);
            accessService.saveLog(entity);
            log.debug("[LogConsumer] 访问日志保存成功，path={}", entity.getPath());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存访问日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理登录日志消息
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleLogin(JSONObject json) {
        try {
            LogLoginEntity entity = json.toJavaObject(LogLoginEntity.class);
            loginService.saveLog(entity);
            log.debug("[LogConsumer] 登录日志保存成功，username={}", entity.getUsername());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存登录日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理错误日志消息
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleError(JSONObject json) {
        try {
            LogErrorEntity entity = json.toJavaObject(LogErrorEntity.class);
            errorService.saveLog(entity);
            // 旁路生成运行告警：同一个问题只占一条告警，后台看到的是「什么错、谁触发的、几次」
            alertService.onError(entity);
            log.debug("[LogConsumer] 错误日志保存成功，errorType={}", entity.getErrorType());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存错误日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理业务日志消息
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleBusiness(JSONObject json) {
        try {
            LogBusinessEntity entity = json.toJavaObject(LogBusinessEntity.class);
            businessService.saveLog(entity);
            log.debug("[LogConsumer] 业务日志保存成功，module={}, action={}", entity.getModule(), entity.getAction());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存业务日志失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 处理系统日志消息
     *
     * <p>系统日志复用操作日志表，将 module 字段强制设置为 "system"。</p>
     *
     * @param json 解析后的 JSON 对象
     */
    private void handleSystem(JSONObject json) {
        try {
            LogOperationEntity entity = json.toJavaObject(LogOperationEntity.class);
            // 系统日志强制标记 module=system
            entity.setModule("system");
            operationService.saveLog(entity);
            log.debug("[LogConsumer] 系统日志保存成功，operation={}", entity.getOperation());
        } catch (Exception e) {
            log.error("[LogConsumer] 保存系统日志失败：{}", e.getMessage(), e);
        }
    }
}
