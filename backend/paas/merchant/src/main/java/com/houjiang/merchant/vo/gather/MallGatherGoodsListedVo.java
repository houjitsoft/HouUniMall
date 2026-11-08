package com.houjiang.merchant.vo.gather;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGatherGoodsListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "采集id")
    private Integer gatherId;

    @Schema(description = "状态 1成功 0失败")
    private Integer status;

    @Schema(description = "已处理 1是 0否")
    private Integer deal;

    @Schema(description = "链接")
    private String link;

    @Schema(description = "商品内容")
    private Object content;

    @Schema(description = "采集记录")
    private Object gather;

    @Schema(description = "渠道")
    private Integer channel;

    @Schema(description = "渠道")
    private String channelName;

}
