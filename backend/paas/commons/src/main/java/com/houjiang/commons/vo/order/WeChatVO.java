package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * @ClassName WeChatVO2
 * @Description 微信响应参数
 * @Author Amos
 * @Date 2024/5/9 15:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class WeChatVO {

    /**
     * 错误代码
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

}

