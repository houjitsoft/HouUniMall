package com.houjiang.merchant.vo.album;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FileVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "所属类目")
    private Integer cid;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件路径")
    private String url;

    @Schema(description = "相对路径")
    private String uri;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
