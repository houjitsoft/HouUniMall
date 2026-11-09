package com.houjiang.userms.dto;

import lombok.Data;

import java.util.List;

/**
 * 标签相关操作入参（新建 / 启停 / 打标 / 取消打标）
 *
 * @author Amos
 */
@Data
public class PortraitTagDTO {

    /** 标签ID（打标、启停、删除时必填） */
    private Integer tagId;

    /** 标签名称（新建时必填） */
    private String tagName;

    /** 权重，越大越靠前，默认 0 */
    private Integer tagWeight;

    /** 显示颜色，#RRGGBB；不传时保留原颜色 */
    private String tagColor;

    /** 标签说明 */
    private String tagDescribe;

    /** 启用状态：1 启用 / 0 停用 */
    private Integer tagIsactive;

    /** 批量打标 / 取消打标的会员ID */
    private List<Long> userIds;
}
