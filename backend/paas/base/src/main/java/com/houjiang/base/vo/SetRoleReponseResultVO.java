package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName SetRoleReponseResultVO
 * @Description
 * @Author Amos
 * @Date 2023/7/24 14:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class SetRoleReponseResultVO {

    /**
     * 消息
     */
    private String msg;

    /**
     * 二维码链接
     */
    private String codeurl;

    /**
     * 错误代码
     */
    private Integer errcode;
}
