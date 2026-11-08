package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 微客配置表(UserVkSet)表实体类
 *
 * @author Amos
 * @since 2024-07-10 11:35:33
 */
@Data
@TableName("hou_user_vk_set")
public class UserVkSetEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 顶部标题
     */
    private String vkTitle;

    /**
     * 顶部图片
     */
    private String vkImage;

    /**
     * 申请条件  1注册成功  2注册成功+购买礼包商品
     */
    private Integer applyType;

    /**
     * 海报图片
     */
    private String posterImage;

    /**
     * 是否开启 1开启 2关闭
     */
    private Integer vkState;

    /**
     * 微客须知
     */
    private String vkNotice;

    /**
     * 固定奖励
     */
    private BigDecimal fixedRewards;

}
