package com.houjiang.commons.dto.job;

import lombok.Data;

import java.util.List;

/**
 * 过期标记ID更新
 * @author Amos
 */
@Data
public class ExpireMarkIdUpdateDTO {

    /**
     * 查询过期的团标识
     */
    private List<String> expireMarkIdList;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 订单id 集合
     */
    private List<String> orderIdList;


}
