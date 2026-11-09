package com.houjiang.thirdparty.service.impl;

import cn.hutool.core.lang.UUID;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.UUIDUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.utils.Base64Utils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Amos
 */
@Service
public class MinioOssClientServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioOssClientServiceImpl.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * MinIO S3 API 地址（用于建连，通常来自 ossRegion）。
     */
    public static String API_ENDPOINT;
    /**
     * 公网访问前缀（用于返回 URL，通常来自 ossEndpoint）。
     */
    public static String PUBLIC_BASE_URL;
    /**
     * @deprecated 兼容旧代码：等同 API_ENDPOINT；getUrl 已改用 PUBLIC_BASE_URL。
     */
    @Deprecated
    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;
    /** 配置中的基础目录（不含日期） */
    public static String FOLDER_BASE;
    /** 实际上传目录（含日期或 ImageSpace） */
    public static String FOLDER;

    public void init(Boolean isShopImageSpace) {
        ResponseResult resultResp = baseServiceClient.selectOSSInfo(CommonType.ossType.MINIO.getDesc());
        Map<String, Object> map = resultResp == null ? null : (Map<String, Object>) resultResp.getData();
        if (map == null) {
            return;
        }
        LOGGER.info("===oss{}", map);
        String configuredEndpoint = stringVal(map.get("ossEndpoint"));
        String configuredRegion = stringVal(map.get("ossRegion"));
        applyEndpointConfig(configuredRegion, configuredEndpoint);
        ACCESS_KEY_ID = stringVal(map.get("ossAccessKeyId"));
        ACCESS_KEY_SECRET = stringVal(map.get("ossAccessKeySecret"));
        BUCKET_NAME = stringVal(map.get("ossBacketName"));
        FOLDER_BASE = normalizeFolderBase(stringVal(map.get("ossFolder")));
        if (Boolean.TRUE.equals(isShopImageSpace)) {
            FOLDER = joinPath(FOLDER_BASE, "ImageSpace");
        } else {
            String day = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            FOLDER = joinPath(FOLDER_BASE, day) + "/";
        }
    }

    /**
     * 按库表约定解析：ossRegion 优先作 API，ossEndpoint 优先作公网访问域名。
     */
    static void applyEndpointConfig(String ossRegion, String ossEndpoint) {
        String api = firstHttpUrl(ossRegion, ossEndpoint);
        String pub = firstHttpUrl(ossEndpoint, ossRegion);
        if (api == null || api.isBlank()) {
            throw new IllegalStateException("MinIO 未配置 API 地址（ossRegion 或 ossEndpoint）");
        }
        API_ENDPOINT = stripTrailingSlash(api);
        PUBLIC_BASE_URL = stripTrailingSlash(pub != null && !pub.isBlank() ? pub : api);
        ENDPOINT = API_ENDPOINT;
    }

    static String resolvePublicObjectUrl(String publicBaseUrl, String bucketName, String objectName) {
        String base = stripTrailingSlash(publicBaseUrl);
        String bucket = bucketName == null ? "" : bucketName.trim();
        String object = objectName == null ? "" : objectName.replaceAll("^/+", "");
        if (base == null || base.isBlank() || bucket.isBlank() || object.isBlank()) {
            return "";
        }
        return base + "/" + bucket + "/" + object;
    }

    static String normalizeFolderBase(String folder) {
        if (folder == null || folder.isBlank()) {
            return "";
        }
        return folder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }

    static String joinPath(String left, String right) {
        String a = left == null ? "" : left.replaceAll("/+$", "");
        String b = right == null ? "" : right.replaceAll("^/+", "");
        if (a.isBlank()) {
            return b;
        }
        if (b.isBlank()) {
            return a;
        }
        return a + "/" + b;
    }

    private static String firstHttpUrl(String primary, String fallback) {
        if (isHttpUrl(primary)) {
            return primary.trim();
        }
        if (isHttpUrl(fallback)) {
            return fallback.trim();
        }
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private static boolean isHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static String stripTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("/+$", "");
    }

    private static String stringVal(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public MinioClient getMinioClient() throws MinioException {
        init(false);
        try {
            return MinioClient.builder()
                .endpoint(API_ENDPOINT)
                .credentials(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
                .build();
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file) throws MinioException {
        MinioClient minioClient = getMinioClient();
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new RuntimeException("文件名不能为空");
        }
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        long timeMillis = System.currentTimeMillis();
        String objectName = FOLDER + timeMillis + "_" + fileName;
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
        String url = getUrl(BUCKET_NAME, objectName);
        LOGGER.info("访问网址路径:{}", url);
        return url;
    }

    public static String getUrl(String bucketName, String fileName) {
        try {
            String base = PUBLIC_BASE_URL != null && !PUBLIC_BASE_URL.isBlank() ? PUBLIC_BASE_URL : ENDPOINT;
            return resolvePublicObjectUrl(base, bucketName, fileName);
        } catch (Exception e) {
            LOGGER.warn("构造 MinIO 访问 URL 失败: {}", e.getMessage());
        }
        return "";
    }

    public String uploadToOss(OssUploadUpdateParamDTO param)
        throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        MinioClient minioClient = getMinioClient();
        byte[] decode = Base64Utils.decode(param.getBase64Str());
        String fileName = getContentType(param.getStreamName());
        fileName = FOLDER + UUIDUtils.getUUID() + fileName;
        Long fileSize = (long) decode.length;
        minioClient.putObject(
            PutObjectArgs.builder().bucket(BUCKET_NAME).object(fileName)
                .stream(new ByteArrayInputStream(decode), fileSize, -1)
                .build());
        String url = getUrl(BUCKET_NAME, fileName);
        LOGGER.debug("url>>>>{}", url);
        return url;
    }

    public String uploadToOssPictureData(OssUploadUpdateParamDTO pictureData) throws MinioException {
        MinioClient minioClient = getMinioClient();
        String fileName = FOLDER + System.currentTimeMillis() + ".jpg";
        try {
            byte[] data = pictureData.getFileBytes();
            minioClient.putObject(
                PutObjectArgs.builder().bucket(BUCKET_NAME).object(fileName)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .build());
            String url = getUrl(BUCKET_NAME, fileName);
            LOGGER.debug("url>>>>{}", url);
            return url;
        } catch (Exception e) {
            LOGGER.warn("MinIO 图片上传失败", e);
            return "";
        }
    }

    public static String getContentType(String fileExtension) {
        if (".vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(fileExtension)) {
            return ".xlsx";
        }
        if (".vnd.ms-excel".equalsIgnoreCase(fileExtension)) {
            return ".xls";
        }
        if (".msword".equalsIgnoreCase(fileExtension)) {
            return ".doc";
        }
        if (".vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(fileExtension)) {
            return ".docx";
        }
        return fileExtension;
    }

    /**
     * 生成「前端直传」用的上传地址。
     *
     * <p>2026-11-09 修复：原实现把 bucket 的公开访问地址同时当成上传地址返回，等于让浏览器
     * 匿名 PUT 对象存储，MinIO 一律返回 {@code 403 AccessDenied}，商家入驻的身份证/协议上传
     * 必然失败（且从报错看不出原因）。这里改为真正调用 MinIO 的 GetPresignedObjectUrl（method=PUT）
     * 生成带签名的临时上传地址，有效期 2 小时；{@code fileUrl} 仍返回对象的公开访问地址供页面展示。
     *
     * @param fileName 原始文件名（只用其扩展名，对象名由服务端生成，避免覆盖与路径穿越）
     * @return uploadUrl 预签名 PUT 地址、fileUrl 公开访问地址
     */
    public Map getPresignedUrl(String fileName) {
        init(false);
        Map<String, String> map = new HashMap<>();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String objectName = FOLDER + java.util.UUID.randomUUID() + fileExtension;
        String fileUrl = getUrl(BUCKET_NAME, objectName);
        map.put("uploadUrl", presignedPutUrl(objectName, fileUrl));
        map.put("fileUrl", fileUrl);
        return map;
    }

    /**
     * 生成 MinIO 预签名 PUT 地址；SDK 异常时回退为公开地址（与旧行为一致，至少不返回空串）。
     */
    private String presignedPutUrl(String objectName, String fallbackUrl) {
        try {
            return getMinioClient().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .expiry(2, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            LOGGER.warn("生成 MinIO 预签名上传地址失败，回退公开地址: {}", e.getMessage());
            return fallbackUrl;
        }
    }

    public Map<String, String> getPresignedUrlToShopImageSpace(String fileName, UserInfoVO ui) {
        init(true);
        Map<String, String> map = new HashMap<>();
        String objectName = joinPath(FOLDER, "shopId" + ui.getShopId() + "AndShopType" + ui.getShopType())
            + "/" + fileName;
        String fileUrl = getUrl(BUCKET_NAME, objectName);
        map.put("uploadUrl", fileUrl);
        map.put("fileUrl", fileUrl);
        return map;
    }
}
