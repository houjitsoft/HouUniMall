package com.houjiang.merchant.vo.setting;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingNoticeListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "通知名称")
    private String sceneName;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "通知类型")
    private String typeDesc;

    @Schema(description = "通知状态")
    private String smsStatusDesc;

    @Schema(description = "通知对象")
    private JSONObject smsNotice;

}
