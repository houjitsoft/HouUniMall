package com.houjiang.goods.vo;

import com.houjiang.goods.dto.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class GdbMerchantSearchWordVO  extends UserTokenBeanDTO {

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
