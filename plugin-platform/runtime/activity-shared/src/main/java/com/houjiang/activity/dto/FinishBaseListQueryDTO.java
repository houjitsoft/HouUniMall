package com.houjiang.activity.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class FinishBaseListQueryDTO extends PageHelper {
    /**
     * 折扣ID数组
     */
    private Integer[] discountIds;

    /**
     * 折扣名称
     */
    private String discountName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 店铺ID
     */
    private Integer shopId;

}
