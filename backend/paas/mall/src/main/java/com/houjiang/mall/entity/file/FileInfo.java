package com.houjiang.mall.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * 对应数据库表：hou_mall_file
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_file")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 分类 ID
     */
    private Long cateId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
