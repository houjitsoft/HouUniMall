package com.houjiang.thirdparty.service.impl;

import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.FileUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.service.OssServer;
import com.houjiang.thirdparty.utils.Base64Utils;
import io.minio.errors.MinioException;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Amos
 */
@Service
public class OssServerImpl implements OssServer {

    @Autowired
    private AliyunOssClientServiceImpl aliyunOssClientService;

    @Autowired
    private MinioOssClientServiceImpl minioOssClientService;

    @Autowired
    private TencentCosClientServiceImpl tencentCosClientService;

    @Autowired
    private BaseServiceClient baseServiceClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(OssServerImpl.class);
    /**
     * 微信支付证书上传路径
     */
    @Value(value = "${crtUploadPath}")
    private String crtUploadPath;

    @Override
    public ResponseResult uploadToOssBase64(OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        // 根据不同的OSS类型，选择不同的上传策略
        String url;
        // 查询当前配置的OSS类型
        String type = baseServiceClient.queryOssStates();
        // 判断当前OSS类型，分别处理阿里云OSS、MinIO和腾讯COS的上传
        if(CommonType.ossType.ALIYUN.getDesc().equals(type)){
            // 阿里云OSS上传，返回上传后的URL
            url = aliyunOssClientService.uploadToOss(param);
        }else if(CommonType.ossType.MINIO.getDesc().equals(type)){
            // MinIO上传，返回上传后的URL
            url = minioOssClientService.uploadToOss(param);
        }else {
            // 腾讯COS上传，返回上传后的URL
            url = tencentCosClientService.uploadToOss(param);
        }
        // 构造上传成功的响应结果，包含状态码、消息和上传后的URL
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), url);

    }

    /**
     * 根据不同的OSS类型，上传视频和视频封面到对应的OSS服务器。
     *
     * @param param 包含视频Base64编码和相关信息的参数对象。
     * @return 返回上传结果，包括视频URL和视频封面URL。
     * @throws MinioException、IOException、NoSuchAlgorithmException、InvalidKeyException 如果上传过程中遇到异常。
     */
    @Override
    public ResponseResult uploadVideoToOssBase64(OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        // 用于存储视频URL和视频封面URL
        Map<String,String> map = new HashMap<>();
        // 视频URL和视频封面URL初始化为null
        String videoUrl = null;
        String videoCoverUrl = null;
        // 查询当前使用的OSS类型
        String type = baseServiceClient.queryOssStates();
        // 根据不同的OSS类型进行不同的上传处理
        if(CommonType.ossType.ALIYUN.getDesc().equals(type)){
            // 如果是阿里云OSS
            // 处理Base64字符串，去掉前缀
            if(param.getBase64Str().startsWith("data:image/png;base64,")){
                param.setBase64Str(param.getBase64Str().substring("data:image/png;base64,".length()));
            }
            // 上传视频
            videoUrl = aliyunOssClientService.uploadToOss(param);
            //抓取视频封面 并上传
            if(null != videoUrl){
                String videoFrameBase64 = getVideoFrame(videoUrl);
                if(null != videoFrameBase64 ){
                    // 设置视频封面的Base64字符串和文件名后缀
                    param.setBase64Str(videoFrameBase64);
                    param.setStreamName(".jpg");
                    // 上传视频封面
                    videoCoverUrl = aliyunOssClientService.uploadToOss(param);
                }
            }
        }else if(CommonType.ossType.MINIO.getDesc().equals(type)){
            // 如果是MinIO
            // 上传视频
            videoUrl = minioOssClientService.uploadToOss(param);
            // 如果视频URL不为空，尝试上传视频封面
            //抓取视频封面 并上传
            if(null != videoUrl){
                String videoFrameBase64 = getVideoFrame(videoUrl);
                if(null != videoFrameBase64){
                    // 设置视频封面的Base64字符串和文件名后缀
                    param.setBase64Str(videoFrameBase64);
                    param.setStreamName(".jpg");
                    // 上传视频封面
                    videoCoverUrl = minioOssClientService.uploadToOss(param);
                }
            }
        }else {
            // 如果是腾讯云COS
            // 处理Base64字符串，去掉前缀
            if(param.getBase64Str().startsWith("data:image/png;base64,")){
                param.setBase64Str(param.getBase64Str().substring("data:image/png;base64,".length()));
            }
            // 上传视频
            videoUrl = tencentCosClientService.uploadToOss(param);
            // 如果视频URL不为空，尝试上传视频封面
            //抓取视频封面 并上传
            if(null != videoUrl){
                String videoFrameBase64 = getVideoFrame(videoUrl);
                if(null != videoFrameBase64 ){
                    // 设置视频封面的Base64字符串和文件名后缀
                    param.setBase64Str(videoFrameBase64);
                    param.setStreamName(".jpg");
                    // 上传视频封面
                    videoCoverUrl = tencentCosClientService.uploadToOss(param);
                }
            }
        }
        // 将视频URL和视频封面URL存入map中
        map.put("videoUrl",videoUrl);
        map.put("videoCoverUrl",videoCoverUrl);
        // 返回上传结果
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), map);
    }

    /**
     * 视频帧抓取器
     * 如需在windows上执行  需要在 pom.xml 中 打开<classifier>windows-x86_64</classifier>注释 注释liunx
     * 根据运行系统打开或关闭对应 注释 防止jar包过大
     * @param videoUrl
     * @return
     */
    public String getVideoFrame(String videoUrl){
       //视频帧抓取器
        FFmpegFrameGrabber frameGrabber = null;
        byte[] data = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            frameGrabber = new FFmpegFrameGrabber(videoUrl);
            //开始
            frameGrabber.start();
            //获取到帧长度
            int length = frameGrabber.getLengthInFrames();
            int i = 0;
            Frame frame = null;
            while (i < length) {
                // 过滤前5帧,防止获取到黑屏画面
                frame = frameGrabber.grabFrame();
                if ((i > 5) && (frame.image != null)) {
                    break;
                }
                i++;
            }
            //Java2DFrameConverter可以对Frame进行相互转换操作
            BufferedImage bi = new Java2DFrameConverter().getBufferedImage(frame);
            ImageIO.write(bi, "jpg", outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (frameGrabber != null) {
                    frameGrabber.stop();
                }
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
        
        String imageBase64 = Base64Utils.encode(outputStream.toByteArray());
        return imageBase64;
    }
    /**
     * 本地文件存储根路径（当OSS不可用时作为回退方案）
     */
    @Value(value = "${local.upload.path:D:/wwwroot/houcloud/public/uploads}")
    private String localUploadPath;

    @Override
    public ResponseResult uploadOssToFile(MultipartFile file) throws MinioException  {
        String url;
        // 根据不同的OSS类型，选择不同的客户端进行文件上传
        String type = baseServiceClient.queryOssStates();
        LOGGER.info("当前OSS类型: {}", type);
        
        try {
            if(CommonType.ossType.ALIYUN.getDesc().equals(type)){
                // 使用阿里云OSS客户端上传文件
                url = aliyunOssClientService.uploadFile(file);
            }else if (CommonType.ossType.MINIO.getDesc().equals(type)){
                // 使用MinIO OSS客户端上传文件
                url = minioOssClientService.uploadFile(file);
            }else {
                // 使用腾讯COS客户端上传文件
                url = tencentCosClientService.uploadFile(file);
            }
        } catch (Exception e) {
            LOGGER.warn("OSS上传异常，自动切换到本地存储。OSS类型: {}, 错误: {}", type, e.getMessage());
            url = null;
        }
        
        // OSS上传失败时，回退到本地存储
        if (url == null || url.isEmpty()) {
            LOGGER.warn("OSS上传返回空URL，回退到本地存储。OSS类型: {}, 文件名: {}", type, file.getOriginalFilename());
            url = uploadToLocalStorage(file);
        }
        
        // 本地存储也失败
        if (url == null || url.isEmpty()) {
            LOGGER.error("文件上传失败（OSS和本地存储均失败）。文件名: {}", file.getOriginalFilename());
            return ResponseResult.fail(BizCodeEnume.UPLOAD_ERROR.getCode(), "文件上传失败，OSS配置异常且本地存储不可用");
        }
        
        // 返回上传成功的响应结果
        LOGGER.info("文件上传成功，URL: {}", url);
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), url);
    }

    /**
     * 本地文件存储（OSS不可用时的回退方案）
     * 将文件保存到本地磁盘，返回相对访问路径
     *
     * @param file 上传的文件
     * @return 文件访问URL（相对路径），失败返回null
     */
    private String uploadToLocalStorage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;

            // 确保上传目录存在
            java.io.File dir = new java.io.File(localUploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            java.io.File destFile = new java.io.File(localUploadPath, newFileName);
            file.transferTo(destFile);

            String fileUrl = "/uploads/" + newFileName;
            LOGGER.info("文件已保存到本地存储: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            LOGGER.error("本地文件存储失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ResponseResult uploadToOssFileMd5(MultipartFile file) throws MinioException {
        Map<String,Object> result = new HashMap<>();
        String url;
        // 根据不同的OSS类型，选择不同的客户端进行文件上传
        String type = baseServiceClient.queryOssStates();
        if(CommonType.ossType.ALIYUN.getDesc().equals(type)){
            // 使用阿里云OSS客户端上传文件
            url = aliyunOssClientService.uploadFile(file);
        }else if (CommonType.ossType.MINIO.getDesc().equals(type)){
            // 使用MinIO OSS客户端上传文件
            url = minioOssClientService.uploadFile(file);
        }else {
            // 使用腾讯COS客户端上传文件
            url = tencentCosClientService.uploadFile(file);
        }
        String md5 = null;
        try {
            md5 = getMD5(file);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        result.put("url",url);
        result.put("md5",md5);
        // 返回上传成功的响应结果，包括状态码、消息和上传文件的URL
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), result);

    }
    public static String getMD5(MultipartFile multipartFile) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = multipartFile.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    @Override
    public ResponseResult uploadPngToOss(OssUploadUpdateParamDTO param) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        String url;
        // 通过基础服务客户端查询OSS状态
        String type = baseServiceClient.queryOssStates();

        // 根据查询到的OSS类型，选择不同的OSS客户端进行文件上传
        if (CommonType.ossType.ALIYUN.getDesc().equals(type)) {
            url = aliyunOssClientService.uploadToOss(param);
        } else if (CommonType.ossType.MINIO.getDesc().equals(type)) {
            url = minioOssClientService.uploadToOss(param);
        } else {
            // 默认情况下，使用腾讯COS进行文件上传
            url = tencentCosClientService.uploadToOss(param);
        }

        try {
            if(null!=url){
                // 从上传后的URL读取图片，以获取原始图片的尺寸
                BufferedImage sourceImg = ImageIO.read(new URL(url).openStream());
                // 宽度
                int width = sourceImg.getWidth();
                // 高度
                int height = sourceImg.getHeight();
                // 检查上传的图片是否为正方形，如果是，则返回上传成功的响应
                if (width == height) {
                    return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.UPLOAD_SUCCESS.getMsg(), url);
                }
            }
            // 如果图片不是正方形，则返回上传失败的响应
            return ResponseResult.fail(BizCodeEnume.EQUAL_ALTITUDE.getCode(), BizCodeEnume.EQUAL_ALTITUDE.getMsg());
        } catch (IOException e) {
            // 异常处理：返回上传失败的响应
            return ResponseResult.fail(BizCodeEnume.UPLOAD_ERROR.getCode(), BizCodeEnume.UPLOAD_ERROR.getMsg());
        }
    }


  @Override
