package com.houjiang.goods.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName TransportTemplateDetailDeleteDTO
 * @Author Amos
 * @Date 2023/7/3 10:27
 * @Version 1.0
 * @author Amos
 */
@Data
public class TransportTemplateDetailDeleteDTO extends PageUserTokenBeanDTO {
    /**
     *
     */
    private Integer id;
    /**
     * 国际物流模板 1 是 2 否
     */
    private Integer internationalTemplate;

    /**
     * 店铺id
     */
    private String shopId;
}
