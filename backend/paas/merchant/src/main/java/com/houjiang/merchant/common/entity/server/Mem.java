package com.houjiang.merchant.common.entity.server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Mem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "内存总量")
    private double total;

    @Schema(description = "已用内存")
    private double used;

    @Schema(description = "剩余内存")
    private double free;

    @Schema(description = "使用率")
    private double usage;

}
