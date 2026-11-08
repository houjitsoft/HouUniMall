package com.houjiang.merchant.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
public class ShopApplyEnum {


    public final static Integer FOLLOW_NONE = 0;

    public final static Integer FOLLOW_HAS = 1;


    public final static Map<Integer, String> FOLLOW_TEXT_ARR = new HashMap() {{
        put(FOLLOW_NONE, "未跟进");
        put(FOLLOW_HAS, "已跟进");
    }};

}
