package com.houjiang.mall.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户跟进记录实体类
 * 表名: hou_mall_customer_follow
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_customer_follow")
public class CustomerFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户ID */
    private Long customerId;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 跟进类型: 1-电话 2-微信 3-拜访 4-其他 */
    private Integer type;

    /** 跟进内容 */
    private String content;

    /** 下次跟进时间 */
    private LocalDateTime nextFollowTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
