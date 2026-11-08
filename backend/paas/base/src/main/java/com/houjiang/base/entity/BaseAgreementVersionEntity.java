package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 协议版本实体类：协议正文按版本存储，支持版本发布与下线。
 *
 * <p>新版本发布后，未同意该版本号的用户会被要求重新同意（scene=reagree）。</p>
 *
 * @author Amos
 */
@Data
@TableName("hou_base_agreement_version")
public class BaseAgreementVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 协议ID（hou_base_agreement.id）
     */
    private Integer agreementId;

    /**
     * 协议编码（冗余，便于查询）
     */
    private String agreementCode;

    /**
     * 版本号，格式 v1.0.0，同一协议内唯一
     */
    private String versionNo;

    /**
     * 版本标题（弹窗标题）
     */
    private String title;

    /**
     * 协议正文（纯文本或 HTML，前端原样渲染）
     */
    private String content;

    /**
     * 本次变更说明（弹窗顶部展示）
     */
    private String changeNote;

    /**
     * 生效时间
     */
    private Date effectiveTime;

    /**
     * 发布时间（NULL=草稿未发布）
     */
    private Date publishTime;

    /**
     * 状态：1=已发布 0=草稿 2=已下线
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
