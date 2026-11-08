package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

final class InstallerTestSupport {
    static final byte[] SOURCE = "customer source archive".getBytes(StandardCharsets.UTF_8);
    static final byte[] RUNTIME = "docker image archive".getBytes(StandardCharsets.UTF_8);
    static final String IMAGE = "sha256:" + "a".repeat(64);
    static final String OLD_IMAGE = "sha256:" + "d".repeat(64);
    final KeyPair keys;
    final ObjectNode config;
    final DockerHost host = new DockerHost();
    final QueueExecutor executor = new QueueExecutor();
    final List<String> downloads = new ArrayList<>();

    InstallerTestSupport(Path directory) throws Exception {
        keys = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        Path pem = directory.resolve("release.pub");
        Files.writeString(pem, "-----BEGIN PUBLIC KEY-----\n" + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keys.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----\n", StandardCharsets.US_ASCII);
        Path env = directory.resolve("service.env");
        Files.writeString(env, "SPRING_PROFILES_ACTIVE=test\n");
        config = InstallerAgent.JSON.createObjectNode();
        config.put("root", directory.resolve("state").toString()).put("instanceId", "test-instance")
                .put("catalogOrigin", "http://127.0.0.1:1").put("bind", "127.0.0.1").put("port", 0);
        config.putObject("publicKeys").put("test", pem.toString());
        ObjectNode services = config.putObject("services");
        for (String code : List.of("erp", "crm")) {
            ObjectNode service = services.putObject(code);
            service.put("envFile", env.toString()).put("network", "unimall").put("healthTimeoutSeconds", 0.1);
            service.putArray("registrationCheck").add("/check-registration");
            service.putArray("upgradePreflight").add("/upgrade-preflight");
        }
    }

    InstallerAgent agent() throws Exception {
        return new InstallerAgent(config, host, (uri, timeout) -> {
            assertEquals(Duration.ofSeconds(120), timeout);
            downloads.add(uri.toString());
            return new ByteArrayInputStream(uri.getQuery().endsWith("=source") ? SOURCE : RUNTIME);
        }, duration -> {}, executor);
    }

    static String sha(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    static ObjectNode manifest() throws Exception { return manifest("erp", "1.2.3"); }
    static ObjectNode manifest(String code, String version) throws Exception {
        return InstallerAgent.JSON.createObjectNode().put("pluginCode", code).put("hostService", code)
                .put("version", version).put("runtime", "standalone").put("sourceLicense", "Apache-2.0")
                .put("image", "unimall-" + code + ":" + version).put("imageId", IMAGE)
                .put("sourceSha256", sha(SOURCE)).put("sourceSize", SOURCE.length)
                .put("runtimeSha256", sha(RUNTIME)).put("runtimeSize", RUNTIME.length);
    }

    ObjectNode signed(byte[] payload) throws Exception {
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(keys.getPrivate());
        signer.update(payload);
        return InstallerAgent.JSON.createObjectNode().put("kid", "test")
                .put("payload", Base64.getEncoder().encodeToString(payload))
                .put("signature", Base64.getEncoder().encodeToString(signer.sign()));
    }
    ObjectNode signed(JsonNode manifest) throws Exception { return signed(InstallerAgent.JSON.writeValueAsBytes(manifest)); }
    ObjectNode bundle() throws Exception { return bundle(manifest()); }
    ObjectNode bundle(JsonNode manifest) throws Exception {
        ObjectNode result = InstallerAgent.JSON.createObjectNode();
        result.set("release", signed(manifest));
        result.putObject("source").put("url", "/base/plugin-center/artifact/raw?token=source");
        result.putObject("runtime").put("url", "/base/plugin-center/artifact/raw?token=runtime");
        return result;
    }
    JsonNode install(InstallerAgent agent) throws Exception {
        String task = agent.submit(bundle()).path("taskId").asText();
        executor.drain();
        return agent.status(task);
    }

    static boolean command(List<String> args, String... prefix) {
        return args.size() >= prefix.length && args.subList(0, prefix.length).equals(List.of(prefix));
    }

    static final class QueueExecutor extends AbstractExecutorService {
        final Deque<Runnable> queue = new ArrayDeque<>();
        boolean shutdown;
        boolean reject;
        @Override public void execute(Runnable task) {
            if (shutdown || reject) throw new java.util.concurrent.RejectedExecutionException("test rejection");
            queue.add(task);
        }
        void drain() { while (!queue.isEmpty()) queue.remove().run(); }
        @Override public void shutdown() { shutdown = true; drain(); }
        @Override public List<Runnable> shutdownNow() { shutdown = true; List<Runnable> result = new ArrayList<>(queue); queue.clear(); return result; }
        @Override public boolean isShutdown() { return shutdown; }
        @Override public boolean isTerminated() { return shutdown && queue.isEmpty(); }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    }

    static final class DockerHost implements CommandRunner {
        final Map<String, ObjectNode> containers = new LinkedHashMap<>();
        final List<List<String>> commands = new ArrayList<>();
        final Set<String> unhealthy = new HashSet<>();
        String imageId = IMAGE;
        JsonNode healthcheck = InstallerAgent.JSON.createArrayNode().add("CMD").add("/check-health");
        Predicate<List<String>> fail = args -> false;
        Predicate<List<String>> failAfter = args -> false;
        int serial;
        ObjectNode add(String name) { return add(name, true); }
        ObjectNode add(String name, boolean managed) {
            ObjectNode value = InstallerAgent.JSON.createObjectNode().put("Id", "container-" + ++serial)
                    .put("Name", "/" + name).put("Image", OLD_IMAGE);
            ObjectNode labels = value.putObject("Config").putObject("Labels");
            if (managed) labels.put("unimall.service", "erp").put("unimall.version", "1.0.0");
            value.putObject("State").put("Status", "running").put("Running", true).putObject("Health").put("Status", "healthy");
            containers.put(name, value);
            return value;
        }
        String find(String target) {
            if (containers.containsKey(target)) return target;
            return containers.entrySet().stream().filter(e -> e.getValue().path("Id").asText().equals(target)).map(Map.Entry::getKey).findFirst().orElse(null);
        }
        @Override public String run(List<String> args, Duration timeout) throws Exception {
            assertEquals(InstallerAgent.COMMAND_TIMEOUT, timeout);
            commands.add(List.copyOf(args));
            if (fail.test(args)) throw new CommandException(1, "injected failure");
            String result;
            if (command(args, "docker", "image", "load")) result = "Loaded image";
            else if (command(args, "docker", "image", "inspect")) {
                ObjectNode image = InstallerAgent.JSON.createObjectNode().put("Id", imageId);
                image.putObject("Config").putObject("Healthcheck").set("Test", healthcheck);
                result = InstallerAgent.JSON.createArrayNode().add(image).toString();
            } else if (command(args, "docker", "inspect")) {
                String name = find(args.getLast());
                if (name == null) throw new CommandException(1, "Error: No such object: " + args.getLast());
                result = InstallerAgent.JSON.createArrayNode().add(containers.get(name).deepCopy()).toString();
            } else if (command(args, "docker", "run")) {
                String name = args.get(args.indexOf("--name") + 1);
                if (containers.containsKey(name)) throw new CommandException(1, "name already exists");
                ObjectNode value = add(name);
                value.put("Image", args.getLast());
                ObjectNode labels = ((ObjectNode) value.path("Config")).putObject("Labels");
                for (int i = 0; i < args.size() - 1; i++) if (args.get(i).equals("--label")) {
                    String[] label = args.get(i + 1).split("=", 2);
                    labels.put(label[0], label[1]);
                }
                if (unhealthy.contains(name)) ((ObjectNode) value.path("State")).put("Status", "exited").put("Running", false).withObject("Health").put("Status", "unhealthy");
                result = value.path("Id").asText();
            } else if (command(args, "docker", "rename")) {
                String name = find(args.get(2));
                if (name == null || containers.containsKey(args.get(3))) throw new CommandException(1, "rename failed");
                ObjectNode value = containers.remove(name);
                value.put("Name", "/" + args.get(3));
                containers.put(args.get(3), value);
                result = "";
            } else if (command(args, "docker", "stop") || command(args, "docker", "start")) {
                String name = find(args.getLast());
                if (name == null) throw new CommandException(1, "container missing");
                boolean running = args.get(1).equals("start");
                ((ObjectNode) containers.get(name).path("State")).put("Status", running ? "running" : "exited").put("Running", running);
                result = "";
            } else if (command(args, "docker", "rm")) {
                String name = find(args.getLast());
                if (name == null) throw new CommandException(1, "container missing");
                containers.remove(name);
                result = "";
            } else if (Set.of("/check-registration", "/upgrade-preflight").contains(args.getFirst())) result = "passed";
            else throw new AssertionError("未预期的命令: " + args);
            if (failAfter.test(args)) throw new CommandException(1, "injected failure after side effect");
            return result;
        }
    }
}
