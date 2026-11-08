package com.houjiang.commons.dto;

import com.houjiang.commons.dto.userms.UserTokenIntegralDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PageUserTokenBeanDTO extends UserTokenIntegralDTO {

    /**
     * 总页数
     */
    private Integer page;

    /**
     * 页大小
     */
    private Integer limit;

    /**
     * 店铺id
     */
    private String shopId;

}
