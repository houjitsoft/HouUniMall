package com.houjiang.goods.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateGoodsTransportInfoDTO extends UserTokenBeanDTO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品id
     */
    private List<Integer> commonIdList;

    /**
     * 售卖区域id
     */
    private Integer transportAreaId;

    /**
     * 运费模板
     */
    private Integer transportTemplate;
    /**
     *  国际售卖区域
     */
    private Integer internationalTransportAreaId;
    /**
     * 国际运费模板
     */
    private Integer internationalTransportTemplate;
}
