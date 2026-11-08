package com.houjiang.base.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ManageExpressQueryVO {

    /**
     * 店铺物流id
     */
    private Integer userExpressId;

    /**
     * 快递公司id
     */
    private Integer expressId;

    private String expressName;

    private Integer flag;

    private Integer expressStatus;
}