public ResponseResult fileUpload2(MultipartFile file, String token) {
    UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
    if (userInfo == null) {
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }
    String shopId = userInfo.getShopId();

    // 保存路径
    String path = crtUploadPath + "/" + shopId;

    // 验证路径安全性
    // 假设 isValidPath 已经被正确定义
    if (!isValidPath(path)) {
        return ResponseResult.fail("Invalid path");
    }

    // 创建目录（如果不存在）
    File directory = new File(path);
    if (!directory.exists() && !directory.mkdirs()) {
        return ResponseResult.fail("Failed to create directory");
    }

    // 生成唯一文件名
    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null) {
        return ResponseResult.fail("File name is null");
    }
    String uniqueFileName = FileUtils.generateUniqueFileName(originalFileName);
    path = path + "/" + uniqueFileName;
    try {
        if(null!=path){
            File newFile = new File(path);
            // 保存文件
            file.transferTo(newFile);
        }

    } catch (IOException e) {
        // 更详细的异常处理
        LOGGER.error("Failed to transfer file [{}]. Error message: {}", originalFileName, e.getMessage(), e);
        return ResponseResult.fail("Failed to save the file: " + e.getMessage());
    } catch (NullPointerException e) {
        // 处理空指针异常
        LOGGER.error("NullPointerException occurred while transferring file [{}]. Error message: {}", originalFileName, e.getMessage(), e);
        return ResponseResult.fail("Failed to save the file due to NullPointerException: " + e.getMessage());
    }

    return ResponseResult.success(1, "文件创建成功", path);
}


    /** 假设 isValidPath 方法如下所示 */
    private boolean isValidPath2(String path) {
        Path p = Paths.get(path);
        try {
            if (!Files.exists(p.getParent())) {
                // 父目录不存在
                return false;
            }
            if (!p.normalize().startsWith(crtUploadPath)) {
                // 防止路径遍历攻击
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Error validating path: {}", path, e);
            return false;
        }
    }

/** 辅助方法：验证路径是否安全 */
    private boolean isValidPath(String path) {
        Path absolutePath = Paths.get(path);
        Path relativePath = Paths.get(crtUploadPath).relativize(absolutePath);
        return !relativePath.isAbsolute() && !relativePath.toString().contains("..");
    }


    /**
     * 获取OSS资源的预签名URL
     *
     * @param fileName 文件名称
     * @return 资源的预签名URL，用于临时访问该资源
     */
    @Override
    public Map<String,String> getPresignedUrl(String fileName) {
        Map<String,String> map = new HashMap<>();
        // 通过基础服务客户端查询OSS（对象存储服务）的状态
        String type = baseServiceClient.queryOssStates();
        // 根据查询到的OSS类型，选择不同的OSS客户端进行文件上传
        if(CommonType.ossType.ALIYUN.getDesc().equals(type)){
            map = aliyunOssClientService.getPresignedUrl(fileName);
        }else if (CommonType.ossType.MINIO.getDesc().equals(type)){
            map = minioOssClientService.getPresignedUrl(fileName);
        }else {
            // 默认情况下，使用腾讯COS（云存储服务）进行文件上传
            map = tencentCosClientService.getPresignedUrl(fileName);
        }
        return map;
    }

    /**
     * 图片空间获取OSS资源的预签名URL
     *
     * @param fileName 请求对象
     * @return 资源的预签名URL，用于临时访问该资源
     */
    @Override
    public Map<String, String> getPresignedUrlToShopImageSpace(String fileName, String token) {
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
        Map<String, String> map;
        // 通过基础服务客户端查询OSS（对象存储服务）的状态
        String type = baseServiceClient.queryOssStates();
        // 根据查询到的OSS类型，选择不同的OSS客户端进行文件上传
        if (CommonType.ossType.ALIYUN.getDesc().equals(type)) {
            map = aliyunOssClientService.getPresignedUrlToShopImageSpace(fileName,  userInfo);
        } else if (CommonType.ossType.MINIO.getDesc().equals(type)) {
            map = minioOssClientService.getPresignedUrlToShopImageSpace(fileName,  userInfo);
        } else {
            // 默认情况下，使用腾讯COS（云存储服务）进行文件上传
            map = tencentCosClientService.getPresignedUrlToShopImageSpace(fileName,  userInfo);
        }
        return map;
    }

    /**
     * 删除给定文件或文件夹。
     * 如果文件存在，且为文件，则直接删除；如果是目录，则递归删除其下的所有文件和子目录。
     *
     * @param file 要删除的文件或目录
     * @throws IOException 如果删除文件失败，则抛出此异常
     */
    public static void deleteIfExists(File file) throws IOException {
        // 创建一个文件栈，用于存储待删除的文件
        Stack<File> filesToDelete = new Stack<>();
        // 将初始文件添加到栈中
        filesToDelete.push(file);

        // 当文件栈不为空时，循环删除文件
        while (!filesToDelete.isEmpty()) {
            // 从栈中弹出一个文件
            File currentFile = filesToDelete.pop();

            // 检查文件是否存在
            if (currentFile.exists()) {
                // 如果是目录，则获取该目录下的所有文件
                if (currentFile.isDirectory()) {
                    File[] subFiles = currentFile.listFiles();
                    // 如果目录下有文件，则将这些文件加入到待删除的栈中
                    if (subFiles != null) {
                        for (File subFile : subFiles) {
                            filesToDelete.push(subFile);
                        }
                    }
                }

                // 尝试删除文件，如果删除失败，则抛出IOException异常
                if (!currentFile.delete()) {
                    throw new IOException("Delete file failure, path: " + currentFile.getAbsolutePath() + ", error: " + new FileNotFoundException(currentFile.getAbsolutePath()).toString());
                }
            }
        }
    }

}
