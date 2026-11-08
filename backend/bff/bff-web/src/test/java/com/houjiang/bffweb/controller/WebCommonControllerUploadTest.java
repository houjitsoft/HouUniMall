package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebCommonControllerUploadTest {
    private static final long MAX_SIZE = 20L * 1024 * 1024;

    @TempDir
    Path uploadDir;

    private WebCommonController controller;

    @BeforeEach
    void setUp() {
        controller = new WebCommonController();
        ReflectionTestUtils.setField(controller, "configuredUploadPath", uploadDir.toString());
    }

    static Stream<Arguments> attachmentTypes() {
        return Stream.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "wps", "et", "dps",
                        "odt", "ods", "odp", "odg", "odf", "rtf", "txt", "csv", "tsv", "md",
                        "json", "xml", "log", "yaml", "yml", "zip", "rar", "7z", "tar", "gz",
                        "bz2", "xz", "tgz", "tbz2", "txz", "tar.gz", "tar.bz2", "tar.xz")
                .flatMap(extension -> Stream.of("." + extension, "." + extension.toUpperCase(Locale.ROOT)))
                .flatMap(extension -> Stream.of("application/octet-stream", "text/html", "", null)
                        .map(mime -> Arguments.of(extension, mime)));
    }

    @ParameterizedTest
    @MethodSource("attachmentTypes")
    void attachmentExtensionsAreAllowedRegardlessOfMimeAndRemainAccessible(String extension, String mime) throws Exception {
        String originalName = "backup.release" + extension;
        byte[] content = {1, 2, 3};
        assertEquals(Boolean.TRUE, ReflectionTestUtils.invokeMethod(
                controller, "isAllowedFileType", originalName));

        ResponseResult<?> result = controller.uploadFile(
                new MockMultipartFile("file", originalName, mime, content));

        assertEquals(1, result.getCode());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        String url = (String) data.get("url");
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.endsWith(extension));
        assertEquals(originalName, data.get("fileName"));
        assertEquals(3L, data.get("fileSize"));
        String savedName = url.substring("/uploads/".length());
        assertArrayEquals(content, Files.readAllBytes(uploadDir.resolve(savedName)));
        var response = controller.serveFile(savedName);
        assertEquals(200, response.getStatusCode().value());
        boolean pdf = extension.equalsIgnoreCase(".pdf");
        assertEquals(pdf ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM,
                response.getHeaders().getContentType());
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertEquals("public, max-age=86400", response.getHeaders().getCacheControl());
        if (pdf) {
            assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        } else {
            assertEquals("attachment", response.getHeaders().getContentDisposition().getType());
            assertEquals(savedName, response.getHeaders().getContentDisposition().getFilename());
        }
        assertEquals(3L, response.getHeaders().getContentLength());
        assertNotNull(response.getBody());
        try (var input = response.getBody().getInputStream()) {
            assertArrayEquals(content, input.readAllBytes());
        }
    }

    @Test
    void emptyFileIsRejectedWithoutSaving() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        ResponseResult<?> result = controller.uploadFile(file);

        assertNotEquals(1, result.getCode());
        assertEquals("文件不能为空", result.getMessage());
        verify(file, never()).transferTo(any(File.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, MAX_SIZE - 1, MAX_SIZE, MAX_SIZE + 1})
    void sizeBoundaryIsEnforcedBeforeSaving(long size) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("backup.tar.gz");
        when(file.getContentType()).thenReturn("application/octet-stream");
        when(file.getSize()).thenReturn(size);

        ResponseResult<?> result = controller.uploadFile(file);

        if (size <= MAX_SIZE) {
            assertEquals(1, result.getCode());
            verify(file).transferTo(any(File.class));
        } else {
            assertNotEquals(1, result.getCode());
            assertEquals("文件大小不能超过20MB", result.getMessage());
            verify(file, never()).transferTo(any(File.class));
        }
    }

    static Stream<Arguments> rejectedTypes() {
        return Stream.of("page.html", "page.htm", "image.svg", "script.js", "program.exe", "legacy.bin",
                        "backup.tar.gz.exe", "backup.tar.bz2.html", "backup.tar.xz.svg", "no-extension", "", " ", null)
                .flatMap(name -> Stream.of("application/pdf", "image/png", "image/jpeg", "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                "application/zip", "application/x-zip-compressed", "application/x-zip", "multipart/x-zip",
                                "APPLICATION/ZIP; charset=UTF-8", "text/plain", "application/octet-stream", "", null)
                        .map(mime -> Arguments.of(name, mime)));
    }

    @ParameterizedTest
    @MethodSource("rejectedTypes")
    void unknownAndActiveExtensionsAreRejectedEvenWithForgedMime(String name, String mime) throws Exception {
        assertEquals(Boolean.FALSE, ReflectionTestUtils.invokeMethod(
                controller, "isAllowedFileType", name));
        ResponseResult<?> result = controller.uploadFile(
                new MockMultipartFile("file", name, mime, new byte[]{1}));
        assertNotEquals(1, result.getCode());
        assertEquals("不支持的文件类型", result.getMessage());
        try (var files = Files.list(uploadDir)) {
            assertEquals(0L, files.count());
        }
    }

    static Stream<Arguments> imageTypes() {
        return Stream.of(Arguments.of("jpg", "image/jpeg"), Arguments.of("jpeg", "image/jpeg"),
                        Arguments.of("png", "image/png"), Arguments.of("gif", "image/gif"),
                        Arguments.of("bmp", "image/bmp"), Arguments.of("webp", "image/webp"))
                .flatMap(args -> Stream.of(Arguments.of(args.get()[0], args.get()[1]),
                        Arguments.of(((String) args.get()[0]).toUpperCase(Locale.ROOT), args.get()[1])));
    }

    @ParameterizedTest
    @MethodSource("imageTypes")
    void existingImagesRemainInlineWithCorrectContentType(String extension, String mime) throws Exception {
        ResponseResult<?> result = controller.uploadFile(
                new MockMultipartFile("file", "picture." + extension, "text/html", new byte[]{1}));
        assertEquals(1, result.getCode());
        String url = (String) ((Map<?, ?>) result.getData()).get("url");
        var response = controller.serveFile(url.substring("/uploads/".length()));
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.parseMediaType(mime), response.getHeaders().getContentType());
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @ParameterizedTest
    @ValueSource(strings = {"legacy.bin", "legacy.html", "legacy.svg", "legacy.js", "legacy.exe"})
    void historicalFilesRemainDownloadableAsAttachments(String name) throws Exception {
        Files.write(uploadDir.resolve(name), new byte[]{1});
        var response = controller.serveFile(name);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertEquals("attachment", response.getHeaders().getContentDisposition().getType());
        assertEquals(name, response.getHeaders().getContentDisposition().getFilename());
    }

    @Test
    void extensionNormalizationDoesNotDependOnDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(1, controller.uploadFile(new MockMultipartFile(
                    "file", "archive.ZIP", null, new byte[]{1})).getCode());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"../backup.tar.gz", "backup.extra.tar.gz", "backup.tar.gz.exe",
            "backup..tar.gz", "/backup.tar.gz", "backup.tar/gz", "../backup.tar.bz2",
            "backup.extra.tar.bz2", "backup..tar.xz", "backup.tar.xz.exe", "backup\\tar.xz",
            "backup.tar.xz\r\n", "backup.tar.xz\"", "backup.tar.xz;", "backup.tar.xz/extra",
            "backup%2etar.xz", ".tar.xz"})
    void compoundExtensionDoesNotRelaxFileAccessWhitelist(String name) {
        var pattern = (java.util.regex.Pattern) ReflectionTestUtils.getField(controller, "SAFE_FILE_NAME");
        assertNotNull(pattern);
        assertFalse(pattern.matcher(name).matches());
        assertEquals(404, controller.serveFile(name).getStatusCode().value());
    }

    @Test
    void missingTarGzipFileReturnsNotFound() {
        assertEquals(404, controller.serveFile("missing.tar.gz").getStatusCode().value());
    }
}
