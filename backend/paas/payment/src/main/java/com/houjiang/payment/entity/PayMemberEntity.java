package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付会员（企业/个人）开户记录。
 *
 * <p>为什么建这张表：渠道侧开户是外部副作用，调用成功就代表会员在渠道已经建立，
 * 本系统若不留痕就会出现「渠道有、本地无」的账实不符，后续审核回调也无法更新状态。
 * 早期实现把落库整段注释掉了，这里补回来。
 *
 * <p>{@code biz_user_id} 唯一：同一会员重复开户走更新而不是新增，避免产生重复档案。
 *
 * @author Amos
 */
@Data
@TableName("pay_member")
public class PayMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道会员编号（唯一） */
    private String bizUserId;

    /** 会员类型：ENTERPRISE / PERSON */
    private String memberType;

    /** 会员类别 */
    private String memCategory;

    /** 渠道号 */
    private String chanNo;

    /** 开户状态：0 处理中 1 成功 2 失败 */
    private Integer status;

    /** 渠道审核结果：2 成功 3 失败 */
    private String auditResult;

    /** 绑定手机号 */
    private String phone;

    /** 绑手机状态：0 未绑定 1 申请中 2 已绑定 3 失败 */
    private Integer bindPhoneStatus;

    /** 补充资料 JSON */
    private String ext;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
