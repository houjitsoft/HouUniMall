package com.houjiang.plugin.artifact.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.Test;

/**
 * 身份解析单测：站点实例与用户 ID 只能来自登录态。
 *
 * @author Amos
 * @since M2
 */
class PluginIdentityResolverTest {

    @Test
    void instanceIdComesFromOrgId() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("1001");
        user.setOrgId("org-77");

        PluginIdentity identity = PluginIdentityResolver.from(user);

        assertEquals("org-77", identity.getInstanceId());
        assertEquals(1001L, identity.getUserId());
        assertTrue(identity.hasInstance());
    }

    @Test
    void fallsBackToUserScopedInstanceWhenOrgMissing() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("1001");
        user.setOrgId("   ");

        PluginIdentity identity = PluginIdentityResolver.from(user);

        assertEquals("u-1001", identity.getInstanceId(),
                "组织为空时退化到用户级实例，保证付费插件的授权查询不会退化成「不限实例」");
        assertTrue(identity.hasInstance());
    }

    @Test
    void nonNumericUserIdYieldsNullUserId() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("admin");
        user.setOrgId("org-77");

        PluginIdentity identity = PluginIdentityResolver.from(user);

        assertEquals("org-77", identity.getInstanceId());
        assertNull(identity.getUserId());
    }

    @Test
    void blankEverythingYieldsEmptyInstance() {
        PluginIdentity identity = PluginIdentityResolver.from(new UserInfoVO());

        assertEquals("", identity.getInstanceId());
        assertNull(identity.getUserId());
    }

    @Test
    void nullUserRejected() {
        assertThrows(IllegalArgumentException.class, () -> PluginIdentityResolver.from(null));
    }
}
