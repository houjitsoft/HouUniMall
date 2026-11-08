package com.houjiang.commons.util.kuaidiniao;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.enums.ActionRule;
import com.houjiang.commons.vo.order.LogisticsInfoVO;
import com.houjiang.commons.vo.order.TraceVO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
public class KdniaoTrackQueryAPI {

    private static final Logger logger = LoggerFactory.getLogger(KdniaoTrackQueryAPI.class);

    /** 电商ID */
    public static String EBusinessID = "xxx";
    /** 电商加密私钥，快递鸟提供，注意保管，不要泄漏 */
    public static String AppKey = "xxxx-xxx-xxx-xxxx-xxxxxx";
    /** 快递鸟接口指令 购买套餐不一样 指令不一样 */
    public static String instructCode = "";
    /** 请求url */
    public static String ReqURL = "http://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

    /**
     * Json方式 查询订单物流轨迹
     * @throws Exception
     */
    public static String getOrderTracesByJson(String expCode, String expNo,String customerName) throws Exception{
        String requestData = null;

        if("SF".equals(expCode)){
            requestData = "{'OrderCode':'','ShipperCode':'" + expCode + "','LogisticCode':'" + expNo + "','CustomerName':'" + customerName + "'}";
        }else{
            requestData = "{'OrderCode':'','ShipperCode':'" + expCode + "','LogisticCode':'" + expNo + "'}";
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("RequestData", urlEncoder(requestData, "UTF-8"));
        params.put("EBusinessID", EBusinessID);
        params.put("RequestType", instructCode);
        String dataSign=encrypt(requestData, AppKey, "UTF-8");
        params.put("DataSign", urlEncoder(dataSign, "UTF-8"));
        params.put("DataType", "2");

        String result=sendPost(ReqURL, params);

        //根据公司业务处理返回的信息......

        return result;
    }

    /**
     * MD5加密
     * @param str 内容
     * @param charset 编码方式
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static String MD5(String str, String charset) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes(charset));
        byte[] result = md.digest();
        StringBuffer sb = new StringBuffer(32);
        for (int i = 0; i < result.length; i++) {
            int val = result[i] & 0xff;
            if (val <= 0xf) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * base64编码
     * @param str 内容
     * @param charset 编码方式
     * @throws UnsupportedEncodingException
     */
    private static String base64(String str, String charset) throws UnsupportedEncodingException {
        String encoded = base64Encode(str.getBytes(charset));
        return encoded;
    }

    @SuppressWarnings("unused")
    private static String urlEncoder(String str, String charset) throws UnsupportedEncodingException{
        String result = URLEncoder.encode(str, charset);
        return result;
    }

    /**
     * 电商Sign签名生成
     * @param content 内容
     * @param keyValue Appkey
     * @param charset 编码方式
     * @throws UnsupportedEncodingException ,Exception
     * @return DataSign签名
     */
    @SuppressWarnings("unused")
    private static String encrypt (String content, String keyValue, String charset) throws UnsupportedEncodingException, Exception
    {
        if (keyValue != null)
        {
            return base64(MD5(content + keyValue, charset), charset);
        }
        return base64(MD5(content, charset), charset);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * @param url 发送请求的 URL
     * @param params 请求的参数集合
     * @return 远程资源的响应结果
     */
    @SuppressWarnings("unused")
    private static String sendPost(String url, Map<String, String> params) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
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
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            // 发送请求参数
            if (params != null) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if(param.length()>0){
                        param.append("&");
                    }
                    param.append(entry.getKey());
                    param.append("=");
                    param.append(entry.getValue());
                    //System.out.println(entry.getKey()+":"+entry.getValue());
                }
                //System.out.println("param:"+param.toString());
                out.write(param.toString());
            }
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
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

