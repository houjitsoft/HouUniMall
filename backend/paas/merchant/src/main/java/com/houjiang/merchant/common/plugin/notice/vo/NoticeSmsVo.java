package com.houjiang.merchant.common.plugin.notice.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Amos
 */
@Data
@Accessors(chain = true)
public class NoticeSmsVo {

    private Integer scene;
    private String mobile;
    private Integer expire;
    private String[] params;

    private String code;

}
