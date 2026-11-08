package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SpecValueListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "IDS")
    private String ids;

    @Schema(description = "值")
    private String value;
}
