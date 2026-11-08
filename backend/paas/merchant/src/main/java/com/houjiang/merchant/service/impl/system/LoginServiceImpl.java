package com.houjiang.merchant.service.impl.system;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.enums.ShopAdminEum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.util.IpUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.system.ILoginService;
import com.houjiang.merchant.validate.system.LoginsValidate;
import com.houjiang.merchant.vo.mall.MallShopAdminVo;
import com.houjiang.merchant.vo.system.LoginVo;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Merchant login backed by hou_user_info.
 */
@Service
public class LoginServiceImpl implements ILoginService {

    @Resource
    private MallShopAdminMapper mallShopAdminMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private RedisCache redisCache;

    @Override
    @Transactional
    public LoginVo login(LoginsValidate loginsValidate) {
        String identifier = loginsValidate.getUsername() == null
                ? "" : loginsValidate.getUsername().trim();
        Map<String, Object> account = jdbcTemplate.queryForList(
                        "SELECT a.id, a.user_id AS userId, u.password AS userPassword, "
                                + "u.user_mobile AS userMobile, u.user_name AS userName, "
                                + "u.user_state AS userState, u.is_delete AS userDeleted "
                                + "FROM hou_merchant_mall_shop_admin a "
                                + "JOIN hou_user_info u ON u.user_id = a.user_id "
                                + "WHERE a.delete_time = 0 AND a.status = 1 "
                                + "AND COALESCE(u.is_delete, 0) = 0 "
                                + "AND (u.user_name = ? OR u.user_mobile = ?) "
                                + "ORDER BY a.id DESC LIMIT 1",
                        identifier, identifier)
                .stream().findFirst().orElse(null);
        if (account == null) {
            throw new OperateException("账号错误");
        }

        MallShopAdmin shopAdmin = mallShopAdminMapper.selectById(
                ((Number) account.get("id")).intValue());
        if (shopAdmin == null || shopAdmin.getStatus() == null
                || shopAdmin.getStatus() != ShopAdminEum.STATUS_NORMAL.getCode()) {
            throw new OperateException("账号已被禁用");
        }

        if (!isActiveMember(account.get("userState"), account.get("userDeleted"))) {
            throw new OperateException("会员账号已停用");
        }
        String storedPassword = account.get("userPassword") == null
                ? null : account.get("userPassword").toString();
        if (!matchesPassword(loginsValidate.getPassword(), storedPassword)) {
            throw new OperateException("账号密码错误");
        }

        String loginIp = IpUtils.getIpAddress();
        shopAdmin.setLoginLastIp(loginIp);
        shopAdmin.setLoginLastTime(System.currentTimeMillis() / 1000);
        mallShopAdminMapper.updateById(shopAdmin);
        jdbcTemplate.update("UPDATE hou_user_info SET user_lastlogin_time = NOW(), "
                        + "user_lastlogin_ip = ?, user_count_login = COALESCE(user_count_login, 0) + 1 "
                        + "WHERE user_id = ?",
                loginIp, shopAdmin.getUserId());

        String token = JwtUtil.createJWT(String.valueOf(shopAdmin.getId()));
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(String.valueOf(shopAdmin.getId()));
        userInfo.setUserName(preferredLoginAccount(account));
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token,
                JSON.toJSONString(userInfo), 86400, TimeUnit.SECONDS);

        MallShopAdminVo shopAdminVo = new MallShopAdminVo();
        shopAdminVo.setId(shopAdmin.getId());
        shopAdminVo.setUserId(shopAdmin.getUserId());
        shopAdminVo.setName(shopAdmin.getName());
        shopAdminVo.setLoginAccount(preferredLoginAccount(account));
        shopAdminVo.setFace(UrlUtils.toShopAbsoluteUrl(shopAdmin.getFace()));
        shopAdminVo.setShopId(shopAdmin.getShopId());
        shopAdminVo.setIsSuper(shopAdmin.getIsSuper());
        shopAdminVo.setCreateTime(TimeUtils.timestampToDate(shopAdmin.getCreateTime()));

        LoginVo loginVo = new LoginVo();
        loginVo.setShopToken(token);
        loginVo.setShopAdminVo(shopAdminVo);
        return loginVo;
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) return false;
        if (storedPassword.startsWith("$2")) {
            return Boolean.TRUE.equals(PasswordUtils.BCryptPasswordMatches(rawPassword, storedPassword));
        }
        return ToolUtils.makePassword(rawPassword).equals(storedPassword);
    }

    private boolean isActiveMember(Object state, Object deleted) {
        return !(deleted instanceof Number && ((Number) deleted).intValue() != 0)
                && state instanceof Number
                && (((Number) state).intValue() == 1 || ((Number) state).intValue() == 2);
    }

    private String preferredLoginAccount(Map<String, Object> account) {
        Object mobile = account.get("userMobile");
        if (mobile != null && !mobile.toString().isBlank()) return mobile.toString();
        Object username = account.get("userName");
        return username == null ? "" : username.toString();
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) return;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + token);
    }
}
