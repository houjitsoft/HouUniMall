package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCommentSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否回复")
    private Integer is_reply;

    @Schema(description = "关键词")
    private String key_word;

    @Schema(description = "关键词类型")
    private Integer key_word_type;

    @Schema(description = "评价等级")
    private String level;

    @Schema(description = "是否显示")
    private Integer is_show;

    @Schema(description = "评价开始时间")
    private String start_time;

    @Schema(description = "评价结束时间")
    private String end_time;
}
