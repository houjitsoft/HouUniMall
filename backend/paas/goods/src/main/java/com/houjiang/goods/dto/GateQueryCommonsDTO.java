package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName GateQueryCommonsDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/2 10:01
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateQueryCommonsDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 平台货号
     */
    private String commonId;
}
