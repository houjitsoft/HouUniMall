package com.houjiang.merchant.common.plugin.storage.engine;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.houjiang.merchant.common.exception.OperateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * 阿里云存储
 */
@Slf4j
public class AliyunStorage {

    /**
     * 存储配置
     */
    private final Map<String, String> config;

    /**
     * 构造方法
     */
    public AliyunStorage(Map<String, String> config) {
        this.config = config;
    }

    /**
     * 鉴权令牌
     *
     * @author Amos
     * @return String
     */
    public OSS ossClient() {
        String endpoint        = this.config.getOrDefault("endpoint", "https://oss-cn-shenzhen.aliyuncs.com");
        if ("".equals(endpoint)) {
            endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        }
        String accessKeyId     = this.config.get("access_key");
        String accessKeySecret = this.config.get("secret_key");
       return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件对象
     * @param key 文件名称 20220331/11.png
     */
    public void upload(MultipartFile multipartFile, String key) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    this.config.get("bucket"), key,
                    new ByteArrayInputStream(multipartFile.getBytes())
            );
            PutObjectResult result = this.ossClient().putObject(putObjectRequest);
        } catch (OSSException oe) {
            log.error("阿里云存储上传失败", oe);
            throw new OperateException("阿里云存储上传失败，请稍后重试");
        } catch (Exception ce) {
            log.error("阿里云存储上传失败", ce);
            throw new OperateException("阿里云存储上传失败，请稍后重试");
        } finally {
            if (this.ossClient() != null) {
                this.ossClient().shutdown();
            }
        }
    }

    public void delete(String key) {
        this.ossClient().deleteObject(this.config.get("bucket"), key);
    }

}
