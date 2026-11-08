package com.houjiang.base.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName OrganizationDTO
 * @Description 组织信息DTO
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrganizationDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织类型：1-公司，2-部门，3-小组
     */
    private Integer orgType;

    /**
     * 父组织ID
     */
    private String parentOrgId;

    /**
     * 组织层级
     */
    private Integer orgLevel;

    /**
     * 组织状态：1-启用，0-禁用
     */
    private Integer orgStatus;

    /**
     * 组织描述
     */
    private String orgDescription;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 组织地址
     */
    private String orgAddress;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 分页参数 - 页码
     */
    private Integer pageNum = 1;

    /**
     * 分页参数 - 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 父组织名称（用于显示）
     */
    private String parentOrgName;

    /**
     * 组织类型名称（用于显示）
     */
    private String orgTypeName;

    /** 当前会话允许访问的组织范围，仅供服务端查询条件使用。 */
    private List<String> accessibleOrgIds;
}
