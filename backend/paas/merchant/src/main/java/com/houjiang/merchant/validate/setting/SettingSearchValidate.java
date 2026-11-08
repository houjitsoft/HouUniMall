package com.houjiang.merchant.validate.setting;

import com.houjiang.merchant.vo.setting.SettingSearchObjectVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SettingSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否开启热门搜索")
    private Integer status;

    @Schema(description = "关键词列表")
    private List<SettingSearchObjectVo> data;

}
