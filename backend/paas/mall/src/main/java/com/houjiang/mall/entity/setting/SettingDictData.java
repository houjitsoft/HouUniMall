package com.houjiang.mall.entity.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据实体（对应 hou_merchant_dict_data）。
 *
 * 时间字段为 bigint 秒级时间戳，软删除使用 delete_time。
 *
 * @author Amos
 */
@Data
@TableName("hou_merchant_dict_data")
public class SettingDictData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 类型ID，关联 hou_merchant_dict_type.id */
    private Integer typeId;

    /** 类型值（冗余字段，存储所属字典类型的 type 值） */
    private String typeValue;

    /** 字典名称（键） */
    private String name;

    /** 字典值 */
    private String value;

    /** 备注 */
    private String remark;

    /** 排序号 */
    private Integer sort;

    /** 状态：0=停用，1=正常 */
    private Integer status;

    /** 创建时间（秒级时间戳） */
    private Long createTime;

    /** 更新时间（秒级时间戳） */
    private Long updateTime;

    /** 删除时间（秒级时间戳，NULL 表示未删除） */
    private Long deleteTime;
}
