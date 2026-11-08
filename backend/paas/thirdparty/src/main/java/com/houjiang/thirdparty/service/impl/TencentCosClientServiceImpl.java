package com.houjiang.thirdparty.service.impl;

import cn.hutool.core.lang.UUID;
import com.aliyun.oss.OSSException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.util.UUIDUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.utils.Base64Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class TencentCosClientServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TencentCosClientServiceImpl.class);

    @Autowired
    private BaseServiceClient baseServiceClient;

    /**
     * COS API的内或外网域名
     */
    public static String ENDPOINT;
    /**
     * COS API的密钥Access Key ID
     */
    public static String ACCESS_KEY_ID;
    /**
     * COS API的密钥Access Key Secret
     */
    public static String ACCESS_KEY_SECRET;
    /**
     * COS API的bucket名称
     */
    public static String BUCKET_NAME;
    /**
     * COS API的文件夹名称
     */
    public static String FOLDER;
    /**
     * 服务器集群地址
     */
    public static String REGION;

    public void init(Boolean isShopImageSpace) {
        ResponseResult resultResp = baseServiceClient.selectOSSInfo(CommonType.ossType.TENCENT_CLOUD.getDesc());
        Map<String, Object> map = (Map<String, Object>) resultResp.getData();
        if (null != map) {
            LOGGER.info("===oss" + map);
            //初始化SMS配置信息
            // 初始化腾讯云对象存储服务的配置参数
            // 从配置映射中获取并设置对象存储服务的终端节点
            TencentCosClientServiceImpl.ENDPOINT = map.get("ossEndpoint").toString();
            // 从配置映射中获取并设置访问密钥ID
            TencentCosClientServiceImpl.ACCESS_KEY_ID = map.get("ossAccessKeyId").toString();
            // 从配置映射中获取并设置访问密钥秘钥
            TencentCosClientServiceImpl.ACCESS_KEY_SECRET = map.get("ossAccessKeySecret").toString();
            // 从配置映射中获取并设置存储桶名称
            TencentCosClientServiceImpl.BUCKET_NAME = map.get("ossBacketName").toString();
            // 从配置映射中获取并设置存储桶中的目录
            TencentCosClientServiceImpl.FOLDER = map.get("ossFolder").toString();
            // 从配置映射中获取并设置存储桶所在的地域
            TencentCosClientServiceImpl.REGION = map.get("ossRegion").toString();
            if (isShopImageSpace) {
                TencentCosClientServiceImpl.FOLDER = TencentCosClientServiceImpl.FOLDER + "ImageSpace";
            }else {
                // 获取当前日期，用于生成文件夹路径
                Date date = new Date();
                // 检查TencentCosClientServiceImpl.FOLDER是否为空
                if (StringUtils.isEmpty(TencentCosClientServiceImpl.FOLDER)) {
                    // 如果为空，初始化为当前日期格式的字符串
                    TencentCosClientServiceImpl.FOLDER = new SimpleDateFormat("yyyy/MM/dd/").format(date);
                } else {
                    // 如果不为空，追加当前日期格式的字符串到已有路径
                    TencentCosClientServiceImpl.FOLDER = TencentCosClientServiceImpl.FOLDER + "/" + new SimpleDateFormat("yyyy/MM/dd/").format(date);
                }
            }
        }
    }

    /**
     * 获取CosOSS客户端对象
     *
     * @return ossClient
     */
    public COSClient getCosClient(Boolean isShopImageSpace) {
        init(isShopImageSpace);
        // 1. 设置用户身份信息。
        COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 2. 设置bucket的区域。
        Region region = new Region(REGION);

        // 3. 初始化客户端配置。
        ClientConfig clientConfig = new ClientConfig(region);
        // 设置HTTP协议，可选值为http或https，默认为http。
        clientConfig.setHttpProtocol(HttpProtocol.https);

        // 4. 初始化客户端。
        return new COSClient(cred, clientConfig);
    }

    public String uploadFile(MultipartFile file) {
        // 初始化COS客户端，用于后续的文件上传操作
        COSClient cosClient = getCosClient(false);

        // 获取上传文件的原始文件名
        String originalFilename = file.getOriginalFilename();

        // 检查原始文件名是否为空，如果为空则抛出运行时异常
        // 因为文件名是上传文件所必需的信息
        if (StringUtils.isEmpty(originalFilename)) {
            throw new RuntimeException();
        }

        // 生成新的文件名，采用UUID加上原始文件名的方式
        // 这样可以避免文件名重复，同时保留了原始文件名的信息
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;

        // 加一个时间戳
        long timeMillis = System.currentTimeMillis();
        String objectName = FOLDER + timeMillis + fileName;
        try (InputStream inputStream = file.getInputStream()) {
            // 创建元数据对象，可以设置内容类型等
            ObjectMetadata metadata = new ObjectMetadata();
            // 设置内容类型，根据实际情况调整
            metadata.setContentType(AliyunOssClientServiceImpl.getContentType(fileName));

            // 创建上传请求对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, inputStream, metadata);

            // 执行上传操作
            cosClient.putObject(putObjectRequest);
            // 关闭客户端
            cosClient.shutdown();
            LOGGER.info("访问网址路径:" + ENDPOINT + "/" + objectName);
            return ENDPOINT + "/" + objectName;
        } catch (OSSException oe) {
            LOGGER.info("Error Message: " + oe.getErrorCode());
            LOGGER.info("Error Code:    " + oe.getErrorCode());
            LOGGER.info("Request ID:    " + oe.getRequestId());
            LOGGER.info("Host ID:       " + oe.getHostId());
            return null;
        } catch (IOException e) {
            LOGGER.info("Error Message: " + e.getMessage());
            return null;
        }
    }

    /**
     * 上传文件
     *
     * @param param 上传文件信息
     * @return 文件访问路径
     */
    public String uploadToOss(OssUploadUpdateParamDTO param) {

        COSClient cosClient = getCosClient(false);
        // 解密
        byte[] decode = Base64Utils.decode(param.getBase64Str());

        //获取问价后缀
        String fileName = AliyunOssClientServiceImpl.getSuffixName(param.getStreamName());
        fileName = FOLDER + UUIDUtils.getUUID() + fileName;
        System.out.println(fileName);
        // 创建元数据对象，可以设置内容类型等
        ObjectMetadata metadata = new ObjectMetadata();
        // 设置内容类型，根据实际情况调整
        metadata.setContentType(AliyunOssClientServiceImpl.getContentType(fileName));

        // 将字节数组转换为输入流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode);

        // 创建上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, byteArrayInputStream, metadata);
        // 执行上传操作
        try {
            cosClient.putObject(putObjectRequest);
            // 关闭客户端
            cosClient.shutdown();
            LOGGER.debug("url>>>>" + ENDPOINT + "/" + fileName);
            return ENDPOINT + "/" + fileName;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String uploadToOssPictureData(OssUploadUpdateParamDTO pictureData) {
        // 创建Cos OSS 客户端
        COSClient cosClient = getCosClient(false);
        // 设置上传的文件名，可以根据实际需求进行调整
        String fileName =  FOLDER + System.currentTimeMillis()+".jpg";
        try {
            // 将 PictureData 转换为字节数组
            byte[] data = pictureData.getFileBytes();
            // 创建元数据对象，可以设置内容类型等
            ObjectMetadata metadata = new ObjectMetadata();
            // 设置内容类型，根据实际情况调整
            metadata.setContentType(AliyunOssClientServiceImpl.getContentType(fileName));

            // 将字节数组转换为输入流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

            // 创建上传请求对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, byteArrayInputStream, metadata);

            // 执行上传操作
            cosClient.putObject(putObjectRequest);
            // 关闭客户端
            cosClient.shutdown();
            System.out.println("图片上传成功！");
            return ENDPOINT + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("图片上传失败！");
            return "";
        }
    }

    public Map<String, String> getPresignedUrl(String fileName) {
        Map<String,String> map = new HashMap<>();
        COSClient cosClient = null;
        try {
            // 创建Cos OSS 客户端
             cosClient = getCosClient(false);
            // 拼接对象名称，使用UUID确保唯一性。
            String fileExtension = fileName.substring(fileName.lastIndexOf("."));
            String objectName = FOLDER + java.util.UUID.randomUUID() + fileExtension;

            // 生成预签名 URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, objectName, HttpMethodName.PUT);
            // 设置URL过期时间为10年 3600l* 1000*24*365*10
            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            request.setExpiration(expiration);
            URL presignedUrl = cosClient.generatePresignedUrl(request);

            map.put("uploadUrl",presignedUrl.toString());
            map.put("fileUrl",ENDPOINT + "/" + objectName);
            return map;
        } catch ( Exception e) {
            // 处理异常
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getPresignedUrlToShopImageSpace(String fileName, UserInfoVO ui) {
        // 定义COS客户端，初始值为null
        COSClient cosClient = null;
        try {
            // 创建Cos OSS 客户端
            cosClient = getCosClient(true);
            // 拼接对象名称，确保唯一性。
            String objectName = FOLDER + "/shopId" + ui.getShopId() + "AndShopType" + ui.getShopType() + "/" + fileName;

            return getUrlMap(cosClient, objectName);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
        // 如果发生异常，返回null
        return null;
    }

    /**
     * 生成包含预签名URL和文件URL的映射
     * 该方法用于创建一个预签名的URL，用于上传文件到COS，并获取文件的访问URL
     *
     * @param cosClient COS客户端，用于操作COS服务
     * @param objectName COS中的对象名称，即文件路径
     * @return 返回一个包含上传URL和文件URL的映射，如果生成失败则返回null
     */
    private Map<String, String> getUrlMap(COSClient cosClient, String objectName){
        // 初始化用于返回结果的Map
        Map<String, String> map = new HashMap<>();
        // 生成预签名 URL
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, objectName, HttpMethodName.PUT);
        // 设置URL过期时间为10年 3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        request.setExpiration(expiration);
        // 通过COS客户端生成预签名URL
        URL presignedUrl = cosClient.generatePresignedUrl(request);

        // 将生成的上传URL和对应的文件访问URL放入Map中
        map.put("uploadUrl", presignedUrl.toString());
        map.put("fileUrl", ENDPOINT + "/" + objectName);
        // 返回包含URL的Map
        return map;
    }

}
