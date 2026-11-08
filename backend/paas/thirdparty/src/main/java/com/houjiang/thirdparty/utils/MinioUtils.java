package com.houjiang.thirdparty.utils;


import cn.hutool.core.lang.UUID;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * MinIO 工具类（配置从数据库动态加载）
 *
 * @Description: MinIO文件上传工具
 * @Author Amos
 * @CreateTime: 2023-07-17  11:13
 * @Version: 4.0
 * @author Amos
 */
@Component
public class MinioUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioUtils.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    private MinioClient buildMinioClient() throws MinioException {
        ResponseResult resultResp = baseServiceClient.selectOSSInfo(CommonType.ossType.MINIO.getDesc());
        Map<String, Object> map = (Map<String, Object>) resultResp.getData();
        if (map == null) {
            throw new MinioException("MinIO配置未找到，请先在后台配置 MinIO 存储信息");
        }
        String endpoint = map.get("ossEndpoint").toString();
        String accessKey = map.get("ossAccessKeyId").toString();
        String secretKey = map.get("ossAccessKeySecret").toString();
        try {
            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    /**
     * 表单提交file上传到 MinIO
     *
     * @param file 上传的文件
     * @return 文件对象名
     */
    public String upload(MultipartFile file) throws MinioException {
        ResponseResult resultResp = baseServiceClient.selectOSSInfo(CommonType.ossType.MINIO.getDesc());
        Map<String, Object> map = (Map<String, Object>) resultResp.getData();
        if (map == null) {
            throw new MinioException("MinIO配置未找到");
        }
        String bucket = map.get("ossBacketName").toString();
        String endpoint = map.get("ossEndpoint").toString();

        MinioClient minioClient = buildMinioClient();

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new RuntimeException("文件名不能为空");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;
        // 日期目录
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formatDatePath = formatter.format(now);
        // 加一个时间戳
        long timeMillis = System.currentTimeMillis();
        String objectName = formatDatePath + "/" + timeMillis + fileName;
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
        String url = endpoint + "/" + bucket + "/" + objectName;
        LOGGER.info("MinIO上传成功，访问路径: {}", url);
        return url;
    }
}
