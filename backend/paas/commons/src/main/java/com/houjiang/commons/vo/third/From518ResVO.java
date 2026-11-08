package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 接口统一返回参数
 * @CreateDate: 2025/7/8 22:47
 * @author Amos
 */
@Data
public class From518ResVO<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;
}
