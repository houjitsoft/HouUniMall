package com.houjiang.userms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人群包保存条件：圈人条件 + 名称与备注
 *
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PortraitCrowdDTO extends PortraitSegmentDTO {

    /** 人群包名称 */
    private String crowdName;

    /** 备注 / 运营目的 */
    private String remark;

    /** 创建人 user_id（由后端从会话或入参补齐） */
    private Long createBy;
}
