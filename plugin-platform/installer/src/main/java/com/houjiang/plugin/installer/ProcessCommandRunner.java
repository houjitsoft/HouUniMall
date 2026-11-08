package com.houjiang.plugin.installer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** 同时排空 stdout/stderr，限制捕获内存；超时终止子进程树。 */
public final class ProcessCommandRunner implements CommandRunner {
    private static final int CAPTURE_LIMIT = 8 * 1024 * 1024;

    @Override
    public String run(List<String> arguments, Duration timeout) throws Exception {
        InstallerAgent.validateCommand(arguments, "invalid command");
        Process process = new ProcessBuilder(arguments).start();
        process.getOutputStream().close();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        Thread out = Thread.ofVirtual().start(() -> drain(process.getInputStream(), stdout));
        Thread err = Thread.ofVirtual().start(() -> drain(process.getErrorStream(), stderr));
        long deadline = System.nanoTime() + timeout.toNanos();
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new CommandException(-1, "command timed out");
            }
            if (!out.join(Duration.ofNanos(Math.max(0, deadline - System.nanoTime()))) ||
                    !err.join(Duration.ofNanos(Math.max(0, deadline - System.nanoTime())))) {
                throw new CommandException(-1, "command output timed out");
            }
            if (process.exitValue() != 0) {
                throw new CommandException(process.exitValue(), stderr.toString(StandardCharsets.UTF_8));
            }
            if (stdout.size() >= CAPTURE_LIMIT) throw new CommandException(-1, "command output too large");
            return stdout.toString(StandardCharsets.UTF_8).strip();
        } finally {
            if (process.isAlive()) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }

    private static void drain(InputStream input, ByteArrayOutputStream output) {
        try (input) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                int remaining = CAPTURE_LIMIT - output.size();
                if (remaining > 0) output.write(buffer, 0, Math.min(count, remaining));
            }
        } catch (java.io.IOException ignored) {
            // 进程退出或被超时终止时，管道可能被关闭。
        }
    }
}
