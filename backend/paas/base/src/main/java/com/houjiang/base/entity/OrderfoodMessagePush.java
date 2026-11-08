package com.houjiang.base.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class OrderfoodMessagePush implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String masterplate;

    private Integer type;

    private String multiplejson;
}
