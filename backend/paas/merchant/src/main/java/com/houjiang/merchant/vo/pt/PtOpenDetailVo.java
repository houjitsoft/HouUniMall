package com.houjiang.merchant.vo.pt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class PtOpenDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "状态 0拼团中 1拼团成功 2拼团失败")
    private Integer status;

    @Schema(description = "状态 0拼团中 1拼团成功 2拼团失败")
    private String statusText;
}
