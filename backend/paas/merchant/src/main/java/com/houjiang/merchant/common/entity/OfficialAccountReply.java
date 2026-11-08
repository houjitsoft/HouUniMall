package com.houjiang.merchant.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class OfficialAccountReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "规则名")
    private String name;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "回复类型: [1=关注回复 2=关键字回复, 3=默认回复]")
    private Integer replyType;

    @Schema(description = "匹配方式: [1=全匹配, 2=模糊匹配]")
    private Integer matchingType;

    @Schema(description = "内容类型: [1=文本]")
    private Integer contentType;

    @Schema(description = "启动状态: [1=启动, 0=关闭]")
    private Integer status;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
