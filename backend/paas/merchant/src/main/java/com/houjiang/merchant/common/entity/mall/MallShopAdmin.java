package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;

@Data
public class MallShopAdmin implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO) private Integer id;
    private Long createTime;
    private Long updateTime;
    private Long deleteTime;
    private Integer shopId;
    private Long userId;
    @TableField(value = "super") private Integer isSuper;
    private String name;
    private String face;
    private Integer status;
    private Integer loginAll;
    private Long loginLastTime;
    private String loginLastIp;
}
