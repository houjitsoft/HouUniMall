package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型实体
 * 对应表：hou_merchant_dict_type
 * 时间字段为 bigint 秒级时间戳，软删除使用 delete_time IS NULL 判定。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_dict_type")
public class SettingDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 字典名称 */
    private String name;

    /** 字典类型（编码） */
    private String type;

    /** 字典备注 */
    private String remark;

    /** 字典状态：0=停用，1=正常 */
    private Integer status;

    /** 创建时间（秒级时间戳） */
    private Long createTime;

    /** 更新时间（秒级时间戳） */
    private Long updateTime;

    /** 删除时间（秒级时间戳），NULL 表示未删除 */
    private Long deleteTime;
}
