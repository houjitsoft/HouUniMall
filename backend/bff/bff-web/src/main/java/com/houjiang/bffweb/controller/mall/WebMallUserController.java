package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.UserAddressServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城前台会员中心接口（mall/uniapp）
/**
 *
 * 路径：/web/user/* （mall/uniapp 专属子集）
 * 注意：与 {@link com.houjiang.bffweb.controller.WebUserController} 同前缀但路径互不重叠：
 *  - WebUserController 服务于 SaaS 用户体系（register / refresh-token / queryUserInfo / dept / organization）
 *    返回 commons.ResponseResult 风格
 *  - 本类服务于商城会员（center / level_info / address / wallet / ...）
 *    返回 AjaxResult 同构 {code, msg, data, show}
 *
 * 当前实现策略：返回最小可用空数据避免商城前台白屏；
 * 未登录场景前端拦截器会按 token 处理，无需在这里强校验。
 *
 * @author Amos
 */
@RestController
public class WebMallUserController {

    @Autowired
    private UserAddressServiceFeign userAddressServiceFeign;

    // ================= 用户中心 =================

    /** /web/user/center 用户中心首页（头像、昵称、积分、订单数等聚合） */
    @RequestMapping(value = "/web/user/center", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> center() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("avatar", "");
        data.put("nickname", "");
        data.put("mobile", "");
        data.put("sn", "");
        data.put("integral", 0);
        data.put("growth", 0);
        data.put("balance", "0.00");
        data.put("user_money", "0.00");

        // 订单状态计数（前端按 0/1/2/3/4 渲染角标）
        Map<String, Object> orderCount = new LinkedHashMap<>();
        // 待付款
        orderCount.put("payment", 0);
        // 待发货
        orderCount.put("delivery", 0);
        // 待收货
        orderCount.put("take", 0);
        // 待评价
        orderCount.put("comment", 0);
        // 售后中
        orderCount.put("refund", 0);
        data.put("order_count", orderCount);

        // 会员等级
        Map<String, Object> level = new LinkedHashMap<>();
        level.put("level_id", 0);
        level.put("level_name", "");
        level.put("level_icon", "");
        data.put("level", level);

        data.put("coupon_count", 0);
        data.put("favorite_count", 0);
        data.put("footprint_count", 0);
        data.put("address_count", 0);
        data.put("is_distribution", 0);
        return ok(data);
    }

    /** /web/user/level_info 会员等级详情 */
    @RequestMapping(value = "/web/user/level_info", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> levelInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("level_id", 0);
        data.put("level_name", "");
        data.put("level_icon", "");
        data.put("growth", 0);
        data.put("next_level", null);
        data.put("level_lists", new ArrayList<>());
        data.put("equity_lists", new ArrayList<>());
        return ok(data);
    }

    /** /web/user/updateUserInfo 更新用户信息 */
    @PostMapping("/web/user/updateUserInfo")
    public Map<String, Object> updateUserInfo(@RequestBody(required = false) Map<String, Object> body) {
        return ok(new LinkedHashMap<>());
    }

    /** /web/user/bindMobile 绑定手机号 */
    @PostMapping("/web/user/bindMobile")
    public Map<String, Object> bindMobile(@RequestBody(required = false) Map<String, Object> body) {
        return ok(new LinkedHashMap<>());
    }

    /** /web/user/getMobileByMnp 一键登录手机号 */
    @PostMapping("/web/user/getMobileByMnp")
    public Map<String, Object> getMobileByMnp(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mobile", "");
        return ok(data);
    }

    /** /web/user/changePassword 修改密码 */
    @PostMapping("/web/user/changePassword")
    public Map<String, Object> changePassword(@RequestBody(required = false) Map<String, Object> body) {
        return ok(new LinkedHashMap<>());
    }

    /** /web/user/resetPassword 重置密码 */
    @PostMapping("/web/user/resetPassword")
    public Map<String, Object> resetPassword(@RequestBody(required = false) Map<String, Object> body) {
        return ok(new LinkedHashMap<>());
    }

