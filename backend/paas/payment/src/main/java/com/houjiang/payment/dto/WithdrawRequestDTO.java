package com.houjiang.payment.dto;

import lombok.ToString;

/**
 * @author Amos
 * @date 2021-01-0516:03
 * @Description: 提现请求参数
 **/
@ToString
public class WithdrawRequestDTO extends FXCommonDTO {
    public static final String ALI_CONFIG_KEY = "game_group_ali_withdraw";
    public static final String WX_CONFIG_KEY = "game_group_wx_withdraw";
}
