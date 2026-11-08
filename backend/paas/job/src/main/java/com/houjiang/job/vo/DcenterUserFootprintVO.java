package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏商品访问足迹表
 * @TableName ${database.prefix.datav}hou_datav_user_footprint
 * @author Amos
 */

@Data
public class DcenterUserFootprintVO implements Serializable {
    /**
     * 
     */

    private Integer footprintId;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 时间
     */
    private String footprintTime;

}