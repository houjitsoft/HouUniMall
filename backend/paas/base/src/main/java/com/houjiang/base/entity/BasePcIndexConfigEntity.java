package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-05-24 09:39:22
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_pc_index_config")
@Data
public class BasePcIndexConfigEntity  implements Serializable {

    @TableId
    private Integer id;
    /**
     * 页面名称
     */
    private String pageName;
    /**
     * 页面类型 1 天天特价 2 特色专题  3 为你推荐 4家居馆 5单楼层 6多楼层（5、6状态在gdb_merchant_floor_adpage表）
     */
    private Integer pageType;
    /**
     * 页面标题
     */
    private String pageTitle;
    /**
     * 二级页面标题
     */
    private String pageTwoTitle;
    /**
     * 页面数据
     */
    private String pageData;
    /**
     * 页面状态 1 开启 2 关闭
     */
    private Integer pageStart;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 排序
     */
    private Integer pageSort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

