package com.houjiang.merchant.validate.setting;

import com.houjiang.merchant.vo.setting.SettingCopyrightVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SettingCopyrightValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "版权列表")
    private List<SettingCopyrightVo> config;

}
