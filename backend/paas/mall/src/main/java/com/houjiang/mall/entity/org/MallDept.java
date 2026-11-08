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
@TableName("hou_mall_dept")
public class MallDept implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private Long pid;

    private Integer sort;

    private String leader;

    private String mobile;

    private Integer status;

    private Integer createTime;

    private Integer updateTime;

    private Integer deleteTime;
}
