package com.houjiang.permission.security;

import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionIdentityTest {

    private final PermissionIdentity identity = new PermissionIdentity();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectAnonymousIdentity() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                identity::currentUserId);

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void shouldUseAuthenticatedUserWhenRequestOmitsUserId() {
        authenticate("790708000000000001");

        assertEquals(790708000000000001L, identity.requireSelf(null));
    }

    @Test
    void shouldAcceptMatchingStringOrNumericUserId() {
        authenticate("790708000000000001");

        assertEquals(790708000000000001L, identity.requireSelf("790708000000000001"));
        assertEquals(790708000000000001L, identity.requireSelf(790708000000000001L));
    }

    @Test
    void shouldRejectDifferentRequestedUserId() {
        authenticate("790708000000000001");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> identity.requireSelf("640697115455213568"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void shouldRejectMalformedRequestedUserIdAsBadRequest() {
        authenticate("790708000000000001");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> identity.requireSelf("not-a-user-id"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectBlankRequestedUserIdAsBadRequest() {
        authenticate("790708000000000001");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> identity.requireSelf("   "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private void authenticate(String userId) {
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));
    }
}
