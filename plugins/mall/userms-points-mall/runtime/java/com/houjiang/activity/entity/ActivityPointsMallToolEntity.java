package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 积分商城工具配置
 * @TableName activity_points_mall_tool
 * @author Amos
 */
@TableName(value = "activity_points_mall_tool")
@Data
public class ActivityPointsMallToolEntity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long toolId;

    /**
     * 工具名称（例如：积分商城首页楼层、积分专区推荐位）
     */
    private String toolName;

    /**
     * 工具编码（前端约定使用，用于区分不同位置，如 POINTS_HOME_BANNER）
     */
    private String toolCode;

    /**
     * 展示位置编码（页面或模块位置标识）
     */
    private String positionCode;

    /**
     * 配置内容（JSON 字符串，前端自定义解析）
     */
    private String configJson;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 生效开始时间
     */
    private Date startTime;

    /**
     * 生效结束时间
     */
    private Date endTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
