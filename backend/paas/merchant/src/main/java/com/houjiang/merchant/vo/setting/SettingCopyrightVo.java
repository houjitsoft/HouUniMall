package com.houjiang.merchant.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingCopyrightVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    private String key;

    @Schema(description = "链接")
    private String value;

}
