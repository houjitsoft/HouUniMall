package com.houjiang.thirdparty.service.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.UUIDUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.utils.Base64Utils;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 阿里云 OSS 客户端封装，与 MinIO、腾讯云 COS 使用相同的 base 配置字段（ossEndpoint 等）。
 *
 * @author Amos
 */
@Service
public class AliyunOssClientServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunOssClientServiceImpl.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;
    public static String FOLDER;

    /**
     * 从 base 服务加载 OSS 配置并拼装目录前缀。
     *
     * @param isShopImageSpace 是否店铺图片空间目录
     */
    public void init(Boolean isShopImageSpace) {
        ResponseResult resultResp = baseServiceClient.selectOSSInfo(CommonType.ossType.ALIYUN.getDesc());
        if (resultResp == null || resultResp.getData() == null) {
            LOGGER.warn("阿里云 OSS 配置为空");
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) resultResp.getData();
        LOGGER.info("=== aliyun oss {}", map);
        ENDPOINT = String.valueOf(map.get("ossEndpoint"));
        ACCESS_KEY_ID = String.valueOf(map.get("ossAccessKeyId"));
        ACCESS_KEY_SECRET = String.valueOf(map.get("ossAccessKeySecret"));
        BUCKET_NAME = String.valueOf(map.get("ossBacketName"));
        FOLDER = map.get("ossFolder") != null ? String.valueOf(map.get("ossFolder")) : "";
        if (Boolean.TRUE.equals(isShopImageSpace)) {
            FOLDER = FOLDER + "ImageSpace";
        } else {
            Date date = new Date();
            if (StringUtils.isEmpty(FOLDER)) {
                FOLDER = new SimpleDateFormat("yyyy/MM/dd/").format(date);
            } else {
                FOLDER = FOLDER + "/" + new SimpleDateFormat("yyyy/MM/dd/").format(date);
            }
        }
    }

    /**
     * 通过文件名判断并获取上传时的类型片段（与 {@link MinioOssClientServiceImpl} 保持一致）。
     *
     * @param fileExtension 文件名或 MIME 片段
     * @return 归一化扩展名
     */
    public static String getContentType(String fileExtension) {
        return MinioOssClientServiceImpl.getContentType(fileExtension);
    }

    /**
     * 从流名称解析文件后缀（与 MinIO Base64 上传逻辑一致）。
     *
     * @param streamName 流名称
     * @return 后缀片段
     */
    public static String getSuffixName(String streamName) {
        return MinioOssClientServiceImpl.getContentType(streamName);
    }

    /**
     * 构建 OSS 客户端（会 init 配置）。
     *
     * @return 可关闭的 OSS 客户端
     */
    private OSS buildClient() {
        init(false);
        return new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
    }

    /**
     * 拼接与 MinIO/Tencent 一致的对外访问 URL：Endpoint/Bucket/objectKey
     *
     * @param objectName 对象键
     * @return 访问 URL
     */
    private String buildPublicUrl(String objectName) {
        if (ENDPOINT == null) {
            return "";
        }
        return ENDPOINT + "/" + BUCKET_NAME + "/" + objectName;
    }

    /**
     * Multipart 上传至阿里云 OSS。
     *
     * @param file 上传文件
     * @return 文件访问 URL
     * @throws MinioException 兼容原接口签名，上传失败时抛出
     */
    public String uploadFile(MultipartFile file) throws MinioException {
        OSS ossClient = buildClient();
        try {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)) {
                throw new RuntimeException("文件名不能为空");
            }
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }
            String unique = UUID.randomUUID().toString().replace("-", "") + fileExtension;
            long timeMillis = System.currentTimeMillis();
            String objectName = FOLDER + timeMillis + "_" + unique;

            ObjectMetadata metadata = new ObjectMetadata();
            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }
            metadata.setContentLength(file.getSize());

            try (InputStream in = file.getInputStream()) {
                ossClient.putObject(BUCKET_NAME, objectName, in, metadata);
            }
            String url = buildPublicUrl(objectName);
            LOGGER.info("阿里云 OSS 访问路径: {}", url);
            return url;
        } catch (IOException e) {
            throw new MinioException(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * Base64 流上传至阿里云 OSS。
     *
     * @param param Base64 与流名称等
     * @return 文件访问 URL
     */
    public String uploadToOss(OssUploadUpdateParamDTO param)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        OSS ossClient = buildClient();
        try {
            byte[] decode = Base64Utils.decode(param.getBase64Str());
            String suffix = getSuffixName(param.getStreamName());
            String objectName = FOLDER + UUIDUtils.getUUID() + suffix;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(decode.length);
            ossClient.putObject(BUCKET_NAME, objectName, new ByteArrayInputStream(decode), metadata);

            String url = buildPublicUrl(objectName);
            LOGGER.debug("阿里云 OSS url >>> {}", url);
            return url;
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成预上传 URL（PUT）与预期文件访问路径。
     *
     * @param fileName 原始文件名（取后缀）
     * @return uploadUrl、fileUrl；失败返回 null
     */
    public Map<String, String> getPresignedUrl(String fileName) {
        OSS ossClient = null;
        try {
            ossClient = buildClient();
            int lastDot = fileName.lastIndexOf('.');
            String fileExtension = lastDot >= 0 ? fileName.substring(lastDot) : "";
            String objectName = FOLDER + UUID.randomUUID() + fileExtension;

            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, objectName, HttpMethod.PUT);
            request.setExpiration(expiration);
            URL presignedUrl = ossClient.generatePresignedUrl(request);

            Map<String, String> map = new HashMap<>();
            map.put("uploadUrl", presignedUrl.toString());
            map.put("fileUrl", ENDPOINT + "/" + objectName);
            return map;
        } catch (Exception e) {
            LOGGER.error("阿里云预签名 URL 生成失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 店铺图片空间预签名上传。
     *
     * @param fileName 文件名
     * @param ui       当前用户信息
     * @return uploadUrl、fileUrl；失败返回 null
     */
    public Map<String, String> getPresignedUrlToShopImageSpace(String fileName, UserInfoVO ui) {
        OSS ossClient = null;
        try {
            init(true);
            ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
            String objectName = FOLDER + "/shopId" + ui.getShopId() + "AndShopType" + ui.getShopType() + "/" + fileName;

            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, objectName, HttpMethod.PUT);
            request.setExpiration(expiration);
            URL presignedUrl = ossClient.generatePresignedUrl(request);

            Map<String, String> map = new HashMap<>();
            map.put("uploadUrl", presignedUrl.toString());
            map.put("fileUrl", ENDPOINT + "/" + objectName);
            return map;
        } catch (Exception e) {
            LOGGER.error("阿里云店铺图片空间预签名失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
