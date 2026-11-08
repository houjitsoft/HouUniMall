package com.houjiang.mall.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
@TableName("hou_mall_jobs")
public class MallJobs implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private String remark;

    private Integer createTime;

    private Integer updateTime;

    private Integer deleteTime;
}
