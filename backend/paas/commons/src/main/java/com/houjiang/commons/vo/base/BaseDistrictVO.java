package com.houjiang.commons.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseDistrictVO {
    private Integer districtId;

    private String districtName;

    private Integer districtParentId;

    private Short districtDisplayorder;

    private String districtRegion;

    private Boolean districtIsLeaf;

    private Boolean districtIsLevel;


}