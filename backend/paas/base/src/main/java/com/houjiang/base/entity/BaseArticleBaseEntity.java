package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  网站初始化内容设置
 * @Author Amos
 * @Date 2023-05-22 18:54:37
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_article_base")
@Data
public class BaseArticleBaseEntity  implements Serializable {
    /**
     * ID
     */
    @TableId
    private Integer articleId;
    /**
     * 描述
     */
    private String articleDesc;
    /**
     * 标题
     */
    private String articleTitle;
    /**
     * 调用网址-url，默认为本页面构造的网址，可填写其它页面
     */
    private String articleUrl;
    /**
     * 组
     */
    private Integer articleGroupId;
    /**
     * 排序
     */
    private Integer articleSort;
    /**
     * 状态 1:启用  2:关闭
     */
    private Integer articleStatus;
    /**
     * 添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date articleAddTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

