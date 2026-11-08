package com.houjiang.plugin.installer;

import java.time.Duration;
import java.util.List;

/** 参数直接传给进程，不通过 shell；非零退出必须抛出 CommandException。 */
@FunctionalInterface
public interface CommandRunner {
    String run(List<String> arguments, Duration timeout) throws Exception;
}
