package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 直播互动消息 DTO
 * @author Amos
 */
@Data
public class LiveMessageDTO implements Serializable {

    private Long roomId;

    private String userId;

    private String userNick;

    private Integer msgType;

    private String content;
}
