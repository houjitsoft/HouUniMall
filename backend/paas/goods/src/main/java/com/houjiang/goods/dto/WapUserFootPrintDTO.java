package com.houjiang.goods.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

import java.util.List;

/**
 * 足迹查询参数
 *
 * @author Amos
 */
@Data
public class WapUserFootPrintDTO extends PageUserTokenBeanDTO {
    /**
     * 商城用户id
     */
    private String mallUserId;
    /**
     * 日期
     */
    private String footprintDate;

    /**
     * 商品公共id集合
     */
    private List<Integer> commonIds;


}
