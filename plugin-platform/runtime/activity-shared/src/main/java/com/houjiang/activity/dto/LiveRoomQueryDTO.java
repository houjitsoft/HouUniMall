package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 直播间列表查询 DTO
 * @author Amos
 */
@Data
public class LiveRoomQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer size = 10;

    private String roomTitle;

    private Integer status;

    private Long anchorAccountId;
}
