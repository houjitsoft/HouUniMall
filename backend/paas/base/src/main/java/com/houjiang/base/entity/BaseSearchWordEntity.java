package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 搜索热门词(BaseSearchWord)
 *
 * @since 2023-05-10 14:00:51
 * @author Amos
 */
@TableName("hou_base_search_word")
@Data
public class BaseSearchWordEntity implements Serializable {


    /**
     * 搜索id
     */
    @TableId
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

