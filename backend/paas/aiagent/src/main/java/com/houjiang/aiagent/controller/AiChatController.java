package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiChatService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 会话接口。
 *
 * <p>注意路径：BFF 的 AiServiceFeign 调的是 {@code /ai/chat/chat}，而 BFF 对外暴露 {@code /web/ai/chat}，
 * 因此这里同时挂 {@code ""} 与 {@code "/chat"} 两个路径，避免改 Feign 才能对齐。
 *
 * <p>流式端点 {@code /ai/chat/stream} 是后台「试用对话」与前台对话的推荐路径：
 * 实测同一段营销方案同步要 60 ～ 70 秒、首字只要 2 ～ 3 秒，
 * 只把超时放大只会让人多等一分钟，必须边生成边下发。
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI 对话")
public class AiChatController {

    /** SSE 连接最长存活时间：与 AiChatService 内部 blockLast 的 10 分钟保持一致 */
    private static final long SSE_TIMEOUT_MS = 600_000L;

    /**
     * 流式生成走独立线程池，不占 Tomcat 请求线程。
     *
     * <p>🔴 容量必须给足（2026-10-29 踩过）：最初写的是固定 8 线程，一轮联调下来
     * 8 个线程全被 60 ～ 90 秒的长文案占满，后续请求静默排队——前端表现是
     * 「点了完全没反应、连一个字都不出」，而日志里一条错误都没有，极难定位。
     * 现在放到 32 核心 / 64 上限 / 32 排队，满了直接回 error 事件，
     * 让使用者知道「是忙，不是坏」。
     */
    private static final ExecutorService SSE_POOL = new ThreadPoolExecutor(
            32, 64, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(32),
            runnable -> {
                Thread thread = new Thread(runnable, "ai-chat-sse");
                thread.setDaemon(true);
                return thread;
            });

    private final AiChatService chatService;

    @PostMapping({"", "/chat"})
    public ResponseResult<Map<String, Object>> chat(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(chatService.chat(params == null ? Map.of() : params));
    }

    /**
     * 流式对话。事件约定：
     * <pre>
     * event: delta   data: 一段模型增量文本（纯文本）
     * event: done    data: { sessionNo, model, content }（整段结束，前端据此收尾）
     * event: error   data: { message }
     * </pre>
     *
     * <p>🔴 刻意不写 {@code produces = text/event-stream}：一旦声明，未登录 / 参数错这类
     * 在进入本方法之前抛出的异常就没法用 JSON 回包（实测 GlobalExceptionHandler 抛
     * HttpMediaTypeNotAcceptableException「No acceptable representation」，前端只看到空白）。
     * SseEmitter 自己就会把响应声明成 text/event-stream，这里不需要再声明一次；
     * 去掉之后错误能以既有的 JSON 错误码回给前端，正常请求仍是流式。
     */
    @PostMapping("/stream")
    public SseEmitter stream(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params == null ? Map.of() : params;
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AtomicBoolean disconnected = new AtomicBoolean(false);

        try {
            SSE_POOL.execute(() -> {
                try {
                    Map<String, Object> result = chatService.chat(body, chunk -> {
                        if (disconnected.get()) {
                            return;
                        }
                        try {
                            emitter.send(SseEmitter.event().name("delta").data(chunk, MediaType.TEXT_PLAIN));
                        } catch (Exception e) {
                            // 只是浏览器端关掉了抽屉：不打断后端生成，答案仍会完整落库到会话消息
                            disconnected.set(true);
                            log.debug("[aiagent] 流式下发中断，已停止推送 msg={}", e.getMessage());
                        }
                    });
                    if (!disconnected.get()) {
                        emitter.send(SseEmitter.event().name("done").data(result, MediaType.APPLICATION_JSON));
                    }
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("[aiagent] 流式对话异常 msg={}", e.getMessage());
                    if (!disconnected.get()) {
                        try {
                            emitter.send(SseEmitter.event().name("error")
                                    .data(Map.of("message", e.getMessage() == null ? "生成失败" : e.getMessage()),
                                            MediaType.APPLICATION_JSON));
                        } catch (Exception ignored) {
                            // 连接已断，无需再报
                        }
                    }
                    emitter.complete();
                }
            });
        } catch (RejectedExecutionException rejected) {
            log.warn("[aiagent] 流式线程池已满，拒绝新请求");
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data(Map.of("message", "当前对话请求较多，请稍后再试"), MediaType.APPLICATION_JSON));
            } catch (Exception ignored) {
                // 连 error 都发不出去就交给连接超时
            }
            emitter.complete();
        }
        return emitter;
    }

    @PostMapping("/history")
    public ResponseResult<Map<String, Object>> history(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(chatService.history(params == null ? Map.of() : params));
    }

    @PostMapping("/clear")
    public ResponseResult<Map<String, Object>> clear(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(Map.of("success", chatService.clear(params == null ? Map.of() : params)));
    }
}
