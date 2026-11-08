package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 协议定义实体类（注册/登录等场景下要求用户勾选的协议清单）。
 *
 * <p>与 {@link BaseProtocolEntity}（入驻协议，文件型、无版本）区分：
 * 本实体面向注册场景，正文存于 {@link BaseAgreementVersionEntity} 并带版本号。</p>
 *
 * @author Amos
 */
@Data
@TableName("hou_base_agreement")
public class BaseAgreementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 协议唯一编码：PRIVACY_POLICY / USER_SERVICE_AGREEMENT / COMPLIANCE_COMMITMENT
     */
    private String code;

    /**
     * 协议展示名称
     */
    private String name;

    /**
     * 适用场景：register=注册 / login=登录 / shop_join=入驻 / other=其他
     */
    private String scene;

    /**
     * 是否必选：1=必选（不勾选不允许提交）0=可选
     */
    private Integer required;

    /**
     * 是否默认勾选：合规要求一律 0，须用户主动勾选
     */
    private Integer defaultChecked;

    /**
     * 展示排序，小的在前
     */
    private Integer sort;

    /**
     * 状态：1=启用 0=停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
