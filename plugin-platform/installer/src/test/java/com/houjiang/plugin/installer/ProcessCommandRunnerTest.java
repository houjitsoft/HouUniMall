package com.houjiang.plugin.installer;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProcessCommandRunnerTest {
    final ProcessCommandRunner runner = new ProcessCommandRunner();
    List<String> child(String... args) {
        String testClasses;
        try { testClasses = Path.of(Child.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(); }
        catch (java.net.URISyntaxException error) { throw new AssertionError(error); }
        String classpath = testClasses + java.io.File.pathSeparator + System.getProperty("java.class.path");
        List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", classpath, Child.class.getName()));
        command.addAll(List.of(args)); return command;
    }
    @Test void returnsTrimmedUtf8Output() throws Exception { assertEquals("回归输出", runner.run(child("stdout"), Duration.ofSeconds(10))); }
    @Test void capturesExitCodeAndStderrWithoutLeakingMessage() {
        CommandException error = assertThrows(CommandException.class, () -> runner.run(child("failure"), Duration.ofSeconds(10)));
        assertEquals(7, error.exitCode()); assertTrue(error.stderr().contains("synthetic-stderr")); assertFalse(error.getMessage().contains("synthetic-stderr"));
    }
    @Test void drainsStdoutAndStderrConcurrently() throws Exception {
        assertEquals(1024 * 1024, runner.run(child("both"), Duration.ofSeconds(15)).length());
    }
    @Test void rejectsOutputAtCaptureLimitWithoutDeadlock() {
        CommandException error = assertThrows(CommandException.class, () -> runner.run(child("large"), Duration.ofSeconds(15)));
        assertEquals(-1, error.exitCode()); assertEquals("command output too large", error.stderr());
    }
    @Test void timeoutTerminatesRealChild() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            CommandException error = assertThrows(CommandException.class, () -> runner.run(child("wait"), Duration.ofMillis(250)));
            assertEquals(-1, error.exitCode()); assertTrue(error.stderr().contains("timed out"));
        });
    }
    @Test void argumentsAreNotInterpretedByShell() throws Exception {
        String literal = "$(printf unexpected); * && |";
        assertEquals(literal, runner.run(child("argument", literal), Duration.ofSeconds(10)));
    }
    @Test void validatesCommandBeforeLaunchingProcess() {
        for (List<String> invalid : Arrays.asList(null, List.<String>of(), List.of(""), List.of(" "), List.of("bad\0command"), Arrays.asList("java", null)))
            assertThrows(IllegalArgumentException.class, () -> runner.run(invalid, Duration.ofSeconds(1)));
    }
    public static class Child {
        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "stdout" -> System.out.write(" \n回归输出\n ".getBytes(StandardCharsets.UTF_8));
                case "failure" -> { System.err.print("synthetic-stderr"); System.exit(7); }
                case "both" -> {
                    byte[] bytes = new byte[8192]; Arrays.fill(bytes, (byte) 'x');
                    for (int i = 0; i < 128; i++) { System.out.write(bytes); System.err.write(bytes); }
                }
                case "large" -> { byte[] bytes = new byte[8192]; Arrays.fill(bytes, (byte) 'x'); for (int i = 0; i < 1025; i++) System.out.write(bytes); }
                case "wait" -> Thread.sleep(30000);
                case "argument" -> System.out.print(args[1]);
                default -> throw new IllegalArgumentException("未知测试模式");
            }
        }
    }
}