    /** /web/user/wallet 钱包余额 */
    @RequestMapping(value = "/web/user/wallet", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> wallet() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user_money", "0.00");
        data.put("integral", 0);
        data.put("growth", 0);
        return ok(data);
    }

    /** /web/user/customer-service 客服配置 */
    @RequestMapping(value = "/web/user/customer-service", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> customerService() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", 0);
        data.put("mobile", "");
        data.put("wechat", "");
        data.put("qr_code", "");
        return ok(data);
    }

    /** /web/user/points-log 积分流水 */
    @RequestMapping(value = "/web/user/points-log", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> pointsLog(@RequestBody(required = false) Map<String, Object> body) {
        return ok(emptyPage());
    }

    // ================= 收货地址 =================
    // 对接 userms 真实接口 /user/address/*（token 从请求头注入请求体），
    // 响应把 userAddress* 实体字段转为前端 like-shop 风格（id/contact/mobile/...）

    /** /web/user/address/list 地址列表（前端 GET /userAddress/lists 映射至此） */
    @RequestMapping(value = "/web/user/address/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> addressList(@RequestBody(required = false) Map<String, Object> body,
                                           HttpServletRequest request) {
        ResponseResult result = userAddressServiceFeign.getAddressList(
                buildAddressParam(body, request));
        if (result == null || result.getCode() != 1) {
            // 未登录/下游异常时返回空列表，避免页面白屏
            return ok(new ArrayList<>());
        }
        return ok(convertAddressList(result.getData()));
    }

    /** /web/user/address/lists 兼容别名 */
    @RequestMapping(value = "/web/user/address/lists", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> addressLists(@RequestBody(required = false) Map<String, Object> body,
                                            HttpServletRequest request) {
        return addressList(body, request);
    }

    /** /web/user/address/detail 地址详情（前端 GET 带 id） */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/web/user/address/detail", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> addressDetail(@RequestBody(required = false) Map<String, Object> body,
                                             @RequestParam(required = false) Map<String, Object> query,
                                             HttpServletRequest request) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (query != null) {
            merged.putAll(query);
        }
        if (body != null) {
            merged.putAll(body);
        }
        ResponseResult result = userAddressServiceFeign.getAddressList(
                buildAddressParam(merged, request));
        Object targetId = merged.get("id");
        if (result != null && result.getCode() == 1) {
            for (Map<String, Object> item : convertAddressList(result.getData())) {
                if (targetId == null || String.valueOf(item.get("id")).equals(String.valueOf(targetId))) {
                    return ok(item);
                }
            }
        }
        // 未命中具体地址时退回默认地址，兼容结算页取地址场景
        ResponseResult defResult = userAddressServiceFeign.getDefaultAddress(
                buildAddressParam(merged, request));
        if (defResult != null && defResult.getCode() == 1 && defResult.getData() instanceof Map<?, ?> defData) {
            return ok(convertAddressItem((Map<String, Object>) defData));
        }
        return ok(new LinkedHashMap<>());
    }

    /** /web/user/address/add 新增地址 */
    @PostMapping("/web/user/address/add")
    public Map<String, Object> addressAdd(@RequestBody(required = false) Map<String, Object> body,
                                          HttpServletRequest request) {
        return toAjaxResult(userAddressServiceFeign.addAddress(buildAddressParam(body, request)));
    }

    /** /web/user/address/edit 编辑地址 */
    @PostMapping("/web/user/address/edit")
    public Map<String, Object> addressEdit(@RequestBody(required = false) Map<String, Object> body,
                                           HttpServletRequest request) {
        return toAjaxResult(userAddressServiceFeign.updateAddress(buildAddressParam(body, request)));
    }

    /** /web/user/address/delete 删除地址 */
    @PostMapping("/web/user/address/delete")
    public Map<String, Object> addressDelete(@RequestBody(required = false) Map<String, Object> body,
                                             HttpServletRequest request) {
        return toAjaxResult(userAddressServiceFeign.deleteAddress(buildAddressParam(body, request)));
    }

    /** /web/user/address/set_default 设默认地址 */
    @PostMapping("/web/user/address/set_default")
    public Map<String, Object> addressSetDefault(@RequestBody(required = false) Map<String, Object> body,
                                                 HttpServletRequest request) {
        return toAjaxResult(userAddressServiceFeign.setDefaultAddress(buildAddressParam(body, request)));
    }

