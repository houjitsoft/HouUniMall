package com.houjiang.commons.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关联板式表
 *
 * @author Amos
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseFormatVO  {

    private Integer id;

    /**
     * 版式名称
     */
    private String name;

    /**
     * 位置
     */
    private Boolean position;

    /**
     * 内容
     */
    private String content;

    /**
     * 店铺ID
     */
    private Integer shopId;


}