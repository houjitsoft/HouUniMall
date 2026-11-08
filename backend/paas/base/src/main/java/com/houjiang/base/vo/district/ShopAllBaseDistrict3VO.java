package com.houjiang.base.vo.district;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ShopAllBaseDistrict3VO
 * @Description TODO
 * @Author Amos
 * @Date 2023/7/3 10:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopAllBaseDistrict3VO {

    private Integer districtId;
    private String districtName;
    private Boolean checkAll;
    private Boolean isIndeterminate;
    private List<?> checkedSubMenus;
    private List<ShopAllBaseDistrict2VO> childDistrict;
}
