package com.houjiang.merchant.vo.gather;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGatherCreateVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    private Integer id;
}
