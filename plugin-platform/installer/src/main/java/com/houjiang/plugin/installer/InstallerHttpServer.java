package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** HTTP 层不记录请求、认证头、异常链或票据；令牌与实例 ID 均以固定长度摘要恒时比较。 */
public final class InstallerHttpServer implements AutoCloseable {
    public static final int MAX_REQUEST_BYTES = 131072;
    private final InstallerAgent agent;
    private final HttpServer server;
    private final ExecutorService executor;
    private final byte[] authorizationHash;
    private final byte[] instanceHash;

    /** agent 的生命周期由调用方管理。token 不保存在配置或日志中。 */
    public InstallerHttpServer(JsonNode config, InstallerAgent agent, String token) throws Exception {
        if (token == null || token.codePointCount(0, token.length()) < 32) throw InstallerAgent.failure("installer token must be configured with at least 32 characters");
        String instance = InstallerAgent.text(config, "instanceId");
        if (instance.isEmpty()) throw InstallerAgent.failure("instanceId must be configured");
        this.agent = agent;
        this.authorizationHash = hash("Bearer " + token);
        this.instanceHash = hash(instance);
        JsonNode port = config.path("port");
        if (!port.isMissingNode() && (!port.isIntegralNumber() || !port.canConvertToInt() || port.intValue() < 0 || port.intValue() > 65535)) throw InstallerAgent.failure("invalid server port");
        server = HttpServer.create(new InetSocketAddress(config.path("bind").asText("127.0.0.1"), port.asInt(8420)), 128);
        executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }

    public void start() { server.start(); }
    public InetSocketAddress address() { return server.getAddress(); }

    private void handle(HttpExchange exchange) {
        try (exchange) {
            boolean authorization = matches(exchange.getRequestHeaders().get("Authorization"), authorizationHash);
            boolean instance = matches(exchange.getRequestHeaders().get("X-UniMall-Instance"), instanceHash);
            if (!(authorization & instance)) { sendError(exchange, 403, "Forbidden"); return; }
            String method = exchange.getRequestMethod();
            String target = exchange.getRequestURI().toString();
            JsonNode result;
            try {
                if ("GET".equals(method) && "/tasks".equals(target)) result = agent.history();
                else if ("GET".equals(method) && target.matches("/tasks/[a-f0-9-]{36}")) result = agent.status(target.substring(7));
                else if ("POST".equals(method) && "/tasks".equals(target)) {
                    List<String> lengths = exchange.getRequestHeaders().get("Content-Length");
                    if (lengths == null || lengths.size() != 1 || exchange.getRequestHeaders().containsKey("Transfer-Encoding")) throw InstallerAgent.failure("invalid request size");
                    int length;
                    try { length = Integer.parseInt(lengths.get(0)); }
                    catch (NumberFormatException error) { throw InstallerAgent.failure("invalid request size"); }
                    if (length <= 0 || length > MAX_REQUEST_BYTES) throw InstallerAgent.failure("invalid request size");
                    byte[] body;
                    try (InputStream input = exchange.getRequestBody()) { body = input.readNBytes(length); }
                    if (body.length != length) throw InstallerAgent.failure("invalid request size");
                    JsonNode bundle;
                    try { bundle = InstallerAgent.JSON.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(body); }
                    catch (Exception error) { throw InstallerAgent.failure("invalid JSON request"); }
                    if (bundle == null || !bundle.isObject()) throw InstallerAgent.failure("invalid JSON request");
                    result = agent.submit(bundle);
                } else { sendError(exchange, 404, "Not Found"); return; }
                send(exchange, 200, InstallerAgent.JSON.writeValueAsBytes(result));
            } catch (Exception error) { sendError(exchange, 400, InstallerAgent.safeMessage(error)); }
        } catch (Exception ignored) {
            // 客户端断开连接不能导致异常内容进入默认日志。
        }
    }

    private static byte[] hash(String value) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean matches(List<String> values, byte[] expected) throws Exception {
        String value = values != null && values.size() == 1 ? values.get(0) : "";
        boolean equal = MessageDigest.isEqual(expected, hash(value));
        return equal & (values != null && values.size() == 1);
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws Exception {
        send(exchange, status, InstallerAgent.JSON.writeValueAsBytes(InstallerAgent.JSON.createObjectNode().put("error", message)));
    }

    private static void send(HttpExchange exchange, int status, byte[] body) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    @Override public void close() {
        server.stop(1);
        executor.shutdownNow();
    }
}
