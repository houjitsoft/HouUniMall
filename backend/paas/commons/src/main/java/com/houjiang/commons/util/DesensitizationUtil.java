package com.houjiang.commons.util;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
public class DesensitizationUtil {

    public static <T> T Desensitization(T object, Class<T> cls) {
        try {
            log.info("object数据:" + object);

            Map<String, Object> map = ParseMapUtils.beanToMap(object);
            log.info("map数据:" + map);
            if (map.get("userName") != null && StrUtil.isNotEmpty(map.get("userName").toString())) {
                boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("userName").toString());
                if (match) {
                    map.put("userName", DesensitizedUtil.mobilePhone(map.get("userName").toString()));
                }
            }
            if (map.get("buyerUserName") != null && StrUtil.isNotEmpty(map.get("buyerUserName").toString())) {
                boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("buyerUserName").toString());
                if (match) {
                    map.put("buyerUserName", DesensitizedUtil.mobilePhone(map.get("buyerUserName").toString()));
                }
            }
            if (map.get("buyerUserAccount") != null && StrUtil.isNotEmpty(map.get("buyerUserAccount").toString())) {
                boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("buyerUserAccount").toString());
                if (match) {
                    map.put("buyerUserAccount", DesensitizedUtil.mobilePhone(map.get("buyerUserAccount").toString()));
                }
            }
            if (map.get("shopUserName") != null && StrUtil.isNotEmpty(map.get("shopUserName").toString())) {
                boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("shopUserName").toString());
                if (match) {
                    map.put("shopUserName", DesensitizedUtil.mobilePhone(map.get("shopUserName").toString()));
                }
            }
            if (map.get("orderOperator") != null && StrUtil.isNotEmpty(map.get("orderOperator").toString())) {
                boolean match = ReUtil.isMatch("1(3|4|5|6|7|8|9)\\d{9}", map.get("orderOperator").toString());
                if (match) {
                    map.put("orderOperator", DesensitizedUtil.mobilePhone(map.get("orderOperator").toString()));
                }
            }
            if (map.get("userMobile") != null && StrUtil.isNotEmpty(map.get("userMobile").toString())) {
                map.put("userMobile", DesensitizedUtil.mobilePhone(map.get("userMobile").toString()));
            }
            if (map.get("remarkUserPhone") != null && StrUtil.isNotEmpty(map.get("remarkUserPhone").toString())) {
                map.put("remarkUserPhone", DesensitizedUtil.mobilePhone(map.get("remarkUserPhone").toString()));
            }
            if (map.get("remarkedUserPhone") != null && StrUtil.isNotEmpty(map.get("remarkedUserPhone").toString())) {
                map.put("remarkedUserPhone", DesensitizedUtil.mobilePhone(map.get("remarkedUserPhone").toString()));
            }
            if (map.get("orderReceiverContact") != null && StrUtil.isNotEmpty(map.get("orderReceiverContact").toString())) {
                map.put("orderReceiverContact", DesensitizedUtil.mobilePhone(map.get("orderReceiverContact").toString()));
            }
            if (map.get("contactNumber") != null && StrUtil.isNotEmpty(map.get("contactNumber").toString())) {
                map.put("contactNumber", DesensitizedUtil.mobilePhone(map.get("contactNumber").toString()));
            }
            if (map.get("orderSellerContact") != null && StrUtil.isNotEmpty(map.get("orderSellerContact").toString())) {
                map.put("orderSellerContact", DesensitizedUtil.mobilePhone(map.get("orderSellerContact").toString()));
            }
            if (map.get("contactsPhone") != null && StrUtil.isNotEmpty(map.get("contactsPhone").toString())) {
                map.put("contactsPhone", DesensitizedUtil.mobilePhone(map.get("contactsPhone").toString()));
            }
            if (map.get("contactPhone") != null && StrUtil.isNotEmpty(map.get("contactPhone").toString())) {
                map.put("contactPhone", DesensitizedUtil.mobilePhone(map.get("contactPhone").toString()));
            }
            return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return object;
        }
    }

}
