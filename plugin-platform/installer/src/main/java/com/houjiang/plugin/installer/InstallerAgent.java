package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** 独立安装代理。所有公开任务返回值均为调用方独享的 JSON 快照。 */
public class InstallerAgent implements AutoCloseable {
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(600);
    private static final Set<String> PAID = Set.of("crm", "cms", "erp", "finance", "geo", "im", "logging", "mes", "oa", "orderfood", "pm", "rider", "supplier", "webpos");
    private static final String COLUMNS = "id,code,version,state,step,result,created,image_id,source_sha256,runtime_sha256";
    private static final String[] FIELDS = {"taskId", "pluginCode", "version", "state", "step", "result", "created", "imageId", "sourceSha256", "runtimeSha256"};
    private final JsonNode config;
    private final Path root;
    private final String jdbcUrl;
    private final CommandRunner runner;
    private final DownloadTransport transport;
    private final HealthWaiter healthWaiter;
    private final ExecutorService workers;
    private final FileChannel lockChannel;
    private final FileLock processLock;
    private boolean closed;

    /** 测试传输也必须禁止重定向，返回的流由代理关闭；代理自身负责摘要、大小与缓存。 */
    @FunctionalInterface
    public interface DownloadTransport {
        InputStream open(URI uri, Duration timeout) throws Exception;
    }

    /** 可替换健康检查的轮询等待，测试可传入无等待实现。 */
    @FunctionalInterface
    public interface HealthWaiter {
        void await(Duration duration) throws Exception;
    }

    public InstallerAgent(JsonNode config, CommandRunner runner) throws Exception {
        this(config, runner, InstallerAgent::openDownload, duration -> Thread.sleep(duration.toMillis()),
                Executors.newSingleThreadExecutor());
    }

    public InstallerAgent(JsonNode config) throws Exception {
        this(config, new ProcessCommandRunner());
    }

    /** 传入的执行器归代理所有；可使用测试执行器控制排队，close 会等待所有已提交任务完成。 */
    public InstallerAgent(JsonNode config, CommandRunner runner, DownloadTransport transport,
                          HealthWaiter healthWaiter, ExecutorService workers) throws Exception {
        this.config = config.deepCopy();
        this.runner = java.util.Objects.requireNonNull(runner);
        this.transport = java.util.Objects.requireNonNull(transport);
        this.healthWaiter = java.util.Objects.requireNonNull(healthWaiter);
        this.workers = java.util.Objects.requireNonNull(workers);
        FileChannel channel = null;
        FileLock acquired = null;
        try {
            Path configured = Path.of(text(this.config, "root")).toAbsolutePath().normalize();
            Files.createDirectories(configured);
            this.root = configured.toRealPath();
            this.jdbcUrl = "jdbc:sqlite:" + root.resolve("tasks.sqlite");
            channel = FileChannel.open(root.resolve("agent.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            try {
                acquired = channel.tryLock();
            } catch (OverlappingFileLockException error) {
                throw failure("another installer agent owns this root");
            }
            if (acquired == null) throw failure("another installer agent owns this root");
            initializeDatabase();
            this.lockChannel = channel;
            this.processLock = acquired;
        } catch (Exception error) {
            if (acquired != null) acquired.release();
            if (channel != null) channel.close();
            workers.shutdownNow();
            throw error;
        }
    }

    public Path root() { return root; }

    private Connection connect() throws Exception {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout=10000");
        } catch (Exception error) {
            connection.close();
            throw error;
        }
        return connection;
    }

    private void initializeDatabase() throws Exception {
        try (Connection db = connect()) {
            db.setAutoCommit(false);
            try (Statement statement = db.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS tasks(id TEXT PRIMARY KEY,code TEXT,version TEXT,state TEXT,step TEXT,result TEXT,created INTEGER)");
                Set<String> columns = new HashSet<>();
                try (ResultSet rows = statement.executeQuery("PRAGMA table_info(tasks)")) {
                    while (rows.next()) columns.add(rows.getString("name"));
                }
                for (String column : List.of("image_id", "source_sha256", "runtime_sha256")) {
                    if (!columns.contains(column)) statement.execute("ALTER TABLE tasks ADD COLUMN " + column + " TEXT");
                }
                statement.executeUpdate("UPDATE tasks SET state='FAILED',step='INTERRUPTED',result='Agent restarted; inspect containers before retry' WHERE state IN ('PENDING','RUNNING')");
                db.commit();
            } catch (Exception error) {
                db.rollback();
                throw error;
            }
        }
    }

