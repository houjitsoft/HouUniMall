package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName UserUploadBaseEntity
 * @Description 用户附件表-图片、视频
 * @Author Amos
 * @Date 2023/6/29 9:16
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_user_upload_base")
@Data
public class UserUploadBaseEntity {

    /**
     * 商品图片Id
     */
    @TableId(type = IdType.AUTO)
    private Integer uploadId;
    private Integer albumId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 店铺id
     */
    private Integer shopId;
    private String uploadUrlPrefix;
    private String uploadPath;
    /**
     * image|video|
     */
    private String uploadType;

    /**
     * 原文件大小
     */
    private Integer uploadSize;

    /**
     * 附件标题
     */
    private String uploadName;
    /**
     * 附件日期
     */
    private Integer uploadTime;


}
