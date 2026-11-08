package com.houjiang.merchant.validate.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class AnnouncementSearchValidate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "文章标题")
    private String title;
}
