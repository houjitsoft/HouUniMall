package com.houjiang.payment.ums.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.payment.ums.client.UmsClient;
import com.houjiang.payment.ums.client.UmsSignUtil;
import com.houjiang.payment.ums.config.UmsConfigStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业订单交易编排：落库 + 调银商报文。
 */
public class UmsCorpOrderService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DT14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JdbcTemplate jdbc;
    private final UmsConfigStore configStore;
    private final UmsClient client;

    public UmsCorpOrderService(JdbcTemplate jdbc, UmsConfigStore configStore, UmsClient client) {
        this.jdbc = jdbc;
        this.configStore = configStore;
        this.client = client;
    }

    public Map<String, Object> createOrder(long orgId, Map<String, Object> req) {
        UmsConfigStore.Config cfg = configStore.require(orgId);
        String orderNo = required(req, "orderNo");
        long orderAmt = moneyFen(req.get("orderAmt"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankTo(str(req.get("merNo")), cfg.merNo()));
        body.put("orderAmt", String.valueOf(orderAmt));
        body.put("orderTime", blankTo(str(req.get("orderTime")), DT14.format(LocalDateTime.now())));
        body.put("orderNo", orderNo);
        body.put("payerNo", required(req, "payerNo"));
        body.put("payerName", required(req, "payerName"));
        body.put("goodsName", required(req, "goodsName"));
        if (req.get("goodsNum") != null) body.put("goodsNum", String.valueOf(req.get("goodsNum")));
        if (req.get("rsv1") != null) body.put("rsv1", String.valueOf(req.get("rsv1")));

        Map<String, Object> resp = client.invoke(cfg, "202101", body);
        @SuppressWarnings("unchecked")
        Map<String, Object> reqSnap = (Map<String, Object>) resp.get("_req");
        String srcReqId = str(reqSnap.get("srcReqId"));
        String srcReqDate = str(reqSnap.get("srcReqDate"));
        saveOp(orgId, cfg.merNo(), "202101", srcReqId, srcReqDate, orderNo, orderAmt, resp);
        jdbc.update(
                "INSERT INTO hou_ums_corp_order(org_id,mer_no,order_no,src_req_id,src_req_date,order_amt,payer_no,payer_name,goods_name,goods_num,order_state,mark_no,mark_name,bank_no,bank_name,valid_date,fail_reason,raw_resp) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                        + "ON DUPLICATE KEY UPDATE order_state=VALUES(order_state),mark_no=VALUES(mark_no),mark_name=VALUES(mark_name),"
                        + "bank_no=VALUES(bank_no),bank_name=VALUES(bank_name),valid_date=VALUES(valid_date),fail_reason=VALUES(fail_reason),raw_resp=VALUES(raw_resp)",
                orgId, body.get("merNo"), orderNo, srcReqId, srcReqDate, orderAmt,
                body.get("payerNo"), body.get("payerName"), body.get("goodsName"),
                body.getOrDefault("goodsNum", ""),
                str(resp.get("orderState")),
                str(resp.get("markNo")), str(resp.get("markName")),
                str(resp.get("bankNo")), str(resp.get("bankName")),
                str(resp.get("validDate")), str(resp.get("failReason")),
                str(resp.get("_raw")));
        return stripInternal(resp);
    }

    public Map<String, Object> queryOrder(long orgId, Map<String, Object> req) {
        UmsConfigStore.Config cfg = configStore.require(orgId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankTo(str(req.get("merNo")), cfg.merNo()));
        body.put("oriReqId", required(req, "oriReqId"));
        Map<String, Object> resp = client.invoke(cfg, "202106", body);
        saveOp(orgId, cfg.merNo(), "202106", str(((Map<?, ?>) resp.get("_req")).get("srcReqId")),
                str(((Map<?, ?>) resp.get("_req")).get("srcReqDate")),
                str(resp.get("orderNo")), moneyFen(resp.get("orderAmt")), resp);
        syncOrderState(orgId, resp);
        return stripInternal(resp);
    }

    public Map<String, Object> closeOrder(long orgId, Map<String, Object> req) {
        return simpleTxn(orgId, "202103", req, Map.of(
                "merNo", blankOrCfg(orgId, req, "merNo"),
                "oriReqId", required(req, "oriReqId")
        ));
    }

    public Map<String, Object> refund(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("oriReqId", required(req, "oriReqId"));
        body.put("refundOrderNo", required(req, "refundOrderNo"));
        body.put("refundDateTime", blankTo(str(req.get("refundDateTime")), DT14.format(LocalDateTime.now())));
        body.put("refundAmt", String.valueOf(moneyFen(req.get("refundAmt"))));
        body.put("oriOrderDate", required(req, "oriOrderDate"));
        body.put("oriOrderAmt", String.valueOf(moneyFen(req.get("oriOrderAmt"))));
        return simpleTxn(orgId, "202102", req, body);
    }

    public Map<String, Object> transfer(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("payAmt", String.valueOf(moneyFen(req.get("payAmt"))));
        if (req.get("ps") != null) body.put("ps", String.valueOf(req.get("ps")));
        return simpleTxn(orgId, "202104", req, body);
    }

    public Map<String, Object> split(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("payType", blankTo(str(req.get("payType")), "0"));
        String cardNo = required(req, "cardNo");
        // 文档：卡号做 Hash 运算 SHA-256；若已是 64 位 hex 则不再二次哈希
        body.put("cardNo", cardNo.matches("(?i)^[0-9a-f]{64}$") ? cardNo.toLowerCase() : UmsSignUtil.sha256Hex(cardNo));
        body.put("ps", required(req, "ps"));
        body.put("payAmt", String.valueOf(moneyFen(req.get("payAmt"))));
        return simpleTxn(orgId, "202105", req, body);
    }

    public Map<String, Object> queryOp(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("reqDate", required(req, "reqDate"));
        body.put("reqJournalNo", required(req, "reqJournalNo"));
        return simpleTxn(orgId, "202107", req, body);
    }

    public Map<String, Object> balance(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        return simpleTxn(orgId, "202108", req, body);
    }

    public Map<String, Object> queryRefund(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("refundOrderNo", required(req, "refundOrderNo"));
        return simpleTxn(orgId, "202109", req, body);
    }

    public Map<String, Object> cancelRefund(long orgId, Map<String, Object> req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merNo", blankOrCfg(orgId, req, "merNo"));
        body.put("refundOrderNo", required(req, "refundOrderNo"));
        if (req.get("oriReqId") != null) body.put("oriReqId", String.valueOf(req.get("oriReqId")));
        return simpleTxn(orgId, "202110", req, body);
    }

    public Map<String, Object> listOrders(long orgId, Map<String, Object> req) {
        int page = intVal(req.get("pageNum"), 1);
        int size = intVal(req.get("pageSize"), 10);
        int offset = Math.max(0, (page - 1) * size);
        String orderNo = str(req.get("orderNo"));
        String state = str(req.get("orderState"));
        StringBuilder where = new StringBuilder(" WHERE org_id=? ");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        args.add(orgId);
        if (!orderNo.isEmpty()) {
            where.append(" AND order_no=? ");
            args.add(orderNo);
        }
        if (!state.isEmpty()) {
            where.append(" AND order_state=? ");
            args.add(state);
        }
        Integer total = jdbc.queryForObject("SELECT COUNT(1) FROM hou_ums_corp_order" + where, Integer.class, args.toArray());
        args.add(size);
        args.add(offset);
        List<Map<String, Object>> records = jdbc.queryForList(
                "SELECT id,mer_no,order_no,src_req_id,src_req_date,order_amt,payer_no,payer_name,goods_name,order_state,"
                        + "mark_no,mark_name,bank_no,bank_name,valid_date,order_pay_date,refunded_amt,fail_reason,create_time "
                        + "FROM hou_ums_corp_order" + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                args.toArray());
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("records", records);
        pageData.put("total", total == null ? 0 : total);
        pageData.put("pageNum", page);
        pageData.put("pageSize", size);
        return pageData;
    }

    public Map<String, Object> listOps(long orgId, Map<String, Object> req) {
        int page = intVal(req.get("pageNum"), 1);
        int size = intVal(req.get("pageSize"), 10);
        int offset = Math.max(0, (page - 1) * size);
        String transCode = str(req.get("transCode"));
        StringBuilder where = new StringBuilder(" WHERE org_id=? ");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        args.add(orgId);
        if (!transCode.isEmpty()) {
            where.append(" AND trans_code=? ");
            args.add(transCode);
        }
        Integer total = jdbc.queryForObject("SELECT COUNT(1) FROM hou_ums_corp_op" + where, Integer.class, args.toArray());
        args.add(size);
        args.add(offset);
        List<Map<String, Object>> records = jdbc.queryForList(
                "SELECT id,mer_no,trans_code,src_req_id,src_req_date,order_no,pay_amt,status,order_type,resp_code,resp_msg,create_time "
                        + "FROM hou_ums_corp_op" + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                args.toArray());
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("records", records);
        pageData.put("total", total == null ? 0 : total);
        pageData.put("pageNum", page);
        pageData.put("pageSize", size);
        return pageData;
    }

    public void handleNotify(long orgId, Map<String, Object> payload) {
        UmsConfigStore.Config cfg = configStore.get(orgId);
        if (cfg != null && cfg.umsPublicKey() != null && !cfg.umsPublicKey().isBlank()
                && !UmsSignUtil.verify(payload, cfg.umsPublicKey())) {
            throw new IllegalStateException("通知验签失败");
        }
        String notifyId = str(payload.get("notifyId"));
        if (notifyId.isEmpty()) notifyId = UmsClient.newReqId();
        String raw;
        try {
            raw = MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            raw = String.valueOf(payload);
        }
        jdbc.update(
                "INSERT IGNORE INTO hou_ums_corp_notify(org_id,notify_id,notify_type,mer_order_no,order_status,payload,processed) VALUES(?,?,?,?,?,?,0)",
                orgId, notifyId, str(payload.get("notifyType")), str(payload.get("merOrderNo")),
                str(payload.get("orderStatus")), raw);
        String merOrderNo = str(payload.get("merOrderNo"));
        String orderStatus = str(payload.get("orderStatus"));
        if (!merOrderNo.isEmpty() && !orderStatus.isEmpty()) {
            jdbc.update("UPDATE hou_ums_corp_order SET order_state=?, order_pay_date=?, update_time=NOW() WHERE org_id=? AND (order_no=? OR mark_no=?)",
                    orderStatus, str(payload.get("payDate")), orgId, merOrderNo, merOrderNo);
        }
        jdbc.update("UPDATE hou_ums_corp_notify SET processed=1 WHERE org_id=? AND notify_id=?", orgId, notifyId);
    }

    private Map<String, Object> simpleTxn(long orgId, String transCode, Map<String, Object> req, Map<String, Object> body) {
        UmsConfigStore.Config cfg = configStore.require(orgId);
        Map<String, Object> resp = client.invoke(cfg, transCode, body);
        @SuppressWarnings("unchecked")
        Map<String, Object> reqSnap = (Map<String, Object>) resp.get("_req");
        saveOp(orgId, cfg.merNo(), transCode, str(reqSnap.get("srcReqId")), str(reqSnap.get("srcReqDate")),
                str(req.get("orderNo")), moneyFen(body.get("payAmt") != null ? body.get("payAmt") : body.get("refundAmt")), resp);
        return stripInternal(resp);
    }

    private void saveOp(long orgId, String merNo, String transCode, String srcReqId, String srcReqDate,
                        String orderNo, long payAmt, Map<String, Object> resp) {
        String status = "2";
        String respCode = str(resp.get("respCode"));
        if (!respCode.isEmpty() && !"00000000".equals(respCode) && !"0000".equals(respCode)) {
            status = "3";
        }
        try {
            jdbc.update(
                    "INSERT INTO hou_ums_corp_op(org_id,mer_no,trans_code,src_req_id,src_req_date,order_no,pay_amt,status,order_type,resp_code,resp_msg,req_body,resp_body) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) "
                            + "ON DUPLICATE KEY UPDATE status=VALUES(status),resp_code=VALUES(resp_code),resp_msg=VALUES(resp_msg),resp_body=VALUES(resp_body)",
                    orgId, merNo, transCode, srcReqId, srcReqDate, nullToEmpty(orderNo), payAmt, status,
                    str(resp.get("orderType")), respCode, str(resp.get("respMsg")),
                    MAPPER.writeValueAsString(resp.get("_req")), str(resp.get("_raw")));
        } catch (Exception ignored) {
            // 操作记录失败不影响主交易回执
        }
    }

    private void syncOrderState(long orgId, Map<String, Object> resp) {
        String orderNo = str(resp.get("orderNo"));
        if (orderNo.isEmpty()) return;
        jdbc.update(
                "UPDATE hou_ums_corp_order SET order_state=?, order_pay_date=?, refunded_amt=?, fail_reason=?, raw_resp=?, update_time=NOW() "
                        + "WHERE org_id=? AND order_no=?",
                str(resp.get("orderState")), str(resp.get("orderPayDate")),
                moneyFen(resp.get("refundedAmt")), str(resp.get("failReason")),
                str(resp.get("_raw")), orgId, orderNo);
    }

    private String blankOrCfg(long orgId, Map<String, Object> req, String key) {
        String v = str(req.get(key));
        if (!v.isEmpty()) return v;
        return configStore.require(orgId).merNo();
    }

    private static Map<String, Object> stripInternal(Map<String, Object> resp) {
        Map<String, Object> out = new HashMap<>(resp);
        out.remove("_req");
        out.remove("_raw");
        return out;
    }

    private static String required(Map<String, Object> m, String key) {
        String v = str(m.get(key));
        if (v.isEmpty()) throw new IllegalArgumentException("缺少参数: " + key);
        return v;
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static String blankTo(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private static long moneyFen(Object v) {
        if (v == null || String.valueOf(v).isBlank()) return 0L;
        String s = String.valueOf(v).trim();
        if (s.contains(".")) {
            return Math.round(Double.parseDouble(s) * 100);
        }
        return Long.parseLong(s);
    }

    private static int intVal(Object v, int def) {
        if (v == null || String.valueOf(v).isBlank()) return def;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
