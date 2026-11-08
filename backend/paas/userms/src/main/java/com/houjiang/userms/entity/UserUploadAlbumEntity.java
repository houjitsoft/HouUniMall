package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @ClassName UserUploadAlbumEntity
 * @Description 用户相册表
 * @Author Amos
 * @Date 2023/6/28 16:49
 * @Version 1.0
 * @author Amos
 */
@Data
@TableName(value = "hou_user_upload_album")
public class UserUploadAlbumEntity {

    /**
     * 商品图片Id
     */
    @TableId(type = IdType.AUTO)
    private Integer albumId;
    /**
     * 商品图片地址
     */
    private String albumName;
    /**
     * 封面
     */
    private String albumCover;
    /**
     * 描述
     */
    private String albumDesc;
    /**
     * 内容数量
     */
    private Integer albumNum;
    /**
     * 默认相册，1是，0否
     */
    private Byte albumIsDefault;
    /**
     * 排序
     */
    private Short albumDisplayorder;
    /**
     * 附件册
     */
    private String albumType;
    /**
     * 所属用户id
     */
    private String userId;
    private Integer shopId;


}
