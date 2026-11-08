package com.houjiang.merchant.vo.pt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PtOpenCountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数量数量")
    private Long count;

    @Schema(description = "成功数量")
    private Long successCount;

    @Schema(description = "参加数量")
    private Long joinCount;

    @Schema(description = "ptID")
    private Integer ptId;
}
