package com.houjiang.thirdparty.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinioOssClientServiceImplTest {

    @Test
    void applyEndpointConfigUsesRegionAsApiAndEndpointAsPublicUrl() {
        MinioOssClientServiceImpl.applyEndpointConfig(
            "http://1.13.156.250:9000",
            "https://minio.gongpinyun.com");

        assertEquals("http://1.13.156.250:9000", MinioOssClientServiceImpl.API_ENDPOINT);
        assertEquals("https://minio.gongpinyun.com", MinioOssClientServiceImpl.PUBLIC_BASE_URL);
        assertEquals(
            "https://minio.gongpinyun.com/gongpinyun/file/2026/08/10/a.png",
            MinioOssClientServiceImpl.resolvePublicObjectUrl(
                MinioOssClientServiceImpl.PUBLIC_BASE_URL,
                "gongpinyun",
                "file/2026/08/10/a.png"));
    }

    @Test
    void applyEndpointConfigFallsBackWhenPublicMissing() {
        MinioOssClientServiceImpl.applyEndpointConfig("http://127.0.0.1:9000", "");
        assertEquals("http://127.0.0.1:9000", MinioOssClientServiceImpl.API_ENDPOINT);
        assertEquals("http://127.0.0.1:9000", MinioOssClientServiceImpl.PUBLIC_BASE_URL);
    }

    @Test
    void applyEndpointConfigRejectsEmpty() {
        assertThrows(IllegalStateException.class,
            () -> MinioOssClientServiceImpl.applyEndpointConfig("", ""));
    }

    @Test
    void normalizeFolderAvoidsDoubleSlash() {
        assertEquals("file/uploads", MinioOssClientServiceImpl.normalizeFolderBase("/file/uploads/"));
        assertEquals("file/uploads/2026/08/10",
            MinioOssClientServiceImpl.joinPath("file/uploads", "2026/08/10"));
    }
}
