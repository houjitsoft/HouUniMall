package com.houjiang.goods.util.es;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.AccessTokenDTO;
import com.houjiang.commons.util.HttpResponseUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.feign.PayMentServiceClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName AccessTokenUtil.java
 * @Description AccessTokenUtil
 * @createTime 2021年08月18日 13:32:00
 */
@Component
public class AccessTokenUtil {
    /** 生成小程序码地址 */
    private static final String GETWXACODEUNLIMIT_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s";
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
     * 查询品牌店铺 AccessToken
     * @param brandShopId
     * @return
     */
    public static String queryAccessTokenByShopId(Integer brandShopId) {
        try {
            if(brandShopId != 0){
                Map<String, Object> param = new HashMap<>();
                param.put("shopId", brandShopId);
                //查询品牌商家小程序配置
                ResponseResult resultResp2 = accessTokenUtil.payMentServiceClient.selectPayConfigFeign(param);
                if(resultResp2 == null || resultResp2.getCode() == 1 || resultResp2.getData() == null){
                    return null;
                }
                Map<String, String> outMap = (Map<String, String>) resultResp2.getData();
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
            }
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
    /**
     * 生成小程序码返回base64字符串
     * @param sceneStr 要携带的参数
     * @param page 要跳转的小程序页面（必须是已发布的）
     * @return
     */
    public static String getminiqrQr(String sceneStr,String page,Integer shopId) {
        HttpURLConnection httpURLConnection = null;
        try {
            String accessToken = null;
            if(null != shopId){
                accessToken = AccessTokenUtil.queryAccessTokenByShopId(shopId);
            }else{
                accessToken = AccessTokenUtil.queryAccessToken();
            }
            URL url = new URL(String.format(GETWXACODEUNLIMIT_URL,accessToken));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // 设置响应头
            httpURLConnection.setRequestProperty("Content-Type", "application/x-javascript; charset=UTF-8");
            // 提交模式
            httpURLConnection.setRequestMethod("POST");
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            // 发送请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("scene", sceneStr);
            paramJson.put("page", page);
            printWriter.write(paramJson.toString());
            // flush输出流的缓冲
            printWriter.flush();
            //开始获取数据

            BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
            try (InputStream is = httpURLConnection.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                //图片保存服务器
                String fileName = GenerateImage(Base64.getEncoder().encodeToString(baos.toByteArray()), null);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    /**
     * 对字节数组字符串进行Base64解码并生成图片
     * @param imgStr
     * @param imgFilePath
     * @return
     */
    public static String GenerateImage(String imgStr, String imgFilePath) {
        if (imgStr == null){
            // 图像数据为空
            return null;
        }

        try {
            // Base64解码
            byte[] bytes = Base64.getDecoder().decode(imgStr);
            for (int i = 0; i < bytes.length; ++i) {
                // 调整异常数据
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(currentTime);
            String pathUrl = ResourceUtils.getURL("classpath:").getPath();
            String path1 = pathUrl+"/static/images/"+ dateString+".jpg";
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(path1);
            out.write(bytes);
            out.flush();
            out.close();
            return dateString+".jpg";
        } catch (Exception e) {
            return null;
        }
    }
}
