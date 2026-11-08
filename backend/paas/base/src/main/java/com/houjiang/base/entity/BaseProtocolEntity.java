package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 协议管理实体类
 * @author Amos
 */
@Data
@TableName("hou_base_protocol")
public class BaseProtocolEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 协议名称
     */
    private String name;
    
    /**
     * 协议类型：PERSONAL-个人入驻，ENTERPRISE-企业入驻，SUPPLIER-供应商入驻，COOPERATION-合作协议
     */
    private String type;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
