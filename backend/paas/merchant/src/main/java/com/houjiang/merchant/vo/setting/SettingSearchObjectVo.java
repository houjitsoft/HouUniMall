package com.houjiang.merchant.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingSearchObjectVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

}