    private static char[] base64EncodeChars = new char[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/' };

    public static String base64Encode(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len)
            {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len)
            {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    /**
     * Json方式 查询订单物流轨迹
     *
     * @param requestDataMap 请求数据映射
     * @return 查询结果字符串
     * @throws Exception
     */
    public static String getOrderTracesAndMap(Map<String, Object> requestDataMap) throws Exception {
        // 表示需要返回物流轨迹地图
        requestDataMap.put("IsReturnRouteMap", "1");
        // 轨迹按时间排序，0-升序，1-降序，默认0
        requestDataMap.put("Sort", "1");

        if (requestDataMap.get("Receiver") != null && requestDataMap.get("Sender") != null) {
            // 获取收件人地址信息并解析
            String receiver = requestDataMap.get("Receiver").toString();
            AddressParser.AddressResult receiverInfo = AddressParser.parseAddress(receiver);

            // 创建一个Map来存储解析后的收件人地址信息
            Map<String, Object> receiverMap = new HashMap<>();
            receiverMap.put("ProvinceName", receiverInfo.getProvince());
            receiverMap.put("CityName", receiverInfo.getCity());
            receiverMap.put("ExpAreaName", receiverInfo.getDistrict());
            receiverMap.put("Address", receiverInfo.getDetailAddress());
            // 将解析后的收件人地址信息Map替换原始的收件人地址字符串
            requestDataMap.put("Receiver", receiverMap);
            // 获取寄件人地址信息并解析
            String sender = requestDataMap.get("Sender").toString();
            AddressParser.AddressResult sendInfo = AddressParser.parseAddress(sender);
            // 创建一个Map来存储解析后的寄件人地址信息
            Map<String, Object> senderMap = new HashMap<>();
            senderMap.put("ProvinceName", sendInfo.getProvince());
            senderMap.put("CityName", sendInfo.getCity());
            senderMap.put("ExpAreaName", sendInfo.getDistrict());
            senderMap.put("Address", sendInfo.getDetailAddress());

            // 将解析后的寄件人地址信息Map替换原始的寄件人地址字符串
            requestDataMap.put("Sender", senderMap);
        }

        // 初始化请求数据字符串
        String requestData = null;
        // 创建一个ObjectMapper对象，用于转换Java对象和JSON
        ObjectMapper objectMapper = new ObjectMapper();
        // 将请求数据转换为JSON字符串
        try {
            requestData = objectMapper.writeValueAsString(requestDataMap);
            // 记录请求数据的日志
            logger.info("requestDataMap:{}", requestDataMap);
        } catch (Exception e) {
            // 如果转换过程中发生异常，则打印异常信息
            e.printStackTrace();
        }
        // 创建一个HashMap用于存储请求参数
        Map<String, String> params = new HashMap<>();
        // 将请求数据放入参数中，并进行URL编码
        params.put("RequestData", urlEncoder(requestData, "UTF-8"));
        // 将商家ID放入参数中
        params.put("EBusinessID", EBusinessID);
        // 计算数据签名，并进行URL编码
        String dataSign = encrypt(requestData, AppKey, "UTF-8");
        params.put("DataSign", urlEncoder(dataSign, "UTF-8"));
        // 设置请求类型
        params.put("RequestType", instructCode);
        // 设置数据类型
        params.put("DataType", "2");
        // 发送POST请求，并返回结果
        String postBody = sendPost(ReqURL, params);
        return getLogisticsInfo(postBody);
    }

    /**
     * 根据给定的物流信息JSON字符串，解析并处理物流追踪信息
     * 该方法首先解析输入的JSON字符串为LogisticsInfoVO对象，然后根据预定义规则处理追踪信息，
     * 最后返回处理后的物流信息JSON字符串
     *
     * @param postBody 物流信息的JSON字符串
     * @return 处理后的物流信息JSON字符串
     */
    public static String getLogisticsInfo(String postBody) {
        // 解析输入的JSON字符串为LogisticsInfoVO对象
        LogisticsInfoVO logisticsInfoVO = JSON.parseObject(postBody, LogisticsInfoVO.class);

        // 获取物流追踪信息列表
        List<TraceVO> traces = logisticsInfoVO.getTraces();
        // 如果追踪信息为空或仅有一条记录，则直接返回原始物流信息的JSON字符串
        if (traces == null || traces.isEmpty()) {
            return JSON.toJSONString(logisticsInfoVO);
        }

        // 初始化新的追踪信息列表
        List<TraceVO> newTraces = new ArrayList<>();
        // 使用Map记录规则是否已触发（Key: 枚举项，Value: 是否已处理）
        Map<ActionRule, Boolean> ruleProcessed = Arrays.stream(ActionRule.values())
                .collect(Collectors.toMap(Function.identity(), e -> false));

        // 遍历追踪信息
        for (TraceVO trace : traces) {
            // 按枚举顺序检查规则（确保优先级）
            for (ActionRule rule : ActionRule.values()) {
                // 如果当前规则未处理且当前追踪动作以该规则的前缀开头，则应用规则
                if (!ruleProcessed.get(rule) && trace.getAction().startsWith(rule.getPrefix())) {
                    // 添加处理后的追踪信息到新列表
                    addNewTrace(newTraces, trace, rule);
                    // 标记当前规则已处理
                    ruleProcessed.put(rule, true);
                    // 匹配成功后跳出，避免重复处理
                    break;
                }
            }
            // 将原始追踪信息添加到新列表中
            newTraces.add(trace);
        }

        // 更新物流信息中的追踪列表为处理后的新列表
        logisticsInfoVO.setTraces(newTraces);
        // 返回更新后的物流信息JSON字符串
        return JSON.toJSONString(logisticsInfoVO);
    }


    /**
     * 创建并添加新轨迹记录
     * <p>
     * 该方法用于在给定的轨迹列表中添加一个新的轨迹记录它通过复制现有轨迹记录的属性，
     * 并根据规则更新特定字段来创建新的轨迹记录这在处理轨迹数据时很有用，
     * 例如在物流或交通管理系统中
     *
     * @param newTraces 存储新轨迹记录的列表
     * @param source 源轨迹记录，用于复制属性
     * @param rule 动作规则，用于确定新轨迹记录中的动作和接受站描述
     */
    private static void addNewTrace(List<TraceVO> newTraces, TraceVO source, ActionRule rule) {
        // 创建一个新的轨迹记录对象
        TraceVO newTrace = new TraceVO();

        // 从源轨迹记录复制属性到新的轨迹记录中
        BeanUtils.copyProperties(source, newTrace);

        // 设置新轨迹记录的动作，根据提供的规则确定
        newTrace.setAction(rule.getTargetAction());

        // 设置新轨迹记录的接受站描述，同样根据规则确定
        newTrace.setAcceptStation(rule.getStationDesc());

        // 将新创建的轨迹记录添加到轨迹列表中
        newTraces.add(newTrace);
    }

    /**
     * Json方式 查询订单物流轨迹
     *
     * @param requestDataMap 请求数据映射
     * @return 查询结果字符串
     * @throws Exception
     */
    public static String getShopDeliveryTime(Map<String, Object> requestDataMap) {
        // 初始化请求数据字符串
        String requestData = null;
        // 创建一个ObjectMapper对象，用于转换Java对象和JSON
        ObjectMapper objectMapper = new ObjectMapper();
        // 将请求数据转换为JSON字符串
        try {
            requestData = objectMapper.writeValueAsString(requestDataMap);
            // 记录请求数据的日志
            logger.info("requestDataMap:{}", requestDataMap);
            // 创建一个HashMap用于存储请求参数
            Map<String, String> params = new HashMap<>();
            // 将请求数据放入参数中，并进行URL编码
            params.put("RequestData", urlEncoder(requestData, "UTF-8"));
            // 将商家ID放入参数中
            params.put("EBusinessID", EBusinessID);
            // 计算数据签名，并进行URL编码
            String dataSign = encrypt(requestData, AppKey, "UTF-8");
            params.put("DataSign", urlEncoder(dataSign, "UTF-8"));
            // 设置请求类型
            params.put("RequestType", "6004");
            // 设置数据类型
            params.put("DataType", "2");
            // 发送POST请求，并返回结果
            String postBody = sendPost(ReqURL, params);
            JSONObject jsonObject = new JSONObject(postBody);
            if (jsonObject.get("Success") != null && jsonObject.getBoolean("Success")) {
                JSONObject data = jsonObject.getJSONObject("Data");
                return data.getString("DeliveryTime");
            }
            return null;
        } catch (Exception e) {
            // 如果发生异常，则打印异常信息并返回空字符串
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析最新的物流信息JSON字符串并返回一个包含最小状态值对应物流信息的映射
     *
     * @param latestLogistics 最新的物流信息JSON字符串，格式非标准JSON
     * @return Map<String, String> 包含最小状态值对应物流信息的映射，键为字段名，值为字段值
     */
    public static Map<String, String> latestLogistics(String latestLogistics) {
        // 创建用于存储最终物流信息的delivery（保留最小状态值对应的信息）
        Map<String, String> delivery = new HashMap<>();
        // 将处理后的字符串转换为JSONArray对象
        JSONArray jsonArray = new JSONArray(latestLogistics);
        // 遍历JSON数组中的每个物流对象
        for (int k = 0; k < jsonArray.length(); k++) {
            // 获取当前物流对象及其详细信息
            JSONObject shippingObject = jsonArray.getJSONObject(k);
            if (shippingObject.isNull("traces")) {
                continue;
            }
            JSONArray traces = shippingObject.getJSONArray("traces");
            // 提取物流信息字段（符合Java命名规范的小写变量名）
            if (traces.isEmpty()) {
                continue;
            }
            String estimatedDeliveryTime = traces.getJSONObject(0).getString("acceptTime");
            String location = traces.getJSONObject(0).getString("location");
            String stateEx = shippingObject.getString("stateEx");
            String acceptStation = traces.getJSONObject(1).getString("acceptStation");

            // 转换为整型便于比较
            int currentState = Integer.parseInt(stateEx);

            // 首次循环直接初始化Map
            if (delivery.isEmpty()) {
                delivery.put("estimatedDeliveryTime", estimatedDeliveryTime);
                delivery.put("location", location);
                delivery.put("state", stateEx);
                delivery.put("acceptStation", acceptStation);
            // 非首次循环时进行状态值比较
            } else {
                // 从Map获取已存状态并转换为整型
                int existingState = Integer.parseInt(delivery.get("state"));
                // 仅当当前状态更小时更新Map（修正原代码逻辑错误）
                if (currentState < existingState) {
                    delivery.put("estimatedDeliveryTime", estimatedDeliveryTime);
                    delivery.put("location", location);
                    delivery.put("state", stateEx);
                    delivery.put("acceptStation", acceptStation);
                }
            }
        }
        if (delivery.get("state") != null) {
            if (delivery.get("state").equals("10")) {
                delivery.put("stateStr", "待揽件");
            }
            if (delivery.get("state").equals("412")) {
                delivery.put("stateStr", "已代收");
            }
            if (delivery.get("stateStr") == null) {
                Integer startInt = Integer.parseInt(String.valueOf(delivery.get("state").trim().charAt(0)));
                delivery.put("stateStr", CommonType.deliveryStatus.getByCode(startInt));
            }
        } else {
            delivery.put("stateStr", "暂无轨迹信息");
        }
        // 返回包含最小状态值对应物流信息的映射
        return delivery;
    }
}