    /**
     * 构造 userms 地址接口参数：注入 token + 前端字段名→userAddress* 字段名映射
     */
    private Map<String, String> buildAddressParam(Map<String, Object> body, HttpServletRequest request) {
        Map<String, String> param = new LinkedHashMap<>();
        String token = MallTokenSupport.resolveToken(request);
        if (body != null) {
            Object bodyToken = body.get("token");
            if (bodyToken != null && !bodyToken.toString().isBlank()) {
                token = bodyToken.toString();
            }
            copyAsString(param, "userAddressId", firstNonNull(body.get("id"), body.get("userAddressId")));
            copyAsString(param, "userAddressContact", firstNonNull(body.get("contact"), body.get("userAddressContact")));
            copyAsString(param, "userAddressPhone", firstNonNull(body.get("mobile"), body.get("userAddressPhone")));
            copyAsString(param, "userAddressAddress", firstNonNull(body.get("info"), body.get("address"), body.get("userAddressAddress")));
            copyAsString(param, "userAddressDefault", firstNonNull(body.get("is_default"), body.get("userAddressDefault")));
            copyAsString(param, "userAddressProvinceId", firstNonNull(body.get("province_id"), body.get("userAddressProvinceId")));
            copyAsString(param, "userAddressCityId", firstNonNull(body.get("city_id"), body.get("userAddressCityId")));
            copyAsString(param, "userAddressAreaId", firstNonNull(body.get("district_id"), body.get("userAddressAreaId")));
            copyAsString(param, "userAddressArea", firstNonNull(body.get("region_content"), body.get("userAddressArea")));
        }
        if (token != null) {
            param.put("token", token);
        }
        return param;
    }

    /**
     * userms 地址列表 → 前端 like-shop 风格列表；兼容 data 为 List 或单个 Map 的情况
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertAddressList(Object data) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (data instanceof List<?> rawList) {
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    list.add(convertAddressItem((Map<String, Object>) map));
                }
            }
        } else if (data instanceof Map<?, ?> map) {
            list.add(convertAddressItem((Map<String, Object>) map));
        }
        return list;
    }

    /** 单条地址：userAddress* → id/contact/mobile/province_id/city_id/district_id/address/info/is_default */
    private Map<String, Object> convertAddressItem(Map<String, Object> item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", item.get("userAddressId"));
        data.put("contact", item.get("userAddressContact"));
        data.put("mobile", item.get("userAddressPhone"));
        data.put("province_id", item.get("userAddressProvinceId"));
        data.put("city_id", item.get("userAddressCityId"));
        data.put("district_id", item.get("userAddressAreaId"));
        data.put("address", item.get("userAddressAddress"));
        data.put("info", item.get("userAddressAddress"));
        data.put("is_default", item.get("userAddressDefault"));
        data.put("region_content", item.get("userAddressArea"));
        return data;
    }

    /** 下游 ResponseResult → AjaxResult 同构 Map */
    private Map<String, Object> toAjaxResult(ResponseResult result) {
        if (result != null && result.getCode() == 1) {
            return ok(result.getData());
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 0);
        resp.put("msg", result != null && result.getMessage() != null && !result.getMessage().isBlank()
                ? result.getMessage() : "操作失败");
        resp.put("data", null);
        resp.put("show", 1);
        return resp;
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null && !value.toString().isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static void copyAsString(Map<String, String> target, String key, Object value) {
        if (value != null && !value.toString().isBlank()) {
            target.put(key, value.toString());
        }
    }

    // ================= 内部工具 =================

    /** AjaxResult 同构成功返回 */
    private static Map<String, Object> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 1);
        resp.put("msg", "");
        resp.put("data", data);
        resp.put("show", 0);
        return resp;
    }

    /** 空分页结构 */
    private static Map<String, Object> emptyPage() {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("count", 0);
        page.put("page_no", 1);
        page.put("page_size", 15);
        page.put("lists", new ArrayList<>());
        page.put("extend", new LinkedHashMap<>());
        return page;
    }
}
