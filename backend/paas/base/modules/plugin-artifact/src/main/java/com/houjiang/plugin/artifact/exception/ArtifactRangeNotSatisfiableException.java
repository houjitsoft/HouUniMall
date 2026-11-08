package com.houjiang.plugin.artifact.exception;

/**
 * Range 请求无法满足（HTTP 416 Range Not Satisfiable）。
 *
 * <p>典型场景：断点续传时客户端持有的本地分片长度已超过服务端制品当前长度
 * （制品被重新发布后变短），此时 {@code start >= totalSize}。
 *
 * @author Amos
 * @since M2
 */
public class ArtifactRangeNotSatisfiableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 制品完整字节数，用于回写 {@code Content-Range: bytes} 星号斜杠 total 形式的响应头。
     */
    private final long totalSize;

    /**
     * 构造 Range 异常。
     *
     * @param message   描述
     * @param totalSize 制品完整字节数
     */
    public ArtifactRangeNotSatisfiableException(String message, long totalSize) {
        super(message);
        this.totalSize = totalSize;
    }

    /**
     * @return 制品完整字节数
     */
    public long getTotalSize() {
        return totalSize;
    }
}
