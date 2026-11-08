package com.houjiang.payment.dto;

import lombok.Data;

/**
 * 返回卡信息
 * @author Amos
 */
@Data
public class ReturnCardBaseInfoDTO
{

    /**
     * 卡号
     */
    public String cardId;

    /**
     * 添加时间
     */
    public String returnAddTimeStart;

    /**
     * 添加时间
     */
    public String returnAddTimeEnd;

    /**
     * 是否
     */
    public String whether;

    /**
     * 卡片激活码
     */
    public String cardCode;

    /**
     * 开始
     */
    public Integer page;

    /**
     * 限度
     */
    public Integer limit;

    /**
     * token
     */
    public String token;

}
