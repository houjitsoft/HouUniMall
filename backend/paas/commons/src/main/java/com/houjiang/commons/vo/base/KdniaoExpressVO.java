package com.houjiang.commons.vo.base;

import lombok.Data;

/**
 * @ClassName KdniaoExpressVO
 * @Description
 * @Author Amos
 * @Date 2023/8/10 14:59
 * @Version 1.0
 * @author Amos
 */
@Data
public class KdniaoExpressVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 快递公司
     */
    private String expressName;

    /**
     * 物流
     */
    private String expressPinyin;

    /**
     * 快递状态
     */
    private Integer expressState;

    /**
     * 操作人姓名
     */
    private Integer customerName;

    /**
     * 操作人密码
     */
    private Integer customerPwd;

    /**
     * 字码
     */
    private Integer monthCode;

    /**
     * 发送码
     */
    private Integer sendSite;

    /**
     * 邮寄人
     */
    private Integer sendStaff;

    /**
     * 物流代码
     */
    private Integer logisticCode;




}
