package com.houjiang.mall.dto.auth;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Mall 登录响应（对齐 like-admin SystemLoginVo：token + 用户信息）
 * @author Amos
 */
@Data
public class MallAuthLoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    /** 兼容 mall/admin 前端读取 adminInfo */
    private Map<String, Object> adminInfo;

    /** 对齐 like-admin 部分前端读取 user 字段 */
    private Map<String, Object> user;
}
