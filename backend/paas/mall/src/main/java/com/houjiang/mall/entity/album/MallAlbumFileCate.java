package com.houjiang.mall.entity.album;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 素材分类（与 like-admin / merchant 共用 hou_merchant_file_cate）
 * @author Amos
 */
@Data
@TableName("hou_merchant_file_cate")
public class MallAlbumFileCate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer pid;

    /** 10=图片 20=视频 */
    private Integer type;

    private String name;

    /** 0=后台 1=用户 2=商家 */
    private Integer source;

    private Integer sourceId;

    private Long createTime;

    private Long updateTime;

    private Long deleteTime;
}
