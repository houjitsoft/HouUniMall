package com.houjiang.merchant.vo.setting;

import com.houjiang.merchant.common.entity.setting.HotSearch;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SettingSearchDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否开起热门搜索")
    private Integer status;

    @Schema(description = "热门搜索关键词")
    private List<HotSearch> data;

}
