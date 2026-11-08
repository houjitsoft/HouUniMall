package com.houjiang.base.dto;

import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName QueryResult.java
 * @Description TODO 封装请求对象 wx设置成员接口
 * @createTime 2021年08月18日 09:22:00
 */
@Data
public class QueryResultDTO {

    /**
     * 微信号
     */
    private String username;
    /**
     *取值
     */
    private Integer role;


}
