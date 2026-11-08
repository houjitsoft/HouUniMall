package com.houjiang.mall.dto.pay;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class MallPayCreateParam {
    private String mallOrderNo;
    private String payMethod;
    private String terminal;
    private String channelCode;
    private String openId;
    private String clientIp;
    private String returnUrl;
}
