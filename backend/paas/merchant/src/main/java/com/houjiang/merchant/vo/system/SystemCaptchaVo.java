package com.houjiang.merchant.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SystemCaptchaVo {

    @Schema(description = "标识")
    private String uuid;

    @Schema(description = "图片")
    private String img;

}