    /** 验证原始 payload 的 Ed25519 签名，不对 JSON 重新序列化。公钥为 OpenSSL SPKI PEM 文件。 */
    public JsonNode verify(JsonNode envelope) throws Exception {
        String encoded = text(envelope, "payload");
        if (encoded.length() > 87384) throw failure("release manifest too large");
        byte[] payload = decode(encoded);
        if (payload.length > 65536) throw failure("release manifest too large");
        JsonNode key = config.path("publicKeys").path(text(envelope, "kid"));
        if (!key.isTextual() || key.textValue().isEmpty()) throw failure("unknown official signing key");
        try {
            String pem = Files.readString(Path.of(key.textValue()), StandardCharsets.US_ASCII).strip();
            String header = "-----BEGIN PUBLIC KEY-----", footer = "-----END PUBLIC KEY-----";
            if (!pem.startsWith(header) || !pem.endsWith(footer)) throw failure("Signature verification failed");
            byte[] der = Base64.getDecoder().decode(pem.substring(header.length(), pem.length() - footer.length()).replaceAll("\\s", ""));
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(der)));
            signature.update(payload);
            byte[] signed = decode(text(envelope, "signature"));
            if (signed.length != 64 || !signature.verify(signed)) throw failure("Signature verification failed");
        } catch (Exception error) {
            throw failure("Signature verification failed");
        }
        JsonNode manifest;
        try { manifest = JSON.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(payload); }
        catch (Exception error) { throw failure("invalid release manifest"); }
        String code = text(manifest, "pluginCode"), version = text(manifest, "version");
        if (!PAID.contains(code) || !code.equals(text(manifest, "hostService")) ||
                !"standalone".equals(text(manifest, "runtime")) || !"Apache-2.0".equals(text(manifest, "sourceLicense"))) {
            throw failure("wrong service ownership or artifact format");
        }
        if (!version.matches("[0-9]+\\.[0-9]+\\.[0-9]+(?:-[A-Za-z0-9.-]+)?")) throw failure("invalid version");
        if (!("unimall-" + code + ":" + version).equals(text(manifest, "image")) ||
                !text(manifest, "imageId").matches("sha256:[a-f0-9]{64}")) throw failure("invalid immutable image identity");
        long max = config.path("maxArtifactBytes").asLong(8589934592L);
        for (String kind : List.of("source", "runtime")) {
            JsonNode size = manifest.path(kind + "Size");
            if (!text(manifest, kind + "Sha256").matches("[a-f0-9]{64}") || !size.isIntegralNumber() ||
                    !size.canConvertToLong() || size.longValue() <= 0 || size.longValue() > max) throw failure("invalid artifact identity");
        }
        if (!config.path("services").has(code)) throw failure("service deployment configuration is missing");
        return manifest;
    }

    private static byte[] decode(String encoded) {
        try {
            if (encoded.length() % 4 != 0 || !encoded.matches("[A-Za-z0-9+/]*={0,2}")) throw failure("invalid base64");
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException error) { throw failure("invalid base64"); }
    }

    public synchronized JsonNode submit(JsonNode request) throws Exception {
        if (closed) throw failure("Installation worker unavailable");
        JsonNode bundle = request.deepCopy();
        JsonNode manifest = verify(bundle.path("release"));
        String code = text(manifest, "pluginCode");
        serviceConfig(code);
        String task;
        try (Connection db = connect()) {
            db.setAutoCommit(false);
            try {
                try (PreparedStatement query = db.prepareStatement("SELECT " + COLUMNS + " FROM tasks WHERE code=? AND state IN ('PENDING','RUNNING')")) {
                    query.setString(1, code);
                    try (ResultSet rows = query.executeQuery()) {
                        if (rows.next()) {
                            ObjectNode active = row(rows);
                            for (String field : List.of("version", "imageId", "sourceSha256", "runtimeSha256")) {
                                if (!active.path(field).equals(manifest.path(field))) throw failure("another release of this service is already being installed");
                            }
                            db.commit();
                            return active;
                        }
                    }
                }
                task = UUID.randomUUID().toString();
                try (PreparedStatement insert = db.prepareStatement("INSERT INTO tasks (" + COLUMNS + ") VALUES(?,?,?,?,?,?,?,?,?,?)")) {
                    insert.setString(1, task); insert.setString(2, code); insert.setString(3, text(manifest, "version"));
                    insert.setString(4, "PENDING"); insert.setString(5, "QUEUED"); insert.setString(6, "");
                    insert.setLong(7, System.currentTimeMillis() / 1000);
                    insert.setString(8, text(manifest, "imageId")); insert.setString(9, text(manifest, "sourceSha256"));
                    insert.setString(10, text(manifest, "runtimeSha256")); insert.executeUpdate();
                }
                db.commit();
            } catch (Exception error) { db.rollback(); throw error; }
        }
        try { workers.submit(() -> install(task, bundle, manifest)); }
        catch (Exception error) {
            update(task, "FAILED", "QUEUE", "Installation worker unavailable");
            throw failure("Installation worker unavailable");
        }
        return status(task);
    }

    public JsonNode status(String task) throws Exception {
        try (Connection db = connect(); PreparedStatement query = db.prepareStatement("SELECT " + COLUMNS + " FROM tasks WHERE id=?")) {
            query.setString(1, task);
            try (ResultSet rows = query.executeQuery()) {
                if (!rows.next()) throw failure("unknown task");
                return row(rows);
            }
        }
    }

    public ArrayNode history() throws Exception {
        ArrayNode result = JSON.createArrayNode();
        try (Connection db = connect(); Statement query = db.createStatement();
             ResultSet rows = query.executeQuery("SELECT " + COLUMNS + " FROM tasks ORDER BY created DESC LIMIT 100")) {
            while (rows.next()) result.add(row(rows));
        }
        return result;
    }

    private static ObjectNode row(ResultSet rows) throws Exception {
        ObjectNode result = JSON.createObjectNode();
        for (int i = 0; i < FIELDS.length; i++) {
            if (i == 6) result.put(FIELDS[i], rows.getLong(i + 1));
            else result.put(FIELDS[i], rows.getString(i + 1));
        }
        return result;
    }

    public void update(String task, String state, String step, String result) throws Exception {
        try (Connection db = connect(); PreparedStatement update = db.prepareStatement("UPDATE tasks SET state=?,step=?,result=? WHERE id=?")) {
            update.setString(1, state); update.setString(2, step); update.setString(3, result); update.setString(4, task);
            update.executeUpdate();
        }
    }

    private void update(String task, String state, String step) throws Exception { update(task, state, step, ""); }

    public static void validateCommand(List<String> command, String message) {
        if (command == null || command.isEmpty() || command.stream().anyMatch(s -> s == null || s.isBlank() || s.indexOf('\0') >= 0)) throw failure(message);
    }

    private static List<String> commandArray(JsonNode value, String message) {
        if (!value.isArray() || value.isEmpty()) throw failure(message);
        List<String> result = new ArrayList<>();
        for (JsonNode part : value) {
            if (!part.isTextual()) throw failure(message);
            result.add(part.textValue());
        }
        validateCommand(result, message);
        return result;
    }

    private record ServiceConfig(Path env, String network, double timeout, List<String> registration, List<String> preflight) { }

    private ServiceConfig serviceConfig(String code) throws Exception {
        JsonNode cfg = config.path("services").path(code);
        List<String> registration = commandArray(cfg.path("registrationCheck"), "registration and business API check is required");
        List<String> preflight = commandArray(cfg.path("upgradePreflight"), "upgrade requires backup and schema compatibility preflight");
        Path env = Path.of(text(cfg, "envFile")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(env)) throw failure("service env file missing");
        env = env.toRealPath();
        String network = text(cfg, "network");
        if (!network.matches("[A-Za-z0-9][A-Za-z0-9_.-]*")) throw failure("service Docker network is required");
        JsonNode seconds = cfg.path("healthTimeoutSeconds");
        double timeout = seconds.isMissingNode() ? 180 : seconds.asDouble(Double.NaN);
        if ((!seconds.isMissingNode() && !seconds.isNumber()) || !Double.isFinite(timeout) || timeout <= 0) throw failure("health timeout must be positive and finite");
        return new ServiceConfig(env, network, timeout, registration, preflight);
    }

    protected String run(List<String> args) throws Exception { return runner.run(List.copyOf(args), COMMAND_TIMEOUT); }

    public JsonNode inspect(String name) throws Exception {
        try { return firstObject(run(List.of("docker", "inspect", name))); }
        catch (CommandException error) {
            String message = error.stderr().strip().toLowerCase(Locale.ROOT);
            if (message.contains("no such object:") || message.contains("no such container:")) return null;
            throw error;
        }
    }

    private static JsonNode firstObject(String output) throws Exception {
        JsonNode value = JSON.readTree(output);
        if (value == null || !value.isArray() || value.isEmpty() || !value.get(0).isObject()) throw failure("invalid container inspection response");
        return value.get(0);
    }

    public static boolean ownedByTask(JsonNode container, String code, String task) {
        JsonNode labels = container.path("Config").path("Labels");
        return code.equals(labels.path("unimall.service").asText()) && task.equals(labels.path("unimall.task").asText());
    }

    public void removeCandidate(String name, String code, String task) throws Exception {
        JsonNode container = inspect(name);
        if (container != null) {
            if (!ownedByTask(container, code, task)) throw failure("container ownership changed; manual recovery required");
            run(List.of("docker", "rm", "-f", text(container, "Id")));
        }
    }

    private String restorePrevious(String name, JsonNode old, String code, String task, ServiceConfig cfg) throws Exception {
        JsonNode current = inspect(name);
        if (current != null && (old == null || !text(current, "Id").equals(text(old, "Id")))) {
            if (!ownedByTask(current, code, task)) throw failure("container ownership changed; manual recovery required");
            run(List.of("docker", "rm", "-f", text(current, "Id")));
        }
        if (old == null) return "FAILED";
        JsonNode previous = inspect(text(old, "Id"));
        if (previous == null) throw failure("previous container is missing");
        if (!text(previous, "Name").replaceFirst("^/+", "").equals(name)) run(List.of("docker", "rename", text(old, "Id"), name));
        if (old.path("State").path("Running").asBoolean()) {
            run(List.of("docker", "start", text(old, "Id")));
            health(name, cfg.timeout());
            String version = old.path("Config").path("Labels").path("unimall.version").asText("");
            if (version.isEmpty()) throw failure("previous version missing; verify service registration manually");
            run(append(cfg.registration(), code, version));
        }
        return "ROLLED_BACK";
    }

    /** 由单线程队列调用；测试可用已有持久化 task 直接调用。bundle/manifest 必须已通过 verify。 */
    public void install(String task, JsonNode bundle, JsonNode manifest) {
        String code = text(manifest, "pluginCode"), version = text(manifest, "version");
        String name = "unimall-" + code, candidate = name + "-candidate", oldName = name + "-previous-" + task;
        JsonNode old = null;
        ServiceConfig cfg = null;
        boolean candidateAttempted = false, switchAttempted = false;
        try {
            cfg = serviceConfig(code);
            old = inspect(name);
            if (old != null && !code.equals(old.path("Config").path("Labels").path("unimall.service").asText())) throw failure("existing container is not managed by this installer");
            if (inspect(candidate) != null) throw failure("stale candidate exists; review previous task before retry");
            if (old != null && inspect(oldName) != null) throw failure("previous container name already exists");
            update(task, "RUNNING", "DOWNLOAD");
            download(bundle.path("source"), root.resolve("sources").resolve(code).resolve(version).resolve("source.tar.gz"), text(manifest, "sourceSha256"), manifest.path("sourceSize").longValue());
            Path runtime = root.resolve("runtime").resolve(code).resolve(version).resolve("image.tar");
            download(bundle.path("runtime"), runtime, text(manifest, "runtimeSha256"), manifest.path("runtimeSize").longValue());
            update(task, "RUNNING", "VERIFY");
            run(List.of("docker", "image", "load", "--input", runtime.toString()));
            JsonNode image = firstObject(run(List.of("docker", "image", "inspect", text(manifest, "image"))));
            if (!text(image, "Id").equals(text(manifest, "imageId"))) throw failure("loaded image differs from signed image identity");
            JsonNode health = image.path("Config").path("Healthcheck").path("Test");
            if (!health.isArray() || health.isEmpty() || "NONE".equals(health.path(0).asText())) throw failure("official image must define HEALTHCHECK");
            if (old != null) {
                update(task, "RUNNING", "BACKUP");
                run(append(cfg.preflight(), version));
            }
            List<String> common = List.of("--restart", "unless-stopped", "--network", cfg.network(), "--env-file", cfg.env().toString(),
                    "--label", "unimall.service=" + code, "--label", "unimall.version=" + version, "--label", "unimall.task=" + task,
                    "--env", "SPRING_APPLICATION_NAME=" + code);
            update(task, "RUNNING", "START");
            candidateAttempted = true;
            List<String> candidateCommand = new ArrayList<>(List.of("docker", "run", "-d", "--name", candidate));
            candidateCommand.addAll(common);
            candidateCommand.addAll(List.of("--env", "SPRING_CLOUD_NACOS_DISCOVERY_REGISTER_ENABLED=false", text(manifest, "imageId")));
            run(candidateCommand);
            health(candidate, cfg.timeout());
            removeCandidate(candidate, code, task);
            candidateAttempted = false;
            switchAttempted = true;
            if (old != null) {
                run(List.of("docker", "stop", name));
                run(List.of("docker", "rename", name, oldName));
            }
            update(task, "RUNNING", "ACTIVATE");
            List<String> activation = new ArrayList<>(List.of("docker", "run", "-d", "--name", name));
            activation.addAll(common); activation.add(text(manifest, "imageId"));
            run(activation);
            health(name, cfg.timeout());
            run(append(cfg.registration(), code, version));
            ObjectNode result = JSON.createObjectNode().put("container", name).put("imageId", text(manifest, "imageId"))
                    .put("sourceSha256", text(manifest, "sourceSha256")).put("runtimeSha256", text(manifest, "runtimeSha256"));
            update(task, "SUCCESS", "HEALTH", JSON.writeValueAsString(result));
        } catch (Exception error) {
            String message = safeMessage(error);
            if (candidateAttempted) {
                try { removeCandidate(candidate, code, task); }
                catch (Exception recovery) { message += "; candidate needs manual recovery"; }
            }
            String state = "FAILED";
            if (switchAttempted) {
                try { state = restorePrevious(name, old, code, task, cfg); }
                catch (Exception recovery) { message += "; previous container needs manual recovery"; }
            }
            try { update(task, state, "FAILED", message); }
            catch (Exception persistence) {
                // 不记录异常链：第三方异常可能包含下载票据或命令输出。
                System.err.println("Installer task state could not be persisted; inspect host state before retry");
            }
        }
    }

    public void health(String name, double seconds) throws Exception {
        long started = System.nanoTime();
        while ((System.nanoTime() - started) / 1_000_000_000.0 < seconds) {
            JsonNode state = inspect(name);
            if (state != null && "healthy".equals(state.path("State").path("Health").path("Status").asText())) return;
            if (state != null && Set.of("exited", "dead").contains(state.path("State").path("Status").asText())) break;
            double remaining = seconds - (System.nanoTime() - started) / 1_000_000_000.0;
            if (remaining > 0) healthWaiter.await(Duration.ofMillis(Math.max(1, (long) (Math.min(2, remaining) * 1000))));
        }
        throw failure("container health check did not pass");
    }

    /** 只接受受信 origin 上的相对票据地址；流式校验，失败总是移除 .part。 */
    public void download(JsonNode record, Path destination, String sha, long size) throws Exception {
        String url = text(record, "url");
        if (!url.startsWith("/base/plugin-center/artifact/raw?token=") || url.indexOf('\r') >= 0 || url.indexOf('\n') >= 0 || url.indexOf('#') >= 0) throw failure("invalid artifact URL");
        URI uri;
        try {
            URI origin = URI.create(text(config, "catalogOrigin").replaceAll("/+$", ""));
            if (!("https".equals(origin.getScheme()) || "http".equals(origin.getScheme())) || origin.getHost() == null ||
                    origin.getRawUserInfo() != null || origin.getRawQuery() != null || origin.getRawFragment() != null ||
                    (origin.getRawPath() != null && !origin.getRawPath().isEmpty())) throw failure("catalog origin is not configured");
            URI relative = URI.create(url);
            if (relative.isAbsolute() || relative.getRawAuthority() != null || !"/base/plugin-center/artifact/raw".equals(relative.getRawPath())) throw failure("invalid artifact URL");
            uri = origin.resolve(relative);
        } catch (SafeException error) { throw error; }
        catch (Exception error) { throw failure("invalid artifact URL or catalog origin"); }
        if (Files.isRegularFile(destination) && Files.size(destination) == size && digest(destination).equals(sha)) return;
        Files.createDirectories(destination.toAbsolutePath().getParent());
        Path partial = destination.resolveSibling(destination.getFileName() + ".part");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        long count = 0;
        long started = System.nanoTime();
        try {
            try (InputStream input = transport.open(uri, Duration.ofSeconds(120)); OutputStream output = Files.newOutputStream(partial)) {
                byte[] buffer = new byte[1024 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (System.nanoTime() - started > Duration.ofSeconds(120).toNanos()) throw failure("artifact download timed out");
                    if (read > size - count) throw failure("artifact exceeds signed size");
                    count += read;
                    digest.update(buffer, 0, read); output.write(buffer, 0, read);
                }
            }
            if (count != size || !HexFormat.of().formatHex(digest.digest()).equals(sha)) throw failure("artifact digest or size mismatch");
            Files.move(partial, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(partial); }
    }

    private static InputStream openDownload(URI uri, Duration timeout) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(Math.toIntExact(timeout.toMillis()));
        connection.setReadTimeout(Math.toIntExact(timeout.toMillis()));
        Thread deadline = Thread.ofVirtual().start(() -> {
            try { Thread.sleep(timeout.toMillis()); connection.disconnect(); }
            catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        });
        try {
            int status = connection.getResponseCode();
            if (status >= 300 && status < 400) throw failure("artifact redirects are forbidden");
            if (status < 200 || status >= 300) throw failure("artifact download failed");
            return new java.io.FilterInputStream(connection.getInputStream()) {
                @Override public void close() throws java.io.IOException {
                    try { super.close(); } finally { deadline.interrupt(); connection.disconnect(); }
                }
            };
        } catch (Exception error) { deadline.interrupt(); connection.disconnect(); throw error; }
    }

    public static String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static List<String> append(List<String> original, String... values) {
        List<String> result = new ArrayList<>(original);
        result.addAll(List.of(values)); return result;
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || !value.isTextual()) throw failure("missing or invalid field: " + safeField(field));
        return value.textValue();
    }

    private static String safeField(String field) {
        return Set.of("root", "payload", "kid", "signature", "pluginCode", "version", "hostService", "runtime", "sourceLicense", "image", "imageId", "sourceSha256", "runtimeSha256", "envFile", "network", "Id", "Name", "url", "catalogOrigin", "instanceId", "tokenEnv", "bind").contains(field) ? field : "configuration";
    }

    private static final class SafeException extends IllegalArgumentException {
        private SafeException(String message) { super(message); }
    }

    static IllegalArgumentException failure(String message) { return new SafeException(message); }

    /** 仅内部固定文案能跨越信任边界；不反射第三方异常、参数、响应或命令输出。 */
    public static String safeMessage(Throwable error) {
        if (error instanceof SafeException) return error.getMessage();
        if (error instanceof CommandException) return "Container command failed; inspect agent host logs";
        return "Installer operation failed; inspect agent host state";
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            if (closed) return;
            closed = true;
            workers.shutdown();
        }
        boolean interrupted = false;
        try {
            while (!workers.isTerminated()) {
                try { workers.awaitTermination(1, TimeUnit.DAYS); }
                catch (InterruptedException error) { interrupted = true; }
            }
        } finally {
            try { processLock.release(); } finally { lockChannel.close(); }
            if (interrupted) Thread.currentThread().interrupt();
        }
    }
}
