package com.houjiang.mall.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商城后台分页参数
 * <p>
 * 兼容前端 mall/admin 历史约定（来源于 like-admin）：
 * <ul>
 *   <li>前端发送下划线命名：<code>page_no</code> / <code>page_size</code></li>
 *   <li>Java 内部使用驼峰：<code>pageNo</code> / <code>pageSize</code></li>
 * </ul>
 * 通过 Jackson 的 {@link JsonAlias} 同时接受两种命名，序列化输出使用驼峰。
 *
 * @author Amos
 */
@Data
@Accessors(chain = true)
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认 1，最小 1
     */
    @Min(value = 1, message = "pageNo 必须大于0")
    @JsonAlias({"page_no"})
    @JsonProperty("pageNo")
    private Integer pageNo = 1;

    /**
     * 每页条数，默认 15，范围 [1, 200]
     */
    @Min(value = 1, message = "pageSize 必须大于0")
    @Max(value = 200, message = "pageSize 不能超过200")
    @JsonAlias({"page_size"})
    @JsonProperty("pageSize")
    private Integer pageSize = 15;
}
