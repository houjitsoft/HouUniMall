package com.houjiang.merchant.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.exception.OperateException;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
/**
 * 飞蛾打印工具
 * @author Amos
 */
public class PrinterUtils {
    /**
     * 计算签名
     *
     * @param config 配置信息，包含 user 和 ukey
     * @param time   时间戳
     * @return SHA-1 哈希值
     */
    public static String signature(Map<String, String> config, String time) {
        // 拼接字符串
        String input = config.get("user") + config.get("ukey") + time;

        // 计算 SHA-1 哈希值
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // 如果 SHA-1 算法不可用，返回 null
            return null;
        }
    }
    public static void printMsg(JSONObject config, AtomicReference<String> content, Integer nums) {
        //通过POST请求，发送打印信息到服务器
        RequestConfig requestConfig = RequestConfig.custom()
                //读取超时
                .setSocketTimeout(30000)
                //连接超时
                .setConnectTimeout(30000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpPost post = new HttpPost("https://api.feieyun.cn/Api/Open/");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("user", config.getString("user")));
        String STIME = String.valueOf(System.currentTimeMillis()/1000);
        nvps.add(new BasicNameValuePair("stime",STIME));
        nvps.add(new BasicNameValuePair("sig",signature(MapUtils.jsonToMap(JSONObject.toJSONString(config)),STIME)));
        //固定值,不需要修改
        nvps.add(new BasicNameValuePair("apiname","Open_printMsg"));
        nvps.add(new BasicNameValuePair("sn",config.getString("sn")));
        nvps.add(new BasicNameValuePair("content",content.get()));
        //打印联数
        nvps.add(new BasicNameValuePair("times",String.valueOf(nums)));

        CloseableHttpResponse response = null;
        String result = null;
        try
        {
            post.setEntity(new UrlEncodedFormEntity(nvps,"utf-8"));
            response = httpClient.execute(post);
            int statecode = response.getStatusLine().getStatusCode();
            if(statecode == 200){
                HttpEntity httpentity = response.getEntity();
                if (httpentity != null){
                    //服务器返回的JSON字符串，建议要当做日志记录起来
                    result = EntityUtils.toString(httpentity);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
                if(response!=null){
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                post.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject postJson = JSONObject.parseObject(result);
        if (StringUtils.isNotNull(postJson.getInteger("ret")) && postJson.getInteger("ret").equals(0) == false) {
            throw new OperateException(postJson.getString("msg"));
        }
    }
}
