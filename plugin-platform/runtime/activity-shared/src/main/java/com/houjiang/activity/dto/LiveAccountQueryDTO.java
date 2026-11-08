package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 直播账号列表查询 DTO
 * @author Amos
 */
@Data
public class LiveAccountQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer size = 10;

    private String userId;

    private Integer roleType;

    private Integer status;
}
