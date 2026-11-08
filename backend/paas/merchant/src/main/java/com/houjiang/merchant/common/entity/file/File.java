package com.houjiang.merchant.common.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "类目ID")
    private Integer cid;

    @Schema(description = "上传者id")
    private Integer sourceId;

    @Schema(description = "来源类型[0-后台,1-用户]")
    private Integer source;

    @Schema(description = "文件类型: [10=图片, 20=视频]")
    private Integer type;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件路径")
    private String uri;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
