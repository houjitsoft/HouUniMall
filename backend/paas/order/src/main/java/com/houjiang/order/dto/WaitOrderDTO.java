package com.houjiang.order.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName WaitOrderDTO
 * @Description 待结算查询请求
 * @Author Amos
 * @Date 2023/7/18 10:01
 * @Version 1.0
 * @author Amos
 */
@Data
public class WaitOrderDTO extends PageUserTokenBeanDTO {

    /**
     * 1经销业绩订单   2普通订单
     */
    private Integer dropShipping;

    /**
     * 1.供应商中心  2商家中心
     */
    private Integer orderSource;
}
