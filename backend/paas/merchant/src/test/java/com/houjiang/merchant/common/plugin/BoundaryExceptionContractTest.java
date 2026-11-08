package com.houjiang.merchant.common.plugin;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.plugin.storage.engine.LocalStorage;
import com.houjiang.merchant.common.util.YmlUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BoundaryExceptionContractTest {

    @TempDir
    Path tempDir;

    @Test
    void localUploadUsesStablePublicMessageAndLogsOriginalFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        IOException failure = new IOException("/srv/private/upload secret-key=internal");
        org.mockito.Mockito.doThrow(failure).when(file).transferTo(org.mockito.ArgumentMatchers.any(java.io.File.class));
        new YmlUtils().setEnvironment(new MockEnvironment()
                .withProperty("merchant.upload-directory", tempDir + "/"));
        Logger logger = (Logger) LoggerFactory.getLogger(LocalStorage.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            OperateException exception = assertThrows(
                    OperateException.class,
                    () -> new LocalStorage().upload(file, "20260726/file.png", "image"));

            assertEquals("本地文件上传失败，请稍后重试", exception.getMsg());
            ILoggingEvent event = appender.list.get(appender.list.size() - 1);
            assertEquals("本地文件上传失败", event.getFormattedMessage());
            assertNotNull(event.getThrowableProxy());
            assertEquals(IOException.class.getName(), event.getThrowableProxy().getClassName());
            assertEquals(failure.getMessage(), event.getThrowableProxy().getMessage());
            assertTrue(event.getThrowableProxy().getStackTraceElementProxyArray().length > 0);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void reviewedThirdPartyAndIoBoundariesUseStableMessagesAndThrowableLogs() throws Exception {
        List<BoundaryContract> contracts = List.of(
                new BoundaryContract("service/impl/finance/FinanceRechargerServiceImpl.java",
                        List.of("退款处理失败，请稍后重试", "重新退款失败，请稍后重试"), 2),
                new BoundaryContract("service/impl/ChannelOaCallBackServiceImpl.java",
                        List.of("公众号消息回调处理失败，请稍后重试"), 1),
                new BoundaryContract("controller/CrontabController.java",
                        List.of("Cron表达式解析失败，请检查后重试"), 1),
                new BoundaryContract("common/plugin/delivery/engine/Kdniao.java",
                        List.of("物流信息查询失败，请稍后重试"), 1),
                new BoundaryContract("common/plugin/delivery/engine/Kd100.java",
                        List.of("物流信息查询失败，请稍后重试"), 1),
                new BoundaryContract("common/plugin/storage/engine/AliyunStorage.java",
                        List.of("阿里云存储上传失败，请稍后重试"), 2),
                new BoundaryContract("common/plugin/storage/engine/QcloudStorage.java",
                        List.of("腾讯云存储上传失败，请稍后重试"), 2),
                new BoundaryContract("common/plugin/storage/engine/QiniuStorage.java",
                        List.of("七牛云存储上传失败，请稍后重试"), 1),
                new BoundaryContract("common/plugin/storage/engine/LocalStorage.java",
                        List.of("本地文件上传失败，请稍后重试"), 1));

        for (BoundaryContract contract : contracts) {
            String source = Files.readString(Path.of("src/main/java/com/houjiang/merchant")
                    .resolve(contract.relativePath()));
            assertTrue(!source.matches("(?s).*new\\s+OperateException\\([^;]*getMessage\\(\\).*"),
                    contract.relativePath() + " must not expose exception messages");
            for (String message : contract.publicMessages()) {
                assertTrue(source.contains("new OperateException(\"" + message + "\")"),
                        contract.relativePath() + " must use stable public message: " + message);
            }
            assertTrue(count(source, "log.error(") >= contract.throwableLogCount(),
                    contract.relativePath() + " must log each reviewed throwable");
        }
    }

    private static int count(String value, String needle) {
        return (value.length() - value.replace(needle, "").length()) / needle.length();
    }

    private record BoundaryContract(
            String relativePath,
            List<String> publicMessages,
            int throwableLogCount) {
    }
}
