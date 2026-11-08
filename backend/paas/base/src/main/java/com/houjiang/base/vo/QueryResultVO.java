package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName QueryResultVO
 * @Description
 * @Author Amos
 * @Date 2023/7/24 14:27
 * @Version 1.0
 * @author Amos
 */
@Data
public class QueryResultVO {

    /**
     * 微信号
     */
    private String username;

    /**
     * 取值
     */
    private Integer role;
}
