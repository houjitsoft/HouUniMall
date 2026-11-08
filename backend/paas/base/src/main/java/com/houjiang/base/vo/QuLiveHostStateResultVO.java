package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName QuLiveHostStateResultVO
 * @Description
 * @Author Amos
 * @Date 2023/7/24 14:36
 * @Version 1.0
 * @author Amos
 */
@Data
public class QuLiveHostStateResultVO {

    /**
     * 微信号
     */
    private String wechatId;

    /**
     * 状态  1 通过  2 审核中 3 未通过 4 待平台审核 5 带微信审核
     */
    private Integer state;

    /**
     * 是否停播 1 是 2否
     */
    private Integer hoststate;
}
