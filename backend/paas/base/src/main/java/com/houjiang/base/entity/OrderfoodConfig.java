package com.houjiang.base.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class OrderfoodConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer type;

    private String name;

    private Object value;

    private Date createTime;

    private Date updateTime;

    private String className;
}
