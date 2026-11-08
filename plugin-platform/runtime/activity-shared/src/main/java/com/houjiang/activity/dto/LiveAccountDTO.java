package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 直播账号配置 DTO
 * @author Amos
 */
@Data
public class LiveAccountDTO implements Serializable {

    private Long id;

    private String userId;

    private String nickName;

    private String avatar;

    private Integer roleType;

    private Integer status;

    private String remark;
}
