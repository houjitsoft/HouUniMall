package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏商品访问足迹表
 * @TableName ${database.prefix.datav}hou_datav_user_footprint
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_footprint")
@Data
public class DcenterUserFootprintEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer footprintId;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 时间
     */
    private String footprintTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}