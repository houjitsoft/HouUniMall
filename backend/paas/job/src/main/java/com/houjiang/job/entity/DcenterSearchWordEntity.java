package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏搜索热门词
 * @TableName ${database.prefix.datav}hou_datav_search_word
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_search_word")
@Data
public class DcenterSearchWordEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer searchId;

    /**
     * key值
     */
    private String searchKeyword;

    /**
     * 下标
     */
    private String searchCharIndex;

    /**
     * 数量
     */
    private Integer searchNums;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}