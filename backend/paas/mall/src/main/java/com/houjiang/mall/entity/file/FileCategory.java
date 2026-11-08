package com.houjiang.mall.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分类实体类
 * 对应数据库表：hou_mall_file_category
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_file_category")
public class FileCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类 ID
     */
    @TableId(value = "cate_id", type = IdType.AUTO)
    private Long cateId;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 分类名称
     */
    private String cateName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
