package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BaseOssConfigEntity
 * @Description OSS表
 * @Author Amos
 * @Date 2023/6/21 16:45
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_oss_config")
@Data
public class BaseOssConfigEntity {

    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer ossId;

    /**
     * API的bucket名称
     */
    @TableField("oss_backetName")
    private String ossBacketName;

    /**
     * API的文件夹名称
     */
    private String ossFolder;

    /**
     * 服务器集群地址
     */
    private String ossRegion;

    /**
     * 域名
     */
    private String ossEndpoint;

    /**
     * 密钥
     */
    @TableField("oss_accessKeyId")
    private String ossAccessKeyId;

    /**
     * 私钥
     */
    @TableField("oss_accessKeySecret")
    private String ossAccessKeySecret;

    /**
     * 状态值 00禁用  01启用，默认启用
     */
    private String ossStates;

    /**
     * 伪删除标识 00删除 01正常，默认删除
     */
    @TableField("oss_isActive")
    private String ossIsActive;

    /**
     * 创建时间
     */
    @TableField("oss_createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ossCreatedDate;

    /**
     * 修改时间
     */
    @TableField("oss_updateDate")
    private String ossUpdateDate;

    /**
     * 配置的备注信息
     */
    private String ossRemark;

    /**
     * oss类型 默认 aliyun
     */
    private String ossType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
