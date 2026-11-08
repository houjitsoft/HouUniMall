package com.houjiang.plugin.artifact.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 制品流：底层输入流 + 本次返回的字节数 + 关闭钩子。
 *
 * <p>单独封装成一个类型而不是直接返回 {@link InputStream}，是因为下载端点需要三样东西：
 * <ol>
 *   <li>流本身（用于 copy 到响应输出流）；</li>
 *   <li>本次返回的字节数（用于 {@code Content-Length}，Range 请求时是区间长度而不是全文件大小）；</li>
 *   <li>完整对象大小（用于 {@code Content-Range: bytes start-end/total}）。</li>
 * </ol>
 * 关闭钩子用于实现（尤其是云存储）在流关闭时释放连接、归还资源。
 *
 * @author Amos
 * @since M0
 */
public class ArtifactStream implements Closeable {

    /**
     * 底层输入流。
     */
    private final InputStream stream;

    /**
     * 本次返回区间的字节数。
     */
    private final long size;

    /**
     * 完整对象字节数（不受 Range 影响）。
     */
    private final long totalSize;

    /**
     * 区间起点（含），无 Range 时为 0。
     */
    private final long rangeStart;

    /**
     * 区间终点（含），无 Range 时为 totalSize - 1。
     */
    private final long rangeEnd;

    /**
     * 关闭钩子，可为 null。
     */
    private final Runnable closeHook;

    /**
     * 构造制品流。
     *
     * @param stream    底层输入流
     * @param size      本次返回区间字节数
     * @param totalSize 完整对象字节数
     * @param rangeStart 区间起点（含）
     * @param rangeEnd   区间终点（含）
     * @param closeHook  关闭钩子，可为 null
     */
    public ArtifactStream(InputStream stream,
                          long size,
                          long totalSize,
                          long rangeStart,
                          long rangeEnd,
                          Runnable closeHook) {
        this.stream = Objects.requireNonNull(stream, "stream 不能为空");
        this.size = size;
        this.totalSize = totalSize;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.closeHook = closeHook;
    }

    /**
     * @return 底层输入流，调用方不要单独关闭它，请调用 {@link #close()}
     */
    public InputStream getStream() {
        return stream;
    }

    /**
     * @return 本次返回区间字节数
     */
    public long getSize() {
        return size;
    }

    /**
     * @return 完整对象字节数
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @return 区间起点（含）
     */
    public long getRangeStart() {
        return rangeStart;
    }

    /**
     * @return 区间终点（含）
     */
    public long getRangeEnd() {
        return rangeEnd;
    }

    /**
     * @return 是否为区间响应（用于决定是否输出 206 与 Content-Range）
     */
    public boolean isRanged() {
        return rangeStart > 0 || rangeEnd < totalSize - 1;
    }

    /**
     * @return 标准 {@code Content-Range} 头值，如 {@code bytes 0-1023/4096}
     */
    public String toContentRange() {
        return "bytes " + rangeStart + "-" + rangeEnd + "/" + totalSize;
    }

    @Override
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            if (closeHook != null) {
                closeHook.run();
            }
        }
    }
}
