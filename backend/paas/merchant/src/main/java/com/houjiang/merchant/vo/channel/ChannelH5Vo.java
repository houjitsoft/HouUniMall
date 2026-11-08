package com.houjiang.merchant.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelH5Vo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否关闭")
    private Integer status;

    @Schema(description = "关闭类型")
    private Integer pageStatus;

    @Schema(description = "关闭访问")
    private String url;

    @Schema(description = "文件页面地址")
    private String pageUrl;

}
