package com.houjiang.merchant.common.plugin.delivery.engine;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.plugin.delivery.vo.KdQueryTrackParam;
import com.houjiang.merchant.common.plugin.delivery.vo.KdTrackResultVo;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Kdniao {

    /**
     * 快递实时查询
     *
     * @author Amos
     * @param customer 用户ID快递鸟提供 => EBusinessID
     * @param key API key快递鸟提供 => ApiKey
     * @param requestType 请求类型: 1002=免费,8001=收费
     * @param kdQueryTrackParam 参数
     */
    public List<KdTrackResultVo> queryTrack(String customer, String key, String requestType, KdQueryTrackParam kdQueryTrackParam) {
        try {
            // 快递编码
            String shipperCode = kdQueryTrackParam.getCom();
            // 快递单号
            String logisticCode = kdQueryTrackParam.getNum();
            // 客户姓名
            String customerName = kdQueryTrackParam.getCustomerName();
            // 订单编号(自定义)
            String orderCode = kdQueryTrackParam.getOrderCode();

            // 组装应用级参数
            String RequestData = "{" +
                    "'CustomerName': '" + customerName + "'," +
                    "'OrderCode': '" + orderCode + "'," +
                    "'ShipperCode': '" + shipperCode + "'," +
                    "'LogisticCode': '" + logisticCode + "'," +
                    "}";

            // 组装系统级参数
            Map<String, String> params = new HashMap<>();
            params.put("RequestData", urlEncoder(RequestData));
            params.put("EBusinessID", customer);
            params.put("RequestType", requestType);
            params.put("DataSign", urlEncoder(encrypt(RequestData, key)));
            params.put("DataType", "2");

            String reqURL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
            String result = sendPost(reqURL, params);

            JSONObject jsonObjectResult = JSONObject.parseObject(result);
            JSONArray jsonArrayResult = jsonObjectResult.getJSONArray("Traces");

            List<KdTrackResultVo> trackResult = new LinkedList<>();
            if (!jsonArrayResult.isEmpty()) {
                for (int i = jsonArrayResult.size() - 1; i >= 0; i--) {
                    JSONObject jsonObject = jsonArrayResult.getJSONObject(i);
                    KdTrackResultVo vo = new KdTrackResultVo();
                    vo.setTime(jsonObject.get("AcceptTime").toString());
                    vo.setContent(jsonObject.get("AcceptStation").toString());
                    trackResult.add(vo);
                }
            }
            return trackResult;

        } catch (Exception e) {
            log.error("快递鸟物流信息查询失败", e);
            throw new OperateException("物流信息查询失败，请稍后重试");
        }
    }

    /**
     * MD5加密
     *
     * @param str 内容
     * @return String
     * @throws Exception 异常
     */
    @SuppressWarnings("unused")
    private String MD5(String str) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes(StandardCharsets.UTF_8));
        byte[] result = md.digest();
        StringBuilder sb = new StringBuilder(32);
        for (byte b : result) {
            int val = b & 0xff;
            if (val <= 0xf) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * base64编码
     *
     * @param str 内容
     * @return String
     */
    private String base64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * url编码
     *
     * @param str String
     * @return String
     * @throws UnsupportedEncodingException 异常
     */
    @SuppressWarnings("unused")
    private String urlEncoder(String str) throws UnsupportedEncodingException{
        return URLEncoder.encode(str, "UTF-8");
    }

    /**
     * 电商Sign签名生成
     *
     * @param content 内容
     * @param keyValue ApiKey
     * @return String
     * @throws Exception 异常
     */
    @SuppressWarnings("unused")
    private  String encrypt(String content, String keyValue) throws Exception {
        if (keyValue != null) {
            return base64(MD5(content + keyValue));
        }
        return base64(MD5(content));
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * url 发送请求的 URL
     * params 请求的参数集合
     * @return 远程资源的响应结果
     */
    @SuppressWarnings("unused")
    private String sendPost(String url, Map<String,String> params) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new    StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn =(HttpURLConnection) realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // POST方法
            conn.setRequestMethod("POST");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();
            // 获取URLConnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new    StringBuilder();
                for (Map.Entry<   String,    String> entry : params.entrySet()) {
                    if(param.length()>0){
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    param.append(entry.getValue());
                }
                out.write(param.toString());
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

}
