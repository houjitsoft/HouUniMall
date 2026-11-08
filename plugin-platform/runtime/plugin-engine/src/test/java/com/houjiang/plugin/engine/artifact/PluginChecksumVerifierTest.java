package com.houjiang.plugin.engine.artifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安装包完整性校验单测。
 *
 * @author HouCloud
 */
class PluginChecksumVerifierTest {

    private final PluginChecksumVerifier verifier = new PluginChecksumVerifier();

    @TempDir
    Path tempDir;

    @Test
    void md5MatchesKnownVector() {
        // "abc" 的标准 MD5
        assertEquals("900150983cd24fb0d6963f7d28e17f72", verifier.md5("abc".getBytes()));
    }

    @Test
    void verifyAcceptsMatchingChecksumIgnoringCase() {
        byte[] content = "mall-live-1.0.0".getBytes();
        String md5 = verifier.md5(content);

        assertTrue(verifier.verify(content, md5));
        assertTrue(verifier.verify(content, md5.toUpperCase()), "摘要大小写不敏感");
        assertTrue(verifier.verify(content, "  " + md5 + "  "), "容忍首尾空白");
    }

    @Test
    void verifyRejectsMismatchedChecksum() {
        byte[] content = "mall-live-1.0.0".getBytes();

        assertFalse(verifier.verify(content, "00000000000000000000000000000000"),
                "摘要不一致必须判失败");
    }

    @Test
    void verifyPassesWhenMarketDeclaresNoChecksum() {
        byte[] content = "mall-risk".getBytes();

        assertTrue(verifier.verify(content, null), "市场未声明摘要时不阻断");
        assertTrue(verifier.verify(content, "  "), "空摘要不阻断");
    }

    @Test
    void verifyWorksOnDiskFile() throws IOException {
        Path jar = tempDir.resolve("mall-points-1.0.0.jar");
        byte[] content = "points".getBytes();
        Files.write(jar, content);

        assertTrue(verifier.verify(jar, verifier.md5(content)));
        assertFalse(verifier.verify(jar, "deadbeef"));
    }
}
