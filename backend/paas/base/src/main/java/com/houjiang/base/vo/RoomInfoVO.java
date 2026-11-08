package com.houjiang.base.vo;

import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName RoomInfo.java
 * @Description TODO
 * @createTime 2021年08月27日 11:04:00
 */
@Data
public class RoomInfoVO {
    private String name;

    private String anchor_name;

    private Integer roomid;

    private String cover_img;

    private Integer live_status;

    private String share_img;

    private Integer live_type;


}
