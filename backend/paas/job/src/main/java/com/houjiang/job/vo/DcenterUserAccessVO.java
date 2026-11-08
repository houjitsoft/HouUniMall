package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏用户访问表
 * @TableName ${database.prefix.datav}hou_datav_user_access
 * @author Amos
 */

@Data
public class DcenterUserAccessVO implements Serializable {
    /**
     * 
     */

    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 添加时间
     */
    private String addTime;


}