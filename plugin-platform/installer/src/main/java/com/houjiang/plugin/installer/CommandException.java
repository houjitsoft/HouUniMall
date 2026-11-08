package com.houjiang.plugin.installer;

/** stderr 仅供内部判断 Docker 对象是否不存在，不应写入日志或 HTTP。 */
public final class CommandException extends Exception {
    private final int exitCode;
    private final String stderr;

    public CommandException(int exitCode, String stderr) {
        super("Container command failed; inspect agent host logs");
        this.exitCode = exitCode;
        this.stderr = stderr == null ? "" : stderr;
    }

    public int exitCode() { return exitCode; }
    public String stderr() { return stderr; }
}
