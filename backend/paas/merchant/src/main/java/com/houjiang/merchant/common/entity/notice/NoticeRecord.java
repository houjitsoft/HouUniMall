package com.houjiang.merchant.common.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class NoticeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "用户")
    private Integer userId;

    @Schema(description = "编码")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "场景")
    private Integer sceneId;

    @Schema(description = "已读状态: [0=未读, 1=已读]")
    @TableField(value = "`read`")
    private Integer read;

    @Schema(description = "通知接收对象类型;1-会员;2-商家;3-平台;4-游客(未注册用户)")
    private Integer recipient;

    @Schema(description = "通知发送类型 1-系统通知 2-短信通知 3-微信模板 4-微信小程序")
    private Integer sendType;
    @Schema(description = "通知类型 1-业务通知 2-验证码")
    private Integer noticeType;
    @Schema(description = "其他")
    private String extra;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;
}
