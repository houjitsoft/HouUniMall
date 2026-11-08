package com.houjiang.commons.util;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;

/**
 * @author Amos
 * @date 2023/3/31 15:10
 */
public class TokenUtils {


    /**
     * 验证token是否为空
     * @param token
     * @return
     */
    public static ResponseResult checkToken(String token) {
        if (null != token && !"".equals(token)) {
            boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER  + token);
            if (isExist) {
                String userId = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USERID + token);
                if (null != userId) {
                    return ResponseResult.success(userId);
                }
            }
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL.getMsg());

    }
}
