package com.houjiang.plugin.artifact.util;

/**
 * HTTP Range 头解析（RFC 7233 单区间子集）。
 *
 * <p>支持三种形式：
 * <pre>
 *   bytes=0-1023    前 1024 字节
 *   bytes=1024-     从第 1024 字节到末尾
 *   bytes=-512      最后 512 字节（suffix range）
 * </pre>
 *
 * <p><b>不支持多区间</b>（{@code bytes=0-99,200-299}）：多区间要求 multipart/byteranges 响应，
 * 对「单个 jar 断点续传」这个场景毫无价值，却会让响应构造复杂一个数量级。
 * 遇到多区间直接抛异常，由上层回 416 并带上完整长度，客户端会退化成全量下载——行为安全。
 *
 * @author Amos
 * @since M2
 */
public final class HttpRanges {

    /**
     * Range 头前缀。
     */
    private static final String BYTES_PREFIX = "bytes=";

    /**
     * 私有构造，工具类不允许实例化。
     */
    private HttpRanges() {
    }

    /**
     * 解析 Range 头。
     *
     * @param headerValue Range 头原始值，可为 null 或空白
     * @return 解析结果；头缺失时返回 null
     * @throws IllegalArgumentException 格式非法或非单区间时抛出
     */
    public static RangeSpec parse(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        String value = headerValue.trim();
        if (!value.startsWith(BYTES_PREFIX)) {
            throw new IllegalArgumentException("只支持 bytes 类型的 Range 请求：" + headerValue);
        }
        String range = value.substring(BYTES_PREFIX.length());
        if (range.contains(",")) {
            throw new IllegalArgumentException("不支持多区间 Range 请求：" + headerValue);
        }
        int dash = range.indexOf('-');
        if (dash < 0) {
            throw new IllegalArgumentException("Range 缺少 '-'：" + headerValue);
        }
        String startPart = range.substring(0, dash).trim();
        String endPart = range.substring(dash + 1).trim();
        if (startPart.isEmpty() && endPart.isEmpty()) {
            throw new IllegalArgumentException("Range 区间为空：" + headerValue);
        }
        Long start = startPart.isEmpty() ? null : parseNonNegative(startPart, headerValue);
        Long end = endPart.isEmpty() ? null : parseNonNegative(endPart, headerValue);
        if (start == null) {
            // bytes=-N：最后 N 字节
            if (end == null || end <= 0) {
                throw new IllegalArgumentException("suffix Range 长度必须为正数：" + headerValue);
            }
            return RangeSpec.suffix(end);
        }
        if (end != null && end < start) {
            throw new IllegalArgumentException("Range 终点不能小于起点：" + headerValue);
        }
        return RangeSpec.explicit(start, end);
    }

    private static long parseNonNegative(String text, String headerValue) {
        try {
            long value = Long.parseLong(text);
            if (value < 0) {
                throw new IllegalArgumentException("Range 不允许负数：" + headerValue);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Range 含非数字：" + headerValue);
        }
    }

    /**
     * 解析出的区间规格（尚未结合制品长度）。
     */
    public static final class RangeSpec {

        private final Long start;

        private final Long end;

        private final Long suffixLength;

        private RangeSpec(Long start, Long end, Long suffixLength) {
            this.start = start;
            this.end = end;
            this.suffixLength = suffixLength;
        }

        /**
         * 构造显式区间 {@code start-end}（end 可为 null 表示到末尾）。
         *
         * @param start 起点（含）
         * @param end   终点（含），可为 null
         * @return 区间规格
         */
        public static RangeSpec explicit(Long start, Long end) {
            return new RangeSpec(start, end, null);
        }

        /**
         * 构造后缀区间 {@code -suffixLength}。
         *
         * @param suffixLength 末尾字节数
         * @return 区间规格
         */
        public static RangeSpec suffix(Long suffixLength) {
            return new RangeSpec(null, null, suffixLength);
        }

        /**
         * 结合制品完整长度解析出起点。
         *
         * @param totalSize 制品完整字节数
         * @return 起点（含）
         */
        public long resolveStart(long totalSize) {
            if (suffixLength != null) {
                return Math.max(0L, totalSize - suffixLength);
            }
            return start == null ? 0L : start;
        }

        /**
         * 结合制品完整长度解析出终点。
         *
         * @param totalSize 制品完整字节数
         * @return 终点（含）
         */
        public long resolveEnd(long totalSize) {
            if (end != null) {
                return Math.min(end, totalSize - 1);
            }
            return totalSize - 1;
        }

        /**
         * 判断区间在给定长度下是否可满足。
         *
         * @param totalSize 制品完整字节数
         * @return true 表示可满足
         */
        public boolean satisfiable(long totalSize) {
            if (totalSize <= 0) {
                return false;
            }
            long resolvedStart = resolveStart(totalSize);
            long resolvedEnd = resolveEnd(totalSize);
            return resolvedStart <= resolvedEnd && resolvedStart < totalSize;
        }
    }
}
