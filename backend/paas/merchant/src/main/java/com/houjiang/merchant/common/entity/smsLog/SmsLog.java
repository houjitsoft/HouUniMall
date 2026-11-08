package com.houjiang.merchant.common.entity.smsLog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SmsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "场景")
    private Integer sceneId;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "发送内容")
    private String content;

    @Schema(description = "发送关键字（注册、找回密码）")
    private String code;

    @Schema(description = "是否已验证；0-否；1-是")
    private Integer isVerify;

    @Schema(description = "验证次数")
    private Integer checkNum;

    @Schema(description = "发送状态：0-发送中；1-发送成功；2-发送失败")
    private Integer sendStatus;

    @Schema(description = "发送时间")
    private Long sendTime;

    @Schema(description = "短信结果")
    private String results;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;
}
