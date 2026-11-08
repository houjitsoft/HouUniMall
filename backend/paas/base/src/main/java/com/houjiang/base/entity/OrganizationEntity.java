package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName OrganizationEntity
 * @Description 组织信息实体
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_organization")
@Data
public class OrganizationEntity implements Serializable {

    /**
     * 组织ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织类型：1-公司，2-部门，3-小组，4-个人空间（注册用户默认归属）
     */
    private Integer orgType;

    /**
     * 组织状态：1-启用，0-禁用
     */
    private Integer orgStatus;



    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
