package com.houjiang.base.utils;

import com.alibaba.fastjson.JSON;
import com.houjiang.base.feign.PayMentServiceClient;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.AccessTokenDTO;
import com.houjiang.commons.util.ResponseResult;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName AccessTokenUtil.java
 * @Description TODO
 * @createTime 2021年08月18日 13:32:00
 */
@Component
public class AccessTokenUtil {

    private static AccessTokenUtil accessTokenUtil;
    /** 小程序appid */
    private static String appid = "";
    /** 小程序秘钥 */
    private static String secret = "";

    @Autowired(required = false)
    private PayMentServiceClient payMentServiceClient;

    public static String queryAccessToken() {
        try {
            //查询小程序的 appid  密钥
            Map<String, Object> param = new HashMap<>(1);
            param.put("paymentMode", "02");
            //TODO 小程序配置接口未完成
            ResponseResult resultResp = accessTokenUtil.payMentServiceClient.selectPayConfig(param);
            Map<String, String> outMap = (Map<String, String>) resultResp.getData();
            appid = outMap.get("appID");
            secret = outMap.get("appSecret");

            String host = CommonType.wxApiUrl.API_WECHAT_QQ_URL.getDesc();
            String path = "/cgi-bin/token";
            String method = "GET";
            String grantType = "client_credential".trim();
            Map<String, String> headers = new HashMap<String, String>();
            // 格式为:Authorization:APPCODE，存放appcode
            headers.put("Accept", " application/json");
            Map<String, String> querys = new HashMap<String, String>();
            querys.put("grant_type", grantType);
            querys.put("appid", appid);
            querys.put("secret", secret);
            String result = null;
            HttpResponse httpResponse = HttpResponseUtils.doGet(host, path, method, headers, querys);

            if (null != httpResponse) {
                HttpEntity entity = httpResponse.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity, "utf-8");
                }
            }
            AccessTokenDTO jsonObject = JSON.parseObject(result, AccessTokenDTO.class);

            if (jsonObject != null) {
                return jsonObject.getAccessToken();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过@PostConstruct实现初始化bean之前进行的操作
     */
    @PostConstruct
    public void init() {
        accessTokenUtil = this;
        accessTokenUtil.payMentServiceClient = this.payMentServiceClient;
        // 初使化时将已静态化的testService实例化
    }
}
