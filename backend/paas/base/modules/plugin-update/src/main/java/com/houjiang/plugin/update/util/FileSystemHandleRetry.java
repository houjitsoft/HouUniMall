package com.houjiang.plugin.update.util;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;

/**
 * Windows 文件句柄释放重试（方案 §5.3 步骤 8）。
 *
 * <p><b>为什么需要它</b>：Windows 上删除 / 移动一个正被 ClassLoader 打开的文件会抛
 * {@link AccessDeniedException}（中文环境提示「另一个程序正在使用此文件」）。
 * 而 {@code unloadPlugin} 只是发起卸载，句柄真正释放是异步的——
 * 返回成功不代表文件已经可删。Linux 上没有这个问题，因此这个坑只在生产 Windows 上出现，
 * 本地开发与单测永远测不出来，只能靠显式重试兜住。
 *
 * <p><b>为什么是「指数退避 + 封顶次数」而不是一直等</b>：一直等会把一次升级挂死，
 * 用户点了升级就再没有反馈；封顶 3 次（200ms → 400ms，累计约 600ms）后判定失败，
 * 由调用方<b>恢复流量并放弃升级</b>——宁可本次不升级，也不留「旧版本已停、新版本没起」的半生效状态。
 *
 * @author Amos
 * @since M4
 */
public final class FileSystemHandleRetry {

    /**
     * 私有构造，工具类不允许实例化。
     */
    private FileSystemHandleRetry() {
    }

    /**
     * 可能抛出受检异常的动作。
     */
    @FunctionalInterface
    public interface HandleAction {

        /**
         * 执行动作。
         *
         * @throws Exception 任意异常，只有「句柄占用」类异常会被重试
         */
        void run() throws Exception;
    }

    /**
     * 执行动作，遇到「文件句柄仍被占用」时指数退避重试。
     *
     * <p>只有 {@link #isHandleLocked(Throwable)} 判定的异常才会重试：
     * 其它异常（schema 失败、验签失败、装载失败）重试毫无意义，直接抛出，
     * 避免把「真失败」伪装成「慢失败」拖长故障时间。
     *
     * @param description    动作描述（日志用）
     * @param attempts       总尝试次数（含首次），小于 1 时按 1 处理
     * @param baseBackoffMs  首次退避毫秒数，之后每次翻倍
     * @param action         待执行动作
     * @throws Exception 最后一次尝试的异常；非句柄类异常原样抛出
     */
    public static void execute(String description, int attempts, long baseBackoffMs,
                               HandleAction action) throws Exception {
        HandleAction target = java.util.Objects.requireNonNull(action, "待执行动作不能为空");
        int total = Math.max(1, attempts);
        long backoff = Math.max(0L, baseBackoffMs);
        int attempt = 1;
        while (true) {
            try {
                target.run();
                return;
            } catch (Throwable error) {
                if (attempt >= total || !isHandleLocked(error)) {
                    throw error;
                }
                sleepQuietly(backoff);
                attempt++;
                backoff = backoff <= 0 ? 0 : backoff * 2;
            }
        }
    }

    /**
     * 判断异常是否为「文件句柄仍被占用」。
     *
     * <p>判据按「类型 + 文案」双保险：不同 JDK / 不同语言环境的 Windows 抛出的具体类型
     * 不完全一致（{@link AccessDeniedException} 最常见，也有包在 {@link FileSystemException}
     * 里的情况），只看类型会漏判，只看文案又会误伤；两者取或最稳。
     *
     * @param error 异常
     * @return true 表示可以重试
     */
    public static boolean isHandleLocked(Throwable error) {
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 5) {
            if (current instanceof AccessDeniedException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(java.util.Locale.ROOT);
                if (lower.contains("另一个程序正在使用") || lower.contains("being used by another process")) {
                    return true;
                }
            }
            current = current.getCause();
            depth++;
        }
        return false;
    }

    /**
     * 退避等待；被中断时恢复中断标记并返回（不抛，避免掩盖真正的失败原因）。
     *
     * @param millis 等待毫秒数
     */
    private static void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
