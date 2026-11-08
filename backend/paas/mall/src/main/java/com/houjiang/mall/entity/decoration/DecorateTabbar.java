package com.houjiang.mall.entity.decoration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 底部导航配置实体类
 * 对应数据库表：hou_mall_decorate_tabbar
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_decorate_tabbar")
public class DecorateTabbar implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID（NULL表示平台级）
     */
    private Long shopId;

    /**
     * 底部导航配置JSON
     */
    private String content;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
