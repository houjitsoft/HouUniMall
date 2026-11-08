package com.houjiang.base.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 协议（含当前生效版本正文）视图对象。
 *
 * <p>供注册页/登录页一次性拉取：协议定义 + 最新已发布版本正文，避免前端二次请求。</p>
 *
 * @author Amos
 */
@Data
public class AgreementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 协议定义ID */
    private Integer id;

    /** 协议编码 */
    private String code;

    /** 协议名称 */
    private String name;

    /** 适用场景 */
    private String scene;

    /** 是否必选：1=必选 0=可选 */
    private Integer required;

    /** 是否默认勾选 */
    private Integer defaultChecked;

    /** 展示排序 */
    private Integer sort;

    /** 协议定义状态：1=启用 0=停用 */
    private Integer status;

    /** 当前生效版本ID；为空表示该协议尚无已发布版本 */
    private Integer versionId;

    /** 当前生效版本号，如 v1.0.0 */
    private String versionNo;

    /** 当前生效版本标题 */
    private String title;

    /** 当前生效版本正文 */
    private String content;

    /** 当前生效版本变更说明 */
    private String changeNote;

    /** 当前生效版本生效时间 */
    private Date effectiveTime;
}
