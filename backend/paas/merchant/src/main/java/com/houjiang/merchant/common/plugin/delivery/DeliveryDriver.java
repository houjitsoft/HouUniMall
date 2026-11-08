package com.houjiang.merchant.common.plugin.delivery;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.plugin.delivery.engine.Kd100;
import com.houjiang.merchant.common.plugin.delivery.engine.Kdniao;
import com.houjiang.merchant.common.plugin.delivery.vo.KdQueryTrackParam;
import com.houjiang.merchant.common.plugin.delivery.vo.KdTrackResultVo;
import com.houjiang.merchant.common.util.ConfigUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DeliveryDriver {

    private final String engine;
    private final String requestType;
    private final String customer;
    private final String key;

    /**
     * 初始化
     */
    public DeliveryDriver() {
        String config = ConfigUtils.get("mall_delivery_type", "express_config");
        JSONObject configJ = JSONObject.parseObject(config);
        this.engine = configJ.getString("express_type");
        //快递100
        if (configJ.getString("express_type").equals("express_kd100")) {
            JSONObject engineJSON = configJ.getJSONObject("express_kd100");
            this.key = engineJSON.getString("appkey");
            this.customer = engineJSON.getString("customer");
            this.requestType = null;
        } else {
            //快递鸟
            JSONObject engineJSON = configJ.getJSONObject("express_kdniao");
            this.key = engineJSON.getString("appkey");
            this.customer = engineJSON.getString("e_bussiness_id");
            String requestType = engineJSON.getOrDefault("combo", "free").toString();
            this.requestType = requestType.equals("free") ? "1002" : "8001";
        }
    }

    /**
     * 查询实时物流
     *
     * @author Amos
     * @param kdQueryTrackParam 查询参数
     */
    public List<KdTrackResultVo> queryTrack(KdQueryTrackParam kdQueryTrackParam) {
        List<KdTrackResultVo> result = new LinkedList<>();
        switch (this.engine) {
            case "express_kd100":
                result = (new Kd100()).queryTrack(this.customer, this.key, kdQueryTrackParam);
                break;
            case "express_kdniao":
                result = (new Kdniao()).queryTrack(this.customer, this.key, this.requestType, kdQueryTrackParam);
                break;
        }
        return result;
    }
}
