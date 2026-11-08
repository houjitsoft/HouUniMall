package com.houjiang.payment.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.RedisUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Amos
 */
public class WeChatUtil {

    public static String getAccessToken(String appId, String appSecret) {
        String accessToken = "accessToken_appid_" + appId + "&secret_" + appSecret;
        if (RedisUtil.exists(accessToken)) {
            return RedisUtil.getNotExpire(accessToken);
        }
        try {
            String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the JSON response to get the access_token
            String jsonResponse = response.toString();
            // Assuming you have a method to parse JSON and get the token
            String accessTokenStr = parseAccessToken(jsonResponse);
            RedisUtil.set(accessToken, accessTokenStr, 1800);
            return accessTokenStr;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String parseAccessToken(String jsonResponse) {
        // Implement your JSON parsing logic here to extract the access_token
        // For example, using a JSON library such as org.json
        JSONObject jsonObject = JSON.parseObject(jsonResponse);
        return jsonObject.getString("access_token");
    }

    public static String sendHttpRequest(String accessToken, String openId) {
        StringBuilder response = new StringBuilder();
        try {
            String userInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info" +
                    "?access_token=" + accessToken +
                    "&openid=" + openId;
            URL url = new URL(userInfoUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

}
