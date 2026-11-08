package com.houjiang.goods.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.houjiang.commons.util.PayToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName HutoolHttpUtils
 * @Description
 * @Author Amos
 * @Date 2024/11/6 14:08
 * @Version 1.0
 * @author Amos
 */

public class HutoolHttpUtils {




    private static final Logger LOGGER = LoggerFactory.getLogger(HutoolHttpUtils.class);
    /**
     * post请求鉴权
     * @param mapParam
     * @return
     */
    public static String httpPost(Map<String, Object> mapParam) {
        //1、配置调用地址
        String ucsUrl = mapParam.get("url").toString();
        String method = mapParam.get("method").toString();
        LOGGER.warn("请求地址::"+ucsUrl+method);
        //2、配置请求头
        Long timestamp = System.currentTimeMillis();
        String randomNumber= PayToolUtil.generateRandomNumber(15);
        Map<String, String> head = Maps.newHashMapWithExpectedSize(3);
        //appkey
        head.put("appKey",  mapParam.get("appKey").toString());
        //appkey
        head.put("appSecret",  mapParam.get("appSecret").toString());
        //当前时间
        head.put("timestamp", timestamp.toString());
        //随机生成数字
        head.put("randomNumber",  randomNumber);
        //版本号
        head.put("version", "1.0");
        //4、方法入参
        Map<String, Object> param = (Map<String, Object>) mapParam.get("param");
        String sign= SignUtils.getSign(JSONUtil.toJsonStr(param),mapParam.get("appKey").toString(),mapParam.get("appSecret").toString(), randomNumber,timestamp);
        //密钥
        head.put("sign",  sign);
        LOGGER.warn("请求header:"+head);
        LOGGER.warn("请求body:"+JSONUtil.toJsonStr(param));
        //6、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl+method).
                headerMap(head, false).body(JSONUtil.toJsonStr(param)).execute();
        return execute.body();
    }

    /**
     * post请求鉴权
     * @param mapParam
     * @return
     */
    public static String httpPostByFromData(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        String method = mapParam.get("method").toString();
        //4、方法入参
        Map<String, Object> param = (Map<String, Object>) mapParam.get("param");
        //6、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl+method)
                .form(param).execute();
        return execute.body();
    }
    /**
     * get请求
     * @param mapParam
     * @return
     */
    public static String httpGet(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        String method = mapParam.get("method").toString();
        String token=null;
        if(null!=mapParam.get("token")){
            token=mapParam.get("token").toString();
        }
        //2、方法入参
        Map<String, Object> param = (Map<String, Object>) mapParam.get("param");
        //3、发送请求

        HttpResponse execute = HttpUtil.createGet(ucsUrl+method).header("token",token).form(param).execute();
        return execute.body();
    }
    /**
     * post请求OA获取用户id
     * @param mapParam
     * @return
     */
    public static String httpPostOAUserInfo(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        //2、方法入参
        Map<String, Object> param = (Map<String, Object>) mapParam.get("param");
        //3、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl).body(JSONUtil.toJsonStr(param)).execute();
        return execute.body();
    }

    /**
     * post请求OA获取secret
     * @param mapParam
     * @return
     */
    public static String httpPostOASecret(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        String appid = mapParam.get("appid").toString();
        //2、配置请求头
        Map<String, String> head = Maps.newHashMapWithExpectedSize(3);
        head.put("appid", appid);
        //3、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl).
                headerMap(head, false).body("").execute();
        return execute.body();
    }

    /**
     * post请求OA获取token
     * @param mapParam
     * @return
     */
    public static String httpPostOAToken(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        String appid = mapParam.get("appid").toString();
        String secret = mapParam.get("secret").toString();
        //2、配置请求头
        Map<String, String> head = Maps.newHashMapWithExpectedSize(3);
        head.put("appid", appid);
        head.put("secret", secret);
        //3、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl).
                headerMap(head, false).body("").execute();
        return execute.body();
    }

    /**
     * post请求OA获取token
     * @param mapParam
     * @return
     */
    public static String httpPostOAData(Map<String, Object> mapParam) {
        //1、配置调用地址 用户中心测试环境
        String ucsUrl = mapParam.get("ucsUrl").toString();
        String appid = mapParam.get("appid").toString();
        String userid = mapParam.get("userid").toString();
        String token = mapParam.get("token").toString();
        //2、配置请求头
        Map<String, String> head = Maps.newHashMapWithExpectedSize(3);
        head.put("appid", appid);
        head.put("userid", userid);
        head.put("token", token);
        LOGGER.info("--请求OAhead---"+head);

        //3、方法入参
        Map<String, Object> param = (Map<String, Object>) mapParam.get("param");
        LOGGER.info("--请求OAparam---"+param);
        //4、发送请求
        HttpResponse execute = HttpUtil.createPost(ucsUrl).
                headerMap(head, false).body(JSON.toJSONString(param)).execute();
        LOGGER.info("--请求JSON.toJSONString(param)---"+JSON.toJSONString(param));
        return execute.body();
    }
}
