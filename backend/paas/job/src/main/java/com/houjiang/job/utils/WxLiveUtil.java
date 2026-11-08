package com.houjiang.job.utils;

import com.alibaba.fastjson.JSONObject;
import com.houjiang.job.vo.QuWxGoodsListVO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WxLiveUtil
 * @Description 微信审核工具类
 * @Author Amos
 * @Date 2023/5/17 11:46
 * @Version 1.0
 * @author Amos
 */
@Component
public class WxLiveUtil {

    private static final String UPLOAD_URL = "/wxaapi/broadcast/goods/getapproved";


/**
 * 查询微信直播商品列表。
 *
 * 通过调用微信接口，传入偏移量和商品状态，获取微信直播商品列表。
 * 此方法用于集成微信直播功能，查询已上架或待上架的商品信息。
 *
 * @param offset 查询偏移量，用于分页查询。
 * @param status 商品状态，指定查询哪种状态的商品。
 * @return QuWxGoodsListVO 对象，包含查询结果的商品列表。
 * @throws Exception 如果查询过程中出现异常，抛出异常。
 */
public QuWxGoodsListVO queryWxLiveGoodsList(String offset, String status) throws Exception {
    // 获取微信访问令牌，用于授权访问微信接口。
    String accessToken = AccessTokenUtil.queryAccessToken();
    // 构造接口URL，将访问令牌插入到URL中。
    String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken);

    // 准备请求参数，包括偏移量、商品状态和访问令牌。
    Map<String, String> map = new HashMap<>();
    map.put("offset", offset);
    map.put("status", status);
    map.put("access_token", accessToken);

    // 设置请求的主机和方法。
    String host = "https://api.weixin.qq.com";
    String method = "GET";
    // 准备请求头，指定接受的字符编码。
    Map<String, String> headers = new HashMap<String, String>();
    String accept = headers.put("Accept", "charset=utf-8");

    // 执行GET请求，获取响应结果。
    String result = null;
    HttpResponse httpResponse = HttpUtil.doGet(host, url, method, headers, map);
    if (null != httpResponse) {
        HttpEntity entity = httpResponse.getEntity();
        if (null != entity) {
            // 从响应实体中提取响应内容，并转换为字符串。
            result = EntityUtils.toString(entity, "utf-8");
        }
    }
    // 将响应内容解析为QuWxGoodsListVO对象，返回查询结果。
    QuWxGoodsListVO jsonObject = JSONObject.parseObject(result, QuWxGoodsListVO.class);
    return jsonObject;
}


}
