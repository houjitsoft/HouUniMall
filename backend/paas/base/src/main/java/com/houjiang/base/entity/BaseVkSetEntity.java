package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
 /**
 * @ClassName
 * @Description  微客配置表
 * @Author Amos
 * @Date 2023-06-05 10:48:39
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_vk_set")
@Data
public class BaseVkSetEntity  implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 0 无业绩归属   1推广人分佣  2分享人分佣  3推广+分享
     */
    private Integer commissionConfig;
    /**
     * 一级推广佣金
     */
    private String oneTgRate;
    /**
     * 二级推广佣金
     */
    private String twoTgRate;
    /**
     * 分享佣金
     */
    private String fxRate;
    /**
     * 分销中心背景图
     */
    private String backgroundImage;
    /**
     * 售后天数
     */
    private Integer afterSalesDays;
    /**
     * 1开启 2关闭
     */
    private Integer vkState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
