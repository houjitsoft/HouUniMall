package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏店铺入驻信息表
 * @TableName ${database.prefix.datav}hou_datav_shop_join_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_shop_join_base")
@Data
public class DcenterShopJoinBaseEntity implements Serializable {
    /**
     * 
     */
    @TableId
    private String joinId;

    /**
     * 店铺所在地-省
     */
    private Integer shopProvince;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 审核状态:0-待审核；1-审核通过；2-审核不通过
     */
    private Integer auditStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}