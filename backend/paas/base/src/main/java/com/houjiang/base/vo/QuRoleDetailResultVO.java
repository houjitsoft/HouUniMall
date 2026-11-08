package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName QuRoleDetailResultVO
 * @Description
 * @Author Amos
 * @Date 2023/7/24 14:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class QuRoleDetailResultVO {

    /**
     * 用户的openid
     */
    private String openid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户名
     */
    private String username;
}
