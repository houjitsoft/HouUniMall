package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
 /**
 * @ClassName
 * @Description  页面导航表
 * @Author Amos
 * @Date 2023-05-24 09:33:08
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_system_platform_nav")
@Data
public class BaseSystemPlatformNavEntity  implements Serializable {
    /**
     * 索引ID
     */
    @TableId
    private Integer navId;
    /**
     * 导航标题
     */
    private String navTitle;
    /**
     * 导航链接
     */
    private String navUrl;
    /**
     * 是否以新窗口打开，0为否，1为是，默认为0
     */
    private Integer navNewOpen;
    /**
     * 排序
     */
    private Integer navDisplayorder;
    /**
     * 是否启用 0 否  1是
     */
    private Integer navActive;
    /**
     * 1商城 2批发市场
     */
    private Integer navProperty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

