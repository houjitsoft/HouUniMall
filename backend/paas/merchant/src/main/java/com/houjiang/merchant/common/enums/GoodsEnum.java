package com.houjiang.merchant.common.enums;


import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.entity.mall.MallGoods;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.util.ConfigUtils;

import java.util.ArrayList;
import java.util.List;

public enum GoodsEnum {

    /** TODO 补充说明 */
    SKU_TYPE_SINGLE(0, "单规格"),
    /** TODO 补充说明 */
    SKU_TYPE_MORE(1, "多规格"),
    // 视频来源 素材库
    VIDEO_FROM_SUCAI(1, "素材库"),
    /** TODO 补充说明 */
    VIDEO_FROM_URI(2, "链接地址"),
    // 运费类型
    GOODS_EXPRESS_TYPE_FREE(0, "包邮"),
    /** TODO 补充说明 */
    GOODS_EXPRESS_TYPE_TPL(1, "运费模板"),
    /** TODO 补充说明 */
    GOODS_EXPRESS_TYPE_MONEY(2, "统一运费"),
    // 状态
    GOODS_STATUS_UP(1, "上架"),
    /** TODO 补充说明 */
    GOODS_STATUS_DOWN(0, "仓库"),
    /** TODO 补充说明 */
    DISCOUNT_TYPE_DEFAULT(0, "默认会员等级"),
    /** TODO 补充说明 */
    DISCOUNT_TYPE_ALONE_DISCOUNT(1, "单独折扣"),
    /** TODO 补充说明 */
    DISCOUNT_TYPE_ALONE_PRICE(2, "单独价格"),
    /** TODO 补充说明 */
    DISTRIBUTION_TYPE_DEFAULT(0, "默认分销等级"),
    /** TODO 补充说明 */
    DISTRIBUTION_TYPE_ALONE_DISCOUNT(1, "单独折扣"),
    /** TODO 补充说明 */
    DISTRIBUTION_TYPE_ALONE_PRICE(2, "单独价格");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    GoodsEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取状态码
     *
     * @author Amos
     * @return Long
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取提示
     *
     * @author Amos
     * @return String
     */
    public String getMsg() {
        return this.msg;
    }


    public static String getGoodsExpressTypeTextArr(Integer code){
        switch (code) {
            case 0:
                return "包邮";
            case 1:
                return "运费模板";
            case 2:
                return "统一运费";
        }
        return "";
    }

    public static String getSkuTypeTextArr(Integer code){
        switch (code) {
            case 0:
                return "单规格";
            case 1:
                return "多规格";
        }
        return "";
    }

    public static String getGoodsStatusTextArr(Integer code){
        switch (code) {
            case 0:
                return "放入仓库";
            case 1:
                return "上架销售";
        }
        return "";
    }

    public static String getVideoFromTextArr(Integer code){
        switch (code) {
            case 1:
                return "素材库";
            case 2:
                return "链接";
        }
        return "";
    }

    public static JSONArray getVideoFromTextArr2() {
        JSONArray ret = new JSONArray();
        List<Object> sucai = new ArrayList<Object>() {{
            add(VIDEO_FROM_SUCAI.getCode());
            add("素材库");
        }};
        List<Object> uri = new ArrayList<Object>() {{
            add(VIDEO_FROM_URI.getCode());
            add("链接");
        }};
        ret.add(sucai);
        ret.add(uri);
        return ret;
    }

    public static JSONArray getGoodsStatusTextArr2() {
        JSONArray ret = new JSONArray();
        List<Object> sucai = new ArrayList<Object>() {{
            add(GOODS_STATUS_UP.getCode());
            add("上架销售");
        }};
        List<Object> uri = new ArrayList<Object>() {{
            add(GOODS_STATUS_DOWN.getCode());
            add("放入仓库");
        }};
        ret.add(sucai);
        ret.add(uri);
        return ret;
    }

    public static JSONArray getSkuTypeTextArr2() {
        JSONArray ret = new JSONArray();
        List<Object> sucai = new ArrayList<Object>() {{
            add(SKU_TYPE_SINGLE.getCode());
            add("单规格");
        }};
        List<Object> uri = new ArrayList<Object>() {{
            add(SKU_TYPE_MORE.getCode());
            add("多规格");
        }};
        ret.add(sucai);
        ret.add(uri);
        return ret;
    }

    public static JSONArray getGoodsExpressTypeTextArr2() {
        JSONArray ret = new JSONArray();
        List<Object> sucai = new ArrayList<Object>() {{
            add(GOODS_EXPRESS_TYPE_FREE.getCode());
            add("包邮");
        }};
        List<Object> uri = new ArrayList<Object>() {{
            add(GOODS_EXPRESS_TYPE_TPL.getCode());
            add("运费模板");
        }};
        List<Object> money = new ArrayList<Object>() {{
            add(GOODS_EXPRESS_TYPE_MONEY.getCode());
            add("统一运费");
        }};
        ret.add(sucai);
        ret.add(uri);
        ret.add(money);
        return ret;
    }


    public static String getPriceText(MallGoods item) {

        if (item.getSkuType().equals(GoodsEnum.SKU_TYPE_SINGLE.getCode())) {
            return item.getMinPrice().toString();
        } else if (item.getSkuType().equals(GoodsEnum.SKU_TYPE_MORE.getCode()) ) {
            if (item.getMinPrice().compareTo(item.getMaxPrice()) == 0) {
                return item.getMinPrice().toString();
            } else {
                return item.getMinPrice() + "~" + item.getMaxPrice();
            }
        }
        return "";
    }

    public static List<Integer> getGoodsSendMode(MallGoods item) {

        if (item.getIsExpress().equals(1)) {
            return DeliveryEnum.DELIVERY_TYPE_ARR;
        }

        return new ArrayList<>();
    }

    public static String getGoodsSendModeText(MallGoods item) {

        Integer express_is = Integer.parseInt(ConfigUtils.get("mall_delivery_type", "express_is", "1"));

        String express_name = ConfigUtils.get("mall_delivery_type", "express_name", "快递发货");

        if (item.getIsExpress().equals(1)) {
            return express_name;
        }

        return "";
    }

    public static Integer getCanSaleAttr(MallGoods item, MallShop shop) {
        return item.getStatus().equals(GoodsEnum.GOODS_STATUS_UP)
                && item.getAuditStatus().equals(AuditEnum.PASS)
                && (shop != null && shop.getShopStatus().equals(ShopEnum.SHOP_STATUS_NORMAL) && shop.getBusinessStatus().equals(ShopEnum.BUSINESS_STATUS_OPEN)) ? 1 : 0;
    }

    public static String getAuditStatusTextAttr(MallGoods item) {
        return AuditEnum.getStatusDesc(item.getAuditStatus());
    }

    public static Integer getCanAuditAttr(MallGoods item) {
        return item.getAuditStatus().equals(AuditEnum.WAIT.getCode()) ? 1 : 0;
    }

    public static Integer getCanReAuditAttr(MallGoods item) {
        return item.getAuditStatus().equals(AuditEnum.PASS.getCode()) ? 1 : 0;
    }
}
