package com.houjiang.goods.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName CheckNameQueryParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/10 18:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class CheckNameQueryParamDTO extends UserTokenBeanDTO {

    /**
     * 检查名称
     */
    private String checkName;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品公共id
     */
    private Integer commonId;
}
