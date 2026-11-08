package com.houjiang.commons.dto.goods;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsMerchantSearchWordDTO extends PageHelper {

    /**
     * 搜索id
     */
    private Integer searchId;

    /**
     * key值
     */
    private String searchKeyword;

    /**
     * 下标
     */
    private String searchCharIndex;

    /**
     * 数量
     */
    private Integer searchNums;


}
