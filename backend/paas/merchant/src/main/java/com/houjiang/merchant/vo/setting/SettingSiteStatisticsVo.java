package com.houjiang.merchant.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingSiteStatisticsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "clarityCode")
    private String clarityCode;

}
