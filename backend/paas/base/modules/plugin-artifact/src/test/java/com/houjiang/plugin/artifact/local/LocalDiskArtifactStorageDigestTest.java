package com.houjiang.plugin.artifact.local;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.util.ArtifactDigests;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 制品摘要回归测试 —— 专门盯 M1 修掉的那个「二次哈希」bug。
 *
 * <p>M1 冒烟只断言 md5 / sha256 的<b>长度</b>，所以
 * {@code ArtifactDigests.md5Hex(md5.digest())} 这种「把已算好的摘要再哈希一次」的写法
 * 一直没被发现：它照样产出 32 位十六进制。真到生产，结果是
 * 「客户端下载后算出的 md5 与 hou_plugin_artifact.md5 对不上，安装必然失败」。
 *
 * <p>因此这里做两件事：
 * <ol>
 *   <li>把「存储层返回的 md5」与「对原始字节算的 md5」<b>逐字符</b>比对；</li>
 *   <li>再从存储把字节读回来算一遍，断言它与库里那份仍然逐字符相等
 *       ——这正是下载后客户端会做的事。</li>
 * </ol>
 *
 * @author Amos
 * @since M2
 */
class LocalDiskArtifactStorageDigestTest {

    @TempDir
    Path tempDir;

    @Test
    void storedDigestEqualsDigestOfRawBytesCharByChar() throws IOException {
        LocalDiskArtifactStorage storage = storage();
        byte[] payload = payload();
        String key = ArtifactKeys.jarKey("coupon-demo", "1.0.0");

        StoredObject stored = storage.putIfAbsent(key, new ByteArrayInputStream(payload), payload.length);

        assertCharByChar(ArtifactDigests.md5Hex(payload), stored.getMd5(), "md5");
        assertCharByChar(ArtifactDigests.sha256Hex(payload), stored.getSha256(), "sha256");
        assertEquals(payload.length, stored.getSizeBytes());
    }

    @Test
    void bytesReadBackFromStorageMatchRecordedDigest() throws IOException {
        LocalDiskArtifactStorage storage = storage();
        byte[] payload = payload();
        String key = ArtifactKeys.jarKey("coupon-demo", "1.0.0");
        StoredObject stored = storage.putIfAbsent(key, new ByteArrayInputStream(payload), payload.length);

        try (ArtifactStream stream = storage.open(key, null, null)) {
            byte[] readBack = stream.getStream().readAllBytes();
            assertArrayEquals(payload, readBack, "读回的字节必须与写入的完全一致");
            assertCharByChar(stored.getMd5(), ArtifactDigests.md5Hex(readBack), "回读 md5");
            assertCharByChar(stored.getSha256(), ArtifactDigests.sha256Hex(readBack), "回读 sha256");
        }
    }

    @Test
    void rangedReadReturnsExactSlice() throws IOException {
        LocalDiskArtifactStorage storage = storage();
        byte[] payload = payload();
        String key = ArtifactKeys.jarKey("coupon-demo", "1.0.0");
        storage.putIfAbsent(key, new ByteArrayInputStream(payload), payload.length);

        try (ArtifactStream stream = storage.open(key, 2L, 5L)) {
            byte[] slice = stream.getStream().readAllBytes();
            assertEquals(4, slice.length);
            assertArrayEquals(new byte[] {payload[2], payload[3], payload[4], payload[5]}, slice);
            assertEquals("bytes 2-5/" + payload.length, stream.toContentRange());
        }
    }

    private LocalDiskArtifactStorage storage() {
        ArtifactProperties properties = new ArtifactProperties();
        properties.setRoot(tempDir.toString());
        return new LocalDiskArtifactStorage(properties);
    }

    /**
     * 固定内容（不用随机），失败时可稳定复现。
     *
     * @return 制品字节
     */
    private static byte[] payload() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            sb.append("houcloud-artifact-digest-").append(i).append(';');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 逐字符比对：整串断言失败时只看到「期望/实际」两行长串，
     * 逐字符能直接指出第几位开始不对，排查二次哈希这种问题快得多。
     *
     * @param expected 期望值
     * @param actual   实际值
     * @param label    摘要名
     */
    private static void assertCharByChar(String expected, String actual, String label) {
        assertEquals(expected.length(), actual.length(), label + " 长度不一致");
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.charAt(i), actual.charAt(i),
                    label + " 第 " + i + " 位不一致（期望 " + expected + "，实际 " + actual + "）");
        }
    }
}
