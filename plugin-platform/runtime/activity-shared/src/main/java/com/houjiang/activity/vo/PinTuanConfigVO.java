package com.houjiang.activity.vo;


import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PinTuanConfigVO {
    /**
     *
     */
    private Integer id;
    /**
     *1 代表模块1 （不可新增 删除）  2 代表 模块 2（可新增 删除）
     */
    private String configName;
    /**
     *一级标题
     */
    private String configTitleOne;
    /**
     *二级标题
     */
    private String configTitleTwo;
    /**
     *页面数据
     */
    private String configData;
    /**
     *拼团信息
     */
    private List<GoodsPinTuanResultVO> goodsPinTuanResultList;


}
