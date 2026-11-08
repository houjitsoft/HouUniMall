package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCommentSetReplyValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    @NotNull(message = "必须提交ID")
    private Integer id;

    @Schema(description = "回复内容")
    @NotNull(message = "请填写回复内容")
    @Size(min = 1, max = 255, message = "回复内容不能超出255个字符")
    private String replyContent;

}
