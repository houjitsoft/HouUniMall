package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/** 独立进程入口：java ... com.houjiang.plugin.installer.InstallerMain --config /path/installer.json */
public final class InstallerMain {
    private InstallerMain() { }

    public static void main(String[] args) {
        if (args.length != 2 || !"--config".equals(args[0])) {
            System.err.println("Usage: InstallerMain --config <installer.json>");
            System.exit(2);
            return;
        }
        try {
            JsonNode config;
            try (var input = Files.newInputStream(Path.of(args[1]))) {
                config = InstallerAgent.JSON.readTree(input);
            }
            String token = System.getenv(config.path("tokenEnv").asText("UNIMALL_INSTALLER_TOKEN"));
            if (token == null || token.codePointCount(0, token.length()) < 32) throw InstallerAgent.failure("installer token must be configured with at least 32 characters");
            InstallerAgent agent = new InstallerAgent(config);
            InstallerHttpServer server;
            try { server = new InstallerHttpServer(config, agent, token); }
            catch (Exception error) { agent.close(); throw error; }
            CountDownLatch stopped = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.close();
                try { agent.close(); }
                catch (Exception error) { System.err.println("Installer shutdown failed; inspect host state"); }
                finally { stopped.countDown(); }
            }, "installer-shutdown"));
            server.start();
            System.out.println("UniMall installer ready on " + server.address());
            stopped.await();
        } catch (Exception error) {
            System.err.println(InstallerAgent.safeMessage(error));
            System.exit(1);
        }
    }
}
