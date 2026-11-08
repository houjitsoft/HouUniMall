package com.houjiang.commons.model;

import lombok.Data;

import java.util.List;

/**
 * 删除表单
 * @author Amos
 */
@Data
public class RemoveForm {
    
    /**
     * 要删除的ID列表
     */
    private List<Long> idList;
}
