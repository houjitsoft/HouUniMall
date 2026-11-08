package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商城后台 - 通用键值配置实体。
 * 对应表 hou_merchant_config，结构为 (id, type, name, value, create_time, update_time)。
 * type 表示配置分组（如 storage/website/login/order/user...），
 * name 表示分组内的键，value 为字符串值（可存 JSON）。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_config")
public class SettingConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 类型分组 */
    private String type;

    /** 键名 */
    private String name;

    /** 值（可存 JSON 字符串） */
    private String value;

    private Long createTime;
    private Long updateTime;
}
