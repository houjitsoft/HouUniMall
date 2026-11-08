package com.houjiang.thirdparty.service;

import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.util.ResponseResult;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Amos
 */
public interface OssServer {

    /**
     * 上传图片或文档 base64 格式传输
     *
     * @param param
     * @return
     */
    ResponseResult uploadToOssBase64(OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;

    /**
     * 上传视频并返回图片封面 base64格式传输
     * @param param
     * @return
     * @throws MinioException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    ResponseResult uploadVideoToOssBase64(OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;

    /**
     * 上传图片或视频  文件 格式传输
     *
     * @param file
     * @return
     */
    ResponseResult uploadOssToFile(MultipartFile file) throws MinioException;
    /**
     * 上传图片或视频  文件 格式传输 返回Md5
     *
     * @param file
     * @return
     */
    ResponseResult uploadToOssFileMd5(MultipartFile file) throws MinioException;

    /**
     * 上传png图片 图片必须等高
     *
     * @param param
     * @return
     * @throws IOException
     */
    ResponseResult uploadPngToOss(OssUploadUpdateParamDTO param) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException ;


    /**
     * 微信支付证书上传
     * @param file
     * @param token
     * @return
     */
   ResponseResult  fileUpload2(MultipartFile file,String token);

    /**
     * 获取OSS资源的预签名URL
     *
     * @param fileName 文件名称
     * @return 资源的预签名URL，用于临时访问该资源
     */
    Map<String,String> getPresignedUrl(String fileName);

    /**
     * 图片空间获取OSS资源的预签名URL
     *
     * @param fileName 请求对象
     * @return 资源的预签名URL，用于临时访问该资源
     */
    Map<String, String> getPresignedUrlToShopImageSpace(String fileName,String token);

}
