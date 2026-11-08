package com.houjiang.merchant.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
public class ShopCategoryEnum {


    public final static Integer AUDIT_STATUS_WAIT = 0;

    public final static Integer AUDIT_STATUS_SUCCESS = 1;

    public final static Integer AUDIT_STATUS_REFUSE = 2;


    public final static Map<Integer, String> AUDIT_STATUS_TEXTS = new HashMap() {{
        put(AUDIT_STATUS_WAIT, "待审核");
        put(AUDIT_STATUS_SUCCESS, "通过");
        put(AUDIT_STATUS_REFUSE, "拒绝");
    }};

}
