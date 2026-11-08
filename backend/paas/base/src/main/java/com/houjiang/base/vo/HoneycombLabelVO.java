package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName HoneycombLabelVO
 * @Description 标签信息
 * @Author Amos
 * @Date 2023/6/16 15:56
 * @Version 1.0
 * @author Amos
 */
@Data
public class HoneycombLabelVO {

    /**
     * 标签id
     */
    private Integer labelId;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 热度值
     */
    private Integer labelHotNum;

    /**
     * 标签图片
     */
    private String labelImage;
}
