package com.houjiang.merchant.vo.setting;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SettingNoticeDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "senceId")
    private Integer senceId;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "通知类型: [1=业务, 2=验证]")
    private String type;

    @Schema(description = "场景描述")
    private String sceneDesc;

    @Schema(description = "系统的通知设置")
    private JSONObject systemNotice;

    @Schema(description = "公众号通知设置")
    private JSONObject oaNotice;

    @Schema(description = "小程序通知设置")
    private JSONObject mnpNotice;

    @Schema(description = "短信的通知设置")
    private JSONObject smsNotice;

}
