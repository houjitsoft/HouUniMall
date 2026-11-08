package com.houjiang.userms.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 权限组表
 * @author Amos
 */
@Data
public class AdminRightsGroupDTO {

    /**
     * token
     */
    private String token;

    /**
     * 权限组id
     */
    private Short rightsGroupId;

    /**
     * 权限组名称
     */
    private String rightsGroupName;

    /**
     * 权限列表
     */
    private String rightsGroupRightsIds;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rightsGroupAddTime;

    /**
     * 逗号分隔所有权限
     */
    private String rightsLimitsAll;


    /**
     * 权限状态
     */
    private String rightsLimitsState;


}
