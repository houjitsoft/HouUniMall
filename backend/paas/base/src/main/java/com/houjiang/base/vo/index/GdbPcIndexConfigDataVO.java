package com.houjiang.base.vo.index;

import lombok.Data;

import java.util.List;
/**
 * @ClassName GdbPcIndexConfigVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbPcIndexConfigDataVO {
    /**
     * 类型
     */
    private Integer type;
    /**
     * id
     */
    private List<Integer> data;
    /**
     * 图片地址
     */
    private String images;


}
