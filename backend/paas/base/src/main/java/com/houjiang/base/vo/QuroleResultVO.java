package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName QuroleResultVO
 * @Description
 * @Author Amos
 * @Date 2023/7/24 14:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class QuroleResultVO {

    /**
     * 错误信息
     */
    private Integer errcode;

    /**
     * 数据集合
     */
    private List<QuRoleDetailResultVO> list;
}
