package com.houjiang.activity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class HouVoucherTemplateResultVO {
    /**
     * 代金券模版集合
     */
    List<HouVoucherTemplateVO> list;
    /**
     * 总数
     */
    Integer totalCount;
}
