package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 直播间配置 DTO
 * @author Amos
 */
@Data
public class LiveRoomDTO implements Serializable {

    private Long id;

    private String roomTitle;

    private String coverImage;

    private Long anchorAccountId;

    private Integer status;

    private Long scheduledStartTime;

    private Long startTime;

    private Long endTime;
}
