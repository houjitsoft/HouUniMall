package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName 
 * @Description  组件化  图片 视频空间
 * @Author Amos
 * @Date 2023-07-04 13:17:22
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_modularization_flie")
@Data
public class BaseModularizationFlieEntity  implements Serializable {
    
    private Integer id;
    /**
     * 文件名称 + 文件后缀
     */
    private String fileName;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 视频封面url
     */
    private String fileCoverUrl;
    /**
     * 文件类型  1 图片 2 视频
     */
    private Integer fileType;
    /**
     * 文件上传时间
     */
    private Date fileCreateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
