package com.houjiang.merchant.common.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class NoticeSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "场景编号")
    private Integer sceneId;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "场景描述")
    private String sceneDesc;

    @Schema(description = "接收人员: [1=用户, 2=平台]")
    private Integer recipient;

    @Schema(description = "通知类型: [1=业务, 2=验证码]")
    private Integer type;

    @Schema(description = "系统的通知设置")
    private String systemNotice;

    @Schema(description = "短信的通知设置")
    private String smsNotice;

    @Schema(description = "公众号通知设置")
    private String oaNotice;

    @Schema(description = "小程序通知设置")
    private String mnpNotice;

    @Schema(description = "更新时间")
    private Long updateTime;

}
