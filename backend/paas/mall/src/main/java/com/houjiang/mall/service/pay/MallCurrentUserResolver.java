package com.houjiang.mall.service.pay;

import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * @author Amos
 */
@Component
public class MallCurrentUserResolver {

    public String resolve(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) token = request.getHeader("token");
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        UserInfoVO user = ParseTokenUtils.getUserInfoByToken(token);
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new DefinedException("登录凭证无效");
        }
        return user.getUserId();
    }
}
