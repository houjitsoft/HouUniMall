package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public class ShopEnum {

    public final static Integer TYPE_SELF = 0;

    public final static Integer TYPE_APPLY = 1;

    public final static List<Integer> TYPE_ARR = new ArrayList() {{
        add(TYPE_SELF);
        add(TYPE_APPLY);
    }};

    public final static Map<Integer, String> TYPE_TEXT_ARR = new HashMap() {{
        put(TYPE_SELF, "自营商家");
        put(TYPE_APPLY, "入驻商家");
    }};

    public final static Integer SHOP_STATUS_NORMAL = 1;

    public final static Integer SHOP_STATUS_DISABLED = 0;

    public final static List<Integer> SHOP_STATUS_ARR = new ArrayList() {{
        add(SHOP_STATUS_NORMAL);
        add(SHOP_STATUS_DISABLED);
    }};

    public final static Map<Integer, String> SHOP_STATUS_TEXT_ARR = new HashMap() {{
        put(SHOP_STATUS_NORMAL, "正常");
        put(SHOP_STATUS_DISABLED, "冻结");
    }};


    public final static Integer BUSINESS_STATUS_OPEN = 1;

    public final static Integer BUSINESS_STATUS_PAUSE = 0;

    public final static List<Integer> BUSiNESS_STATUS_ARR = new ArrayList() {{
        add(BUSINESS_STATUS_OPEN);
        add(BUSINESS_STATUS_PAUSE);
    }};

    public final static Map<Integer, String> BUSiNESS_STATUS_TEXT_ARR = new HashMap() {{
        put(BUSINESS_STATUS_OPEN, "营业中");
        put(BUSINESS_STATUS_PAUSE, "暂停营业");
    }};


    public final static Integer HOME_RECOMMEND_IS = 1;

    public final static Integer HOME_RECOMMEND_NO = 0;

    public final static List<Integer> HOME_RECOMMEND_ARR = new ArrayList() {{
        add(HOME_RECOMMEND_IS);
        add(HOME_RECOMMEND_NO);
    }};

    public final static Map<Integer, String> HOME_RECOMMEND_TEXT_ARR = new HashMap() {{
        put(HOME_RECOMMEND_IS, "推荐");
        put(HOME_RECOMMEND_NO, "不推荐");
    }};


    public final static Integer GOODS_AUDIT_NEED = 1;

    public final static Integer GOODS_AUDIT_NO_NEED = 0;

    public final static List<Integer> GOODS_AUDIT_ARR = new ArrayList() {{
        add(GOODS_AUDIT_NEED);
        add(GOODS_AUDIT_NO_NEED);
    }};

    public final static Map<Integer, String> GOODS_AUDIT_TEXT_ARR = new HashMap() {{
        put(GOODS_AUDIT_NEED, "需要审核");
        put(GOODS_AUDIT_NO_NEED, "无需审核");
    }};

}
