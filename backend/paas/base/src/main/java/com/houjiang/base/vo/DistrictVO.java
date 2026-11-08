package com.houjiang.base.vo;

import lombok.Data;

/**
 * 分区
 * @author Amos
 */
@Data
public class DistrictVO {

    /**
     * 分区ID
     */
    private Integer districtId;

    /**
     * 分区名称
     */
    private String districtName;

    /**
     * 分区父id
     */
    private Integer districtParentId;

}
