package com.houjiang.payment.vo;

import com.houjiang.commons.util.querypage.PageUtils;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName PageInfoVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 15:49
 * @Version 1.0
 * @author Amos
 */
@Data
public class PageInfoVO {

    /**
     * 分页数据
     */
    private PageUtils pageList;

    /**
     * 总金额
     */
    private BigDecimal sumRecordMoney;

    /**
     * 总数量
     */
    private BigDecimal sumAmount;

    /**
     * 总付款金额
     */
    private BigDecimal sumPayAmount;
}
