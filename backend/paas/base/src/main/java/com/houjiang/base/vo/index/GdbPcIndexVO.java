package com.houjiang.base.vo.index;

import lombok.Data;

import java.util.List;

/**
 * 1 天天特价 2 特色专题  3 为你推荐 4家居馆 5单楼层 6多楼层 pageType
 * 返回pc首页装修 对象
 * @author Amos
 */
@Data
public class GdbPcIndexVO {
    /**
     * page_sort 排序
     */
    private Integer pageSort;

    /**
     * 数据模块类型
     */
    private Integer pageType;

    /**
     * 楼层id  pageType 为 5 时 会有值
     */
    private Integer floorAdpageId;

    /**
     *  存放每个模块的数据
     */
    private List data;

    /**
     *   存放会员折扣-plus折扣
     */
    private String memberDiscount;

    /**
     *   存放plus会员
     */
    private Integer plusMember;
}
