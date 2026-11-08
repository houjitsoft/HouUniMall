package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public class ShopDecorateEnum {

    public final static Integer TYPE_HOME = 0;

    public final static Integer TYPE_SERVICE = 1;
    public final static Integer TYPE_BOTTOM_NAV = 2;

    public final static List<Integer> TYPE_ARR = new ArrayList() {{
        add(TYPE_HOME);
        add(TYPE_SERVICE);
        add(TYPE_BOTTOM_NAV);
    }};

    public final static Map<Integer, String> TYPE_TEXT_ARR = new HashMap() {{
        put(TYPE_HOME, "店铺主页");
        put(TYPE_SERVICE, "客服");
        put(TYPE_BOTTOM_NAV, "底部导航");
    }};

}
