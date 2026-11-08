package com.houjiang.thirdparty.controller;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SnowflakeIdWorker;
import com.houjiang.thirdparty.service.OssServer;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/web")
public class OssController {

    @Autowired
    private OssServer ossServer;

    /**
     * 获取OSS资源的预签名URL
     *
     * @param fileName 文件名称
     * @return 资源的预签名URL，用于临时访问该资源
     */
    @GetMapping("/oss/getPresignedUrl")
    public Map<String,String> getPresignedUrl(@RequestParam("fileName") String fileName) {
        return ossServer.getPresignedUrl(fileName);
    }

    /**
     * 图片空间获取OSS资源的预签名URL
     *
     * @param ossMap 请求对象
     * @return 资源的预签名URL，用于临时访问该资源
     */
    @PostMapping("/oss/getPresignedUrl/shopImageSpace")
    public Map<String, String> getPresignedUrlToShopImageSpace(@RequestBody Map<String, String> ossMap) {
        return ossServer.getPresignedUrlToShopImageSpace(ossMap.get("fileName"), ossMap.get("token"));
    }

    /**
     * 上传图片或文档 base64 格式传输
     *
     * @param param
     * @return
     */
    @PostMapping("/oss/upload")
    public ResponseResult uploadToOssBase64(@RequestBody OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return ossServer.uploadToOssBase64(param);
    }
    /**
     * 上传视频并返回图片封面 base64格式传输
     *
     * @param param
     * @return
     */
    @PostMapping("/oss/uploadVideo")
    public ResponseResult uploadVideoToOssBase64(@RequestBody OssUploadUpdateParamDTO param) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return ossServer.uploadVideoToOssBase64(param);
    }
    /**
     * 上传图片或视频  文件 格式传输
     *
     * @param file
     * @return
     */
    @PostMapping("/oss/uploadFile")
    public ResponseResult uploadToOssFile(@RequestParam("file") MultipartFile file) throws MinioException {
        return ossServer.uploadOssToFile(file);
    }
    /**
     * 上传图片或视频  文件 格式传输  返回Md5
     *
     * @param file
     * @return
     */
    @PostMapping("/oss/uploadFileMd5")
    public ResponseResult uploadToOssFileMd5(@RequestParam("file") MultipartFile file) throws MinioException {
        return ossServer.uploadToOssFileMd5(file);
    }
    /**
     * 上传png图片 图片必须等高
     *
     * @param param
     * @return
     * @throws IOException
     */
    @PostMapping("/oss/upload/png")
    public ResponseResult uploadPngToOss(@RequestBody OssUploadUpdateParamDTO param) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        if (".png".equals(param.getStreamName())) {
            return ossServer.uploadPngToOss(param);
        } else {
            return ResponseResult.fail(BizCodeEnume.UPLOAD_TYPE_NOT_PNG.getCode(), BizCodeEnume.UPLOAD_TYPE_NOT_PNG.getMsg());
        }
    }


    /**
     * 生成带有唯一识别字符的二维码
     *
     * @return
     */
    @PostMapping(value = "/creatcode")
    public Map<String, String> creatCode() {
        String uuid = SnowflakeIdWorker.getSnowId();
        Map<String, String> returnMap = new HashMap<String, String>();
        returnMap.put("uuid", HouConfig.QR_CODE_LOGIN + uuid);
        return returnMap;
    }


    /**
     * 手机端识别二维码后对这个接口存入信息
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/createtoken")
    public ResponseResult createToken(@RequestBody Map<String, String> paraMap) {
        String uuid = paraMap.get("uuid");
        String token = paraMap.get("token");
        String newToken = UUID.randomUUID().toString();

        String userString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
        String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
        // 存储uuid对应token
        RedisUtil.set(uuid, token, 300);
        // 对新生成token做登陆处理
        RedisUtil.set(HouConfig.HOU_USER_TOKEN_USER + newToken, userString, HouConfig.TOKEN_OUTTIME);
        RedisUtil.set(HouConfig.HOU_USER_TOKEN_USERID + newToken, userId, HouConfig.TOKEN_OUTTIME);
        return ResponseResult.success(BizCodeEnume.LOGIN_SUCCEEDED.getMsg());
    }

    /**
     * web端ping接口尝试登陆
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/trylogin")
    public ResponseResult tryLogin(@RequestBody Map<String, String> paraMap) {
        String uuid = paraMap.get("uuid");
        String tokenString = RedisUtil.get(uuid);
        if (null == tokenString) {
            return ResponseResult.fail(BizCodeEnume.UNSCANNED_CODE.getCode(),BizCodeEnume.UNSCANNED_CODE.getMsg());
        }
        return ResponseResult.success(BizCodeEnume.LOGIN_SUCCEEDED.getMsg());
    }

    /**
     * 微信支付证书上传
     * @param file
     * @param token
     * @return
     */
    @PostMapping ("/oss/fileUpload" )
    public  ResponseResult  fileUpload2( @RequestParam("file") MultipartFile file, @RequestParam("token")String token){
        ResponseResult result=ossServer.fileUpload2(file,token);
        return result;
    }
}
