package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
 /**
 * @ClassName
 * @Description  网站初始化内容分组表
 * @Author Amos
 * @Date 2023-05-22 18:54:37
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_article_group")
@Data
public class BaseArticleGroupEntity  implements Serializable {
    /**
     * ID
     */
    @TableId
    private Integer articleGroupId;
    /**
     * 标题
     */
    private String articleGroupTitle;
    /**
     * 排序
     */
    private Integer articleGroupSort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

