package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayAuthorizationAppletInformationEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @ClassName PayAuthorizationAppletInformationService
 * @Description  授权小程序信息表
 * @Author Amos
 * @Date 2023-07-20 09:39:51
 * @Version 1.0
 * @author Amos
 */
public interface PayAuthorizationAppletInformationService extends IService<PayAuthorizationAppletInformationEntity> {

    /**
     * 查询品牌店铺小程序授权配置
     * @param map
     * @return
     */
    ResponseResult queryShopAppletConfigFeign(Map<String, Object> map);

    /**
     * 小程序配置查询
     * @param map
     * @return
     */
    ResponseResult querysecret(Map<String,Object> map);

    /**
     * 发布审核通过的小程序
     * @param map
     * @return
     */
    ResponseResult release(Map<String,Object> map);

    /**
     * 设置小程序用户隐私指引
     * @param map
     * @return
     */
    ResponseResult setPrivacySetting(Map<String,Object> map);

    /**
     * 填写小程序信息
     * @param map
     * @return
     */
    ResponseResult updatesecret(Map<String,Object> map);

    /**
     * 查看最近一次审核状态
     * @param map
     * @return
     */
    ResponseResult getLatestAuditStatus( Map<String,Object> map);

    /**
     * 提交代码审核
     * @param map
     * @return
     */
    ResponseResult submitAuditUrl(Map<String,Object> map);

    /**
     * 上传提审素材
     * @param mediaFile
     * @param token
     * @return
     */
     ResponseResult uploadMediaToCodeAudit(MultipartFile mediaFile,String token);

    /**
     * 获取体验二维码
     * @param map
     * @return
     */
    ResponseResult getqrcode(Map<String,Object> map);

    /**
     * 上传代码
     * @param map
     * @return
     */
    ResponseResult commit(Map<String,Object> map);

    /**
     *获取代码模板列表
/**
     * @param map
     * @return
     */
    ResponseResult addToTemplate(Map<String,Object> map);

    /**
     * 获取pre_auth_code预授权码
     * @param map
     * @return
     */
    ResponseResult getPreauthCode(Map<String,Object> map);

    /**
     * 查询该店铺是否授权过小程序
     * @param map
     * @return
     */
    ResponseResult checkAuth(Map<String,Object> map);

    /**
     * 获得授权事件的票据
     * @return
     */
    String ticket(String timestamp, String nonce, String msgSignature, String postData);

    /**
     * 授权回调URL
     * @return
     */
    void authcode(HttpServletResponse response, String authCode, String expiresIn, String token);
}
