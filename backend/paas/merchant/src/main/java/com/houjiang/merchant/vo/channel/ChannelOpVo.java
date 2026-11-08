package com.houjiang.merchant.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ChannelOpVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "appId")
    private String appId;

    @Schema(description = "appSecret")
    private String appSecret;

}
