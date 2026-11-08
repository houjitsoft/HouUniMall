package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 批量发货查找代发货订单商品请求参数
 *
 * @author Amos
 */
@Data
public class RetrunQueryPrintDTO extends PageHelper {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 下单开始时间
     */
    private String returnAddTimeStart;

    /**
     * 下单结束时间
     */
    private String returnAddTimeEnd;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 商品信息
     */
    private String merchandise;

}
