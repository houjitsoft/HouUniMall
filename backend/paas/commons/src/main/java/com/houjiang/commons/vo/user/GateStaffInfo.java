package com.houjiang.commons.vo.user;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class GateStaffInfo {
    private Long id;
    /**
     * 门店ID
     */
    private Long shopGateId;
    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号码
     */
    private String phone;
    /**
     * 性别 0男 1女
     */
    private Integer sex;

    /**
     * 角色ID 1店长 2收银员
     */
    private Long roleId;

    /**
     * 账号状态 0正常 1禁用
     */
    private Integer status;
    /**
     * 入职时间
     */
    private Date entryTime;

    /**
     * 当前时间
     */
    private Date nowTime=new Date();
    /**
     * 工号
     */
    private String jobNumber;
    /**
     * 门店名称
     */
    private String shopGateName;
    /**
     * 登录时间
     */
    private Date loginStartTime;
    /**
     * token信息
     */
    private String token;
    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺地址
     */
    private String address;
}
