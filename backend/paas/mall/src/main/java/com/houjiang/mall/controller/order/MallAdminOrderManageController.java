package com.houjiang.mall.controller.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.feign.OrderPaasFeign;
import com.houjiang.mall.mapper.order.OrderBaseQueryMapper;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mall 平台后台 — 订单管理（BFF /web/mall/order/manage/* → mall 鉴权 → order）
 *
 * <p>🔴 契约适配：admin 前端订单页（views/mall/order/OrderList.vue / OrderDetail.vue）
 * 沿用旧 mall-order 服务的响应契约（data.lists / data.count / data.extend + 行内
 * snake_case 字段；详情为 orderNo/status/goodsList 等驼峰结构），而 order 服务
 * 管理端走 /order/base/queryOrdersByPC（PageInfo.page{totalCount,list} +
 * OrderListVO camelCase；对平台超管放开「仅本人」过滤）与
 * /order/base/platForm/backGroundByOrderDetails（详情）。
 * 本控制器在 mall 单点做双向字段转换，order 侧保持不动。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/order/manage")
@RequiredArgsConstructor
public class MallAdminOrderManageController {

    private static final String ORDER_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','mall:order','shell:mall:order')";

    /** order 服务 orderStatus 枚举 → 中文文案（CommonType.orderStatus） */
    private static final Map<Integer, String> STATUS_TEXT = Map.ofEntries(
            Map.entry(1, "待支付"), Map.entry(2, "已付款"), Map.entry(3, "待发货"),
            Map.entry(4, "已发货"), Map.entry(5, "已签收"), Map.entry(6, "已完成"),
            Map.entry(7, "已取消"), Map.entry(8, "退款中"), Map.entry(9, "已退款"),
            Map.entry(11, "待自提"), Map.entry(12, "虚拟订单已过期"), Map.entry(16, "部分发货"));

    private final MallAdminTokenService mallAdminTokenService;
    private final OrderPaasFeign orderPaasFeign;
    private final OrderBaseQueryMapper orderBaseQueryMapper;

    /**
     * 与 {@code MallAdminTokenAuthenticationFilter} 保持一致：商城管理员 token 与
     * Cloud 内嵌商城的云后台 token 均视为已登录。
     */
    private ResponseResult requireLogin(String token) {
        if (!mallAdminTokenService.isAuthenticated(token)) {
            return ResponseResult.fail(401, "登录已过期");
        }
        return null;
    }

    @PostMapping("/list")
    @PreAuthorize(ORDER_READ)
    public ResponseResult list(@RequestHeader(value = "Authorization", required = false) String token,
                               @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        // order 服务的 queryOrders 从请求体读取 token 字段（该路径在 order 侧 JWT 白名单中，
        // 不建立 SecurityContext），而这里只透传 Authorization 头，必须注入，
        // 否则 order 侧取不到 token 会返回 10016「token失效,请重新登陆」。
        // 注意原写法是 Map.of()（不可变），注入前必须换成可变 Map，否则 put 会抛异常。
        Map<String, Object> body = params == null ? new HashMap<>() : new HashMap<>(params);
        if (!body.containsKey("token")) {
            body.put("token", stripBearer(token));
        }
        mapListRequest(body);
        // PC 版查询：order 侧对平台超管（permissions=["*"]）放开「仅本人」过滤，
        // 可看全平台订单；C 端 queryOrders 恒按 buyer_user_id 过滤，管理端不可用。
        ResponseResult result = orderPaasFeign.queryOrdersByPC(body);
        return adaptListResponse(result);
    }

    /**
     * 订单详情（后台）。前端 OrderDetail.vue 契约：data.{orderNo,status,createTime,
     * paymentMethod,payTime,shipTime,receiverName,receiverPhone,receiverAddress,
     * goodsList[{goodsImage,goodsName,goodsSpec,price,quantity}],goodsAmount,
     * discountAmount,shippingFee,totalAmount,logisticsInfo{company,trackingNo},remark}。
     * order 侧 platForm/backGroundByOrderDetails 按 orderId 查询、不带用户过滤，
     * 返回 OrderbaseDetailsparaVO（camelCase），此处做单点字段转换。
     */
    @PostMapping("/detail")
    @PreAuthorize(ORDER_READ)
    public ResponseResult detail(@RequestHeader(value = "Authorization", required = false) String token,
                                 @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        Map<String, Object> body = params == null ? new HashMap<>() : new HashMap<>(params);
        if (!body.containsKey("token")) {
            body.put("token", stripBearer(token));
        }
        ResponseResult result = orderPaasFeign.backGroundByOrderDetails(token, body);
        return adaptDetailResponse(result);
    }

    /**
     * 请求参数转换：admin 前端契约（page_no/page_size/status/key_word/start_time/end_time）
     * → order 服务 queryOrdersByPC 契约（page/limit/orderStatusNum/orderNo/timeStart/timeEnd）。
     * status 直传 order 枚举码：0 待支付→1  1 待发货→3  2 待收货→4  3 已完成→6(确认收货)
     * 4 已取消→7；空 = 全部。
     */
    private void mapListRequest(Map<String, Object> body) {
        Object pageNo = body.remove("page_no");
        if (pageNo != null) {
            body.put("page", pageNo);
        }
        Object pageSize = body.remove("page_size");
        if (pageSize != null) {
            body.put("limit", pageSize);
        }
        Object status = body.remove("status");
        if (status != null && !String.valueOf(status).isEmpty()) {
            int s = Integer.parseInt(String.valueOf(status));
            Integer orderStatusNum = switch (s) {
                case 0 -> 1;
                case 1 -> 3;
                case 2 -> 4;
                case 3 -> 6;
                case 4 -> 7;
                default -> null;
            };
            if (orderStatusNum != null) {
                body.put("orderStatusNum", orderStatusNum);
            }
        }
        Object keyword = body.remove("key_word");
        if (keyword != null && !String.valueOf(keyword).isEmpty()) {
            body.put("orderNo", keyword);
        }
        Object startTime = body.remove("start_time");
        if (startTime != null && !String.valueOf(startTime).isEmpty()) {
            body.put("timeStart", startTime);
        }
        Object endTime = body.remove("end_time");
        if (endTime != null && !String.valueOf(endTime).isEmpty()) {
            body.put("timeEnd", endTime);
        }
    }

    /**
     * 响应转换：PC 版 PageInfo{page:{totalCount,list}} + OrderListVO(camelCase)
     * → 旧 mall-order 契约 {lists, count, extend} + 行内 snake_case 字段。
     * （兼容旧 C 端 PageUtils 顶层结构，便于回退。）
     */
    private ResponseResult adaptListResponse(ResponseResult result) {
        if (result == null || result.getCode() != 1 || result.getData() == null) {
            return result;
        }
        try {
            JSONObject data = JSON.parseObject(JSON.toJSONString(result.getData()));
            JSONObject page = data.getJSONObject("page") != null ? data.getJSONObject("page") : data;
            JSONArray rows = page.getJSONArray("list");
            JSONObject out = new JSONObject(new LinkedHashMap<>());
            out.put("count", page.getInteger("totalCount") == null ? 0 : page.getInteger("totalCount"));
            JSONArray lists = new JSONArray();
            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    lists.add(adaptOrderRow(rows.getJSONObject(i)));
                }
            }
            out.put("lists", lists);
            // order 的 C 端 SQL 不带收货联系方式/地址，按订单号对本库补查（见 OrderBaseQueryMapper）
            fillReceiverInfo(lists);
            out.put("extend", buildTabCounts());
            result.setData(out);
        } catch (Exception e) {
            log.warn("订单列表响应适配失败，按原始结构返回: {}", e.getMessage());
        }
        return result;
    }

    /**
     * tab 徽标：order 侧无分状态统计接口，对本库 group by 实时统计
     * （口径与 queryOrderList 基础过滤一致：order_del=0 且非自提单；
     * 部分发货 16 计入待收货，退款中 8/已退款 9 计入已取消旁路暂归 0）。
     */
    private JSONObject buildTabCounts() {
        JSONObject extend = new JSONObject(new LinkedHashMap<>());
        long pay = 0, delivery = 0, receive = 0, finish = 0, close = 0, all = 0;
        try {
            for (Map<String, Object> m : orderBaseQueryMapper.selectStatusCounts()) {
                int status = Integer.parseInt(String.valueOf(m.get("orderStatus")));
                long c = Long.parseLong(String.valueOf(m.get("cnt")));
                all += c;
                switch (status) {
                    case 1 -> pay += c;
                    case 3 -> delivery += c;
                    case 4, 5, 11, 16 -> receive += c;
                    case 6 -> finish += c;
                    case 7, 8, 9, 12 -> close += c;
                    default -> { }
                }
            }
        } catch (Exception e) {
            log.warn("订单 tab 统计失败，全部置 0: {}", e.getMessage());
        }
        extend.put("all_count", all);
        extend.put("pay_count", pay);
        extend.put("delivery_count", delivery);
        extend.put("receive_count", receive);
        extend.put("finish_count", finish);
        extend.put("close_count", close);
        return extend;
    }

    /** 详情响应转换：OrderbaseDetailsparaVO → 前端 OrderDetail.vue 契约（失败按原样返回）。 */
    private ResponseResult adaptDetailResponse(ResponseResult result) {
        if (result == null || result.getCode() != 1 || result.getData() == null) {
            return result;
        }
        try {
            JSONObject o = JSON.parseObject(JSON.toJSONString(result.getData()));
            JSONObject out = new JSONObject(new LinkedHashMap<>());
            out.put("orderNo", o.getString("orderId"));
            out.put("status", mapDetailStatus(o.getInteger("orderStatus")));
            out.put("createTime", o.getString("orderCreateTime"));
            out.put("payTime", null);
            out.put("shipTime", o.getString("orderShippingTime"));
            out.put("receiverName", o.getString("orderReceiverName"));
            out.put("receiverPhone", o.getString("orderReceiverContact"));
            out.put("receiverAddress", o.getString("orderReceiverAddress"));
            out.put("shopName", o.getString("shopName"));
            out.put("remark", o.getString("orderMessage"));
            JSONArray goods = new JSONArray();
            JSONArray details = o.getJSONArray("goodsAllByidParam");
            if (details == null) {
                // fastjson 对字段名 GoodsAllByidParam（大写开头）序列化键可能保留原样，做一次兜底
                details = o.getJSONArray("GoodsAllByidParam");
            }
            if (details != null) {
                for (int i = 0; i < details.size(); i++) {
                    JSONObject d = details.getJSONObject(i);
                    JSONObject g = new JSONObject(new LinkedHashMap<>());
                    g.put("goodsImage", d.getString("goodsImage"));
                    g.put("goodsName", d.getString("goodsName"));
                    g.put("goodsSpec", d.getString("orderSpecChineseValue"));
                    g.put("price", d.getBigDecimal("goodsPrice"));
                    g.put("quantity", d.getInteger("orderGoodsNum"));
                    goods.add(g);
                }
            }
            out.put("goodsList", goods);
            out.put("goodsAmount", o.getBigDecimal("orderGoodsAmount"));
            out.put("discountAmount", o.getBigDecimal("orderRptPrice"));
            out.put("shippingFee", o.getBigDecimal("orderShippingFee"));
            out.put("totalAmount", o.getBigDecimal("orderPaymentAmount"));
            JSONObject logistics = new JSONObject(new LinkedHashMap<>());
            logistics.put("company", o.getString("expressName") == null ? "" : o.getString("expressName"));
            logistics.put("trackingNo", o.getString("orderShippingCode") == null ? "" : o.getString("orderShippingCode"));
            logistics.put("traces", new JSONArray());
            out.put("logisticsInfo", logistics);
            result.setData(out);
        } catch (Exception e) {
            log.warn("订单详情响应适配失败，按原始结构返回: {}", e.getMessage());
        }
        return result;
    }

    /** orderStatus 枚举 → 前端详情页状态（0 待付款 1 待发货 2 已发货 3 已完成 4 已取消）。 */
    private Integer mapDetailStatus(Integer orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return switch (orderStatus) {
            case 1 -> 0;
            case 2, 3, 11 -> 1;
            case 4, 5, 16 -> 2;
            case 6 -> 3;
            default -> 4; // 7 取消 / 12 过期 / 8 退款中 / 9 已退款
        };
    }

    /** 补齐收货人联系方式/地址（仅对空字段回填，不覆盖 order 侧已返回的值） */
    private void fillReceiverInfo(JSONArray lists) {
        try {
            java.util.List<String> ids = new java.util.ArrayList<>();
            for (int i = 0; i < lists.size(); i++) {
                JSONObject row = lists.getJSONObject(i);
                if (row.getString("address_mobile") == null || row.getString("address_content") == null) {
                    ids.add(row.getString("id"));
                }
            }
            if (ids.isEmpty()) {
                return;
            }
            Map<String, JSONObject> byId = new HashMap<>();
            for (Map<String, Object> m : orderBaseQueryMapper.selectReceivers(ids)) {
                byId.put(String.valueOf(m.get("orderId")), JSON.parseObject(JSON.toJSONString(m)));
            }
            for (int i = 0; i < lists.size(); i++) {
                JSONObject row = lists.getJSONObject(i);
                JSONObject info = byId.get(row.getString("id"));
                if (info == null) {
                    continue;
                }
                if (row.getString("address_contact") == null) {
                    row.put("address_contact", info.getString("receiverName"));
                }
                if (row.getString("address_mobile") == null) {
                    row.put("address_mobile", info.getString("receiverContact"));
                }
                if (row.getString("address_content") == null) {
                    row.put("address_content", info.getString("receiverAddress"));
                }
            }
        } catch (Exception e) {
            log.warn("订单收货人信息补查失败，保持空值展示: {}", e.getMessage());
        }
    }

    /** OrderListVO(camelCase) → 旧契约行（snake_case，仅前端实际消费的字段） */
    private JSONObject adaptOrderRow(JSONObject o) {
        JSONObject row = new JSONObject(new LinkedHashMap<>());
        String orderId = o.getString("orderId");
        row.put("id", orderId);
        row.put("sn", orderId);
        Integer orderStatus = o.getInteger("orderStatus");
        row.put("status", orderStatus);
        row.put("status_text", STATUS_TEXT.getOrDefault(orderStatus, "未知"));
        row.put("type_text", o.getInteger("virtualGoods") != null && o.getInteger("virtualGoods") == 1
                ? "虚拟订单" : "普通订单");
        row.put("nickname", o.getString("buyerUserName") != null
                ? o.getString("buyerUserName") : o.getString("userId"));
        JSONArray goods = new JSONArray();
        JSONArray details = o.getJSONArray("goodsDetailsList");
        if (details != null) {
            for (int i = 0; i < details.size(); i++) {
                JSONObject d = details.getJSONObject(i);
                JSONObject g = new JSONObject(new LinkedHashMap<>());
                g.put("goods_image", d.getString("goodsImage"));
                g.put("goods_name", d.getString("goodsName"));
                String spec = d.getString("orderSpecChineseValue");
                g.put("goods_sku_value", spec == null || spec.isEmpty()
                        ? new JSONArray() : new JSONArray(java.util.List.of(spec)));
                g.put("goods_price", d.getBigDecimal("goodsPrice"));
                g.put("goods_num", d.getInteger("orderGoodsNum"));
                goods.add(g);
            }
        }
        row.put("goods", goods);
        row.put("need_pay_money", o.getBigDecimal("orderPaymentAmount"));
        row.put("express_money", o.getBigDecimal("orderShippingFee"));
        row.put("address_contact", o.getString("orderReceiverName"));
        row.put("address_mobile", o.getString("orderReceiverContact"));
        row.put("address_content", o.getString("orderReceiverAddress"));
        row.put("pay_is", orderStatus != null && orderStatus != 1);
        row.put("create_time", o.getString("orderCreateTime"));
        return row;
    }

    /**
     * 去掉 Authorization 头的 {@code "Bearer "} 前缀，还原成裸 JWT。
     * order 侧以 Redis key（前缀 + token）查登录态，带前缀会查不到。
     */
    private String stripBearer(String token) {
        if (token == null) {
            return null;
        }
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
