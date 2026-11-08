package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallExpressCompanySingleDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "名称")
    private String name;

}
