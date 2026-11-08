package com.houjiang.thirdparty.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.thirdparty.dto.TemplateDataDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
            RedisUtil.set(accessToken, accessTokenStr, 3600);
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

    public static String getOauth2AccessToken(String appId, String appSecret, String code) {
        String oauth2AccessToken = "oauth2_" + appId + "&secret_" + appSecret + "&code_" + code;
        if (RedisUtil.exists(oauth2AccessToken)) {
            return RedisUtil.get(oauth2AccessToken);
        }
        try {
            String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=" + appId +
                    "&secret=" + appSecret +
                    "&code=" + code +
                    "&grant_type=authorization_code";
            URL url = new URL(accessTokenUrl);
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
            RedisUtil.set(oauth2AccessToken, accessTokenStr, 3600);
            return accessTokenStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isUserSubscribed(String openId, String accessToken) throws Exception {
        String urlStr = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // 解析JSON响应得到订阅状态
        JSONObject jsonObject = JSON.parseObject(response.toString());
        int subscribeStatus = jsonObject.getIntValue("subscribe");
        return subscribeStatus == 1;
    }

    public static void sendSubscribeMessage(String accessToken, String openId, String templateId, Map<String, TemplateDataDTO> data, Map<String, String> miniprogram) {
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Create JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("touser", openId);
            jsonPayload.put("template_id", templateId);
            if (!miniprogram.isEmpty()) {
                // 点击进入小程序跳转的页面
                jsonPayload.put("miniprogram", miniprogram);
            }
            //jsonPayload.put("page", "/salesperson/index/index");
            jsonPayload.put("data", data);
            System.out.println(jsonPayload);
            // Send post request
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);
            // success
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response: " + response.toString());
            } else {
                System.out.println("POST request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
