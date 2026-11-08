package com.houjiang.mall.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户标签实体类
 * 表名: hou_mall_customer_tag
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_customer_tag")
public class CustomerTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签颜色 */
    private String color;

    /** 标签说明 */
    private String description;

    /** 排序 */
    private Integer sort;

    /** 客户数量 */
    private Integer customerCount;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
