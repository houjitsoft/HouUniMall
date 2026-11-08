package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName LiveStudioResult.java
 * @Description TODO
 * @createTime 2021年08月27日 11:02:00
 */
@Data
public class LiveStudioResultVO {

    private Integer errcode;

    private String errmsg;

    private List<RoomInfoVO> room_info;

    private Integer total;


}
