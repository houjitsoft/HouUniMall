package com.houjiang.mall.entity.album;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
@TableName("hou_merchant_file")
public class MallAlbumFile implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer cid;

    private Integer sourceId;

    private Integer source;

    private Integer type;

    private String name;

    private String uri;

    private Long createTime;

    private Long updateTime;

    private Long deleteTime;
}
