package com.houjiang.commons.dto.userms;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class UserPointLogDTO {

    private Integer pointsLogId;
    private Integer pointsLogType;
    private Integer classId;
    private String userId;
    private String userName;
    private String adminName;
    private Integer pointsLogPoints;
    private Integer freezePoints;
    private Date pointsLogTime;
    private String pointsLogDesc;
    private String pointsLogOperate;
    private String pointsLogFlag;
    private Integer operationType;
    private String token;
    private String orderId;

}
