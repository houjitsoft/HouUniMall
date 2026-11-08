package com.houjiang.job.vo;

import lombok.Data;

/**
 * 数据大屏搜索热门词
 * @TableName ${database.prefix.datav}hou_datav_search_word
 * @author Amos
 */

@Data
public class DcenterSearchWordVO{
    /**
     * 
     */

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


}