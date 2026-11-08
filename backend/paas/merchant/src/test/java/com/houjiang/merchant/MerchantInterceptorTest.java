package com.houjiang.merchant;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopMenu;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.exception.LoginException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMenuMapper;
import com.houjiang.merchant.common.util.YmlUtils;
import com.houjiang.merchant.config.WebMvcConfig;
import com.houjiang.merchant.controller.ConfigController;
import com.houjiang.merchant.controller.IndexController;
import com.houjiang.merchant.controller.LoginController;
import com.houjiang.merchant.controller.UploadController;
import com.houjiang.merchant.controller.UserController;
import com.houjiang.merchant.controller.announcement.AnnouncementController;
import com.houjiang.merchant.controller.area.AreaController;
import com.houjiang.merchant.controller.channel.ChannelOaCallBackController;
import com.houjiang.merchant.controller.decorate.DecoratePagesController;
import com.houjiang.merchant.controller.mall.FileController;
import com.houjiang.merchant.controller.mall.MallGoodsController;
import com.houjiang.merchant.controller.mall.MallShopGoodsCategoryController;
import com.houjiang.merchant.controller.system.SystemAuthMenuController;
import com.houjiang.merchant.service.mall.IMallShopAdminMenuService;
import com.houjiang.merchant.service.mall.IMallShopAdminRoleService;
import com.houjiang.merchant.service.mall.IMallShopService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RestController
class MerchantInterceptorTest {

    private static final String GOODS_PATH = "/merchant/mall.Goods/lists";
    private static final String ADMIN_MENU_PATH = "/adminapi/auth.menu/delete";
    private static final String DECORATE_SAVE_PATH = "/adminapi/decorate.page/save";

    @Mock
    private MallShopAdminMapper mallShopAdminMapper;
    @Mock
    private MallShopMenuMapper mallShopMenuMapper;
    @Mock
    private IMallShopAdminRoleService mallShopAdminRoleService;
    @Mock
    private IMallShopService mallShopService;
    @Mock
    private IMallShopAdminMenuService mallShopAdminMenuService;
    @Mock
    private Environment environment;

    @InjectMocks
    private MerchantInterceptor interceptor;

    private HandlerMethod fallbackHandler;
    private Environment originalEnvironment;

    @BeforeEach
    void setUp() throws Exception {
        originalEnvironment = (Environment) ReflectionTestUtils.getField(YmlUtils.class, "env");
        new YmlUtils().setEnvironment(environment);
        lenient().when(mallShopAdminRoleService.getRoleIdAttr(any())).thenReturn(List.of());
        lenient().when(mallShopService.getNameById(any())).thenReturn("Test shop");
        fallbackHandler = handler(new UnmappedHandler(), "handle");
    }

    @AfterEach
    void tearDown() {
        MerchantThreadLocal.remove();
        SecurityContextHolder.clearContext();
        new YmlUtils().setEnvironment(originalEnvironment);
    }

    @Test
    void webMvcRegistersBothProtectedNamespaces() {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(config, "merchantInterceptor", interceptor);
        TestInterceptorRegistry registry = new TestInterceptorRegistry();

        config.addInterceptors(registry);

        MappedInterceptor mapped = assertInstanceOf(
                MappedInterceptor.class, registry.registered().getFirst());
        assertArrayEquals(
                new String[]{"/merchant/**", "/adminapi/**"},
                mapped.getIncludePathPatterns());
    }

    @Test
    void seededCombinedMenuTokensCoverOnlyTheirDeclaredActions() {
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "mall.Goods/add"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "mall.Goods/edit"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "mall.Goods/delete"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "mall.Goods/status"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "other.Goods/add"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", "mall.goods/add"));

        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.expressTpl/add:edit", "mall.expressTpl/add"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.expressTpl/add:edit", "mall.expressTpl/edit"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.expressTpl/add:edit", "mall.expressTpl/delete"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "printer.printer/add:edit", "printer.printer/add"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "printer.printer/add:edit", "printer.printer/edit"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "printer.printer/add:edit", "printer.printer/delete"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "decorate.page/save", "decorate.page/save"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "decorate.page/save", "decorate.page/detail"));

        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess("", "mall.Goods/add"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/", "mall.Goods/add"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add::edit", "mall.Goods/add"));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "mall.Goods/add:edit", ""));
        assertFalse(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                null, "mall.Goods/add"));
    }

    @Test
    void rejectsControlledMerchantRouteWithoutMatchingAccess() {
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/detail"));
        MerchantThreadLocal.put("adminId", 9);

        LoginException exception = assertThrows(
                LoginException.class, () -> interceptor.checkAuth(GOODS_PATH));

        assertNoPermission(exception);
    }

    @Test
    void allowsControlledMerchantRouteWithExactAccess() {
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/lists"));
        MerchantThreadLocal.put("adminId", 9);

        assertDoesNotThrow(() -> interceptor.checkAuth(GOODS_PATH));

        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void allowsControlledMerchantRouteWithWildcardAccess() {
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(List.of("*"));
        MerchantThreadLocal.put("adminId", 9);

        assertDoesNotThrow(() -> interceptor.checkAuth(GOODS_PATH));

        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void unconfiguredMerchantRouteFailsClosed() {
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());
        MerchantThreadLocal.put("adminId", 9);

        LoginException exception = assertThrows(
                LoginException.class,
                () -> interceptor.checkAuth("/merchant/public/ping"));

        assertNoPermission(exception);
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void unconfiguredGoodsDeleteAndStatusRoutesFailClosed() {
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());
        MerchantThreadLocal.put("adminId", 9);

        assertEquals(
                "/merchant/mall.Goods/delete",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        handler(new MallGoodsController(), "del")));
        assertEquals(
                "/merchant/mall.Goods/status",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        handler(new MallGoodsController(), "status")));
        assertNoPermission(assertThrows(
                LoginException.class,
                () -> interceptor.checkAuth("/merchant/mall.Goods/delete")));
        assertNoPermission(assertThrows(
                LoginException.class,
                () -> interceptor.checkAuth("/merchant/mall.Goods/status")));

        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void goodsControllerCanonicalRoutesHonorSeededCombinedToken() {
        String addAccess = ReflectionTestUtils.invokeMethod(
                interceptor, "resolveCanonicalRoute",
                handler(new MallGoodsController(), "add"));
        String editAccess = ReflectionTestUtils.invokeMethod(
                interceptor, "resolveCanonicalRoute",
                handler(new MallGoodsController(), "edit"));
        String deleteAccess = ReflectionTestUtils.invokeMethod(
                interceptor, "resolveCanonicalRoute",
                handler(new MallGoodsController(), "del"));
        String statusAccess = ReflectionTestUtils.invokeMethod(
                interceptor, "resolveCanonicalRoute",
                handler(new MallGoodsController(), "status"));
        assertEquals("/merchant/mall.Goods/add", addAccess);
        assertEquals("/merchant/mall.Goods/edit", editAccess);
        assertEquals("/merchant/mall.Goods/delete", deleteAccess);
        assertEquals("/merchant/mall.Goods/status", statusAccess);

        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/add:edit")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/add:edit"));
        MerchantThreadLocal.put("adminId", 9);

        assertDoesNotThrow(() -> interceptor.checkAuth(addAccess));
        assertDoesNotThrow(() -> interceptor.checkAuth(editAccess));
        assertNoPermission(assertThrows(
                LoginException.class, () -> interceptor.checkAuth(deleteAccess)));
        assertNoPermission(assertThrows(
                LoginException.class, () -> interceptor.checkAuth(statusAccess)));

        verify(mallShopMenuMapper, times(4)).selectList(any());
        verify(mallShopAdminMenuService, times(2)).getAdminAccessList(9);
    }

    @Test
    void authorizationAllowsAnyGrantedTokenThatCoversTheCanonicalAccess() {
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of(
                enabledMenu("mall.Goods/add:edit"),
                enabledMenu("mall.Goods/add")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/add"));
        MerchantThreadLocal.put("adminId", 9);

        assertDoesNotThrow(() -> interceptor.checkAuth("/merchant/mall.Goods/add"));
    }

    @Test
    void authorizationIsRecheckedForEveryAdministrator() {
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/lists"));
        when(mallShopAdminMenuService.getAdminAccessList(10))
                .thenReturn(List.of("mall.Goods/detail"));

        MerchantThreadLocal.put("adminId", 9);
        assertDoesNotThrow(() -> interceptor.checkAuth(GOODS_PATH));
        MerchantThreadLocal.put("adminId", 10);

        assertThrows(LoginException.class, () -> interceptor.checkAuth(GOODS_PATH));
        verify(mallShopMenuMapper, times(2)).selectList(any());
        verify(mallShopAdminMenuService).getAdminAccessList(9);
        verify(mallShopAdminMenuService).getAdminAccessList(10);
    }

    @Test
    void menuConfigurationChangesTakeEffectOnTheNextRequest() {
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(), List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/lists"));
        MerchantThreadLocal.put("adminId", 9);

        LoginException exception = assertThrows(
                LoginException.class, () -> interceptor.checkAuth(GOODS_PATH));
        assertDoesNotThrow(() -> interceptor.checkAuth(GOODS_PATH));

        assertNoPermission(exception);
        verify(mallShopMenuMapper, times(2)).selectList(any());
    }

    @Test
    void unconfiguredAdminapiRouteFailsClosed() {
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());
        MerchantThreadLocal.put("adminId", 9);

        LoginException exception = assertThrows(
                LoginException.class, () -> interceptor.checkAuth(ADMIN_MENU_PATH));

        assertNoPermission(exception);
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void adminapiRouteAllowsExactSeededAccess() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("decorate.page/save")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("decorate.page/save"));

        boolean allowed = interceptor.preHandle(
                request("POST", DECORATE_SAVE_PATH), new MockHttpServletResponse(),
                handler(new DecoratePagesController(), "save"));

        assertTrue(allowed);
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void adminapiRouteRejectsWrongAccess() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("decorate.page/save")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("decorate.page/detail"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request("POST", DECORATE_SAVE_PATH), response,
                handler(new DecoratePagesController(), "save"));

        assertFalse(allowed);
        assertTrue(response.getContentAsString().contains("无权限访问该功能"));
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void shopSuperAdministratorWildcardAllowsSeededAdminapiRoute() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 1));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("decorate.page/save")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(List.of("*"));

        boolean allowed = interceptor.preHandle(
                request("POST", DECORATE_SAVE_PATH), new MockHttpServletResponse(),
                handler(new DecoratePagesController(), "save"));

        assertTrue(allowed);
        verify(mallShopAdminMenuService).getAdminAccessList(9);
        assertEquals(9, MerchantThreadLocal.getAdminId());
    }

    @Test
    void shopSuperAdministratorWildcardCannotAccessUnseededAdminapiRoute() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 1));
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());

        boolean allowed = interceptor.preHandle(
                request("POST", ADMIN_MENU_PATH), new MockHttpServletResponse(),
                handler(new SystemAuthMenuController(), "del"));

        assertFalse(allowed);
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void shopSuperAdministratorWildcardCannotAccessUnseededMerchantRoute() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 1));
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());

        boolean allowed = interceptor.preHandle(
                request("POST", "/merchant/mall.Goods/delete"),
                new MockHttpServletResponse(),
                handler(new MallGoodsController(), "del"));

        assertFalse(allowed);
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void handlerMappingCanonicalizesContextPathMatrixParamsAndLowercaseAlias() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/lists"));
        MockHttpServletRequest request = request(
                "GET", "/gateway/merchant/mall.goods/lists;x=1");
        request.setContextPath("/gateway");

        boolean allowed = interceptor.preHandle(
                request, new MockHttpServletResponse(),
                handler(new MallGoodsController(), "list"));

        assertTrue(allowed);
        assertExactMenuQuery("mall.Goods/lists");
        assertEquals(
                "/merchant/mall.Goods/lists",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        handler(new MallGoodsController(), "list")));
    }

    @Test
    void fakeLoginSubstringIsNotAnAnonymousWhitelist() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request("GET", "/merchant/foo/login/bar"), response,
                handler(new FakeLoginHandler(), "bar"));

        assertFalse(allowed);
        assertTrue(response.getContentAsString().contains("\"code\":-1"));
    }

    @Test
    void exactPublicEndpointsRemainAnonymous() throws Exception {
        assertTrue(interceptor.preHandle(
                request("POST", "/merchant/login/login"), new MockHttpServletResponse(),
                handler(new LoginController(), "login")));
        assertTrue(interceptor.preHandle(
                request("GET", "/merchant/config/getConfig"), new MockHttpServletResponse(),
                handler(new ConfigController(), "getConfig")));
        assertTrue(interceptor.preHandle(
                request("GET", "/adminapi/channel/oa/callback"), new MockHttpServletResponse(),
                handler(new ChannelOaCallBackController(), "authGet")));
        assertTrue(interceptor.preHandle(
                request("POST", "/adminapi/channel/oa/callback"), new MockHttpServletResponse(),
                handler(new ChannelOaCallBackController(), "post")));
    }

    @Test
    void authenticatedBootstrapRoutesAllowNonSuperAdministratorWithoutPermissionQueries()
            throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        IndexController controller = new IndexController();
        MockHttpServletRequest menuRequest = request("GET", "/merchant/index/menu_list");
        MockHttpServletResponse menuResponse = new MockHttpServletResponse();
        HandlerMethod menuHandler = handler(controller, "menuList");

        assertTrue(interceptor.preHandle(menuRequest, menuResponse, menuHandler));
        assertEquals(9, MerchantThreadLocal.getAdminId());
        assertEquals(17, MerchantThreadLocal.getShopId());
        interceptor.afterCompletion(menuRequest, menuResponse, menuHandler, null);
        assertEquals(0, MerchantThreadLocal.getAdminId());

        MockHttpServletRequest selfRequest = request("GET", "/merchant/index/mySelf");
        MockHttpServletResponse selfResponse = new MockHttpServletResponse();
        HandlerMethod selfHandler = handler(controller, "mySelf");
        assertTrue(interceptor.preHandle(selfRequest, selfResponse, selfHandler));
        assertEquals(9, MerchantThreadLocal.getAdminId());
        interceptor.afterCompletion(selfRequest, selfResponse, selfHandler, null);

        verify(mallShopAdminMapper, times(2)).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void announcementNewestRequiresSeededRolePermission()
            throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("mall.announcement/lists:detail")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.announcement/lists:detail"));
        AnnouncementController controller = new AnnouncementController();
        MockHttpServletRequest request = request(
                "GET", "/merchant/mall.announcement/newest");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod routeHandler = handler(controller, "newest");

        assertTrue(interceptor.preHandle(request, response, routeHandler));
        assertEquals(9, MerchantThreadLocal.getAdminId());
        assertEquals(17, MerchantThreadLocal.getShopId());
        interceptor.afterCompletion(request, response, routeHandler, null);

        verify(mallShopAdminMapper).selectOne(any());
        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void goodsCategoryListsUsesDeclaredMenuAndRolePermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.Goods/lists"));
        MallGoodsController controller = new MallGoodsController();
        MockHttpServletRequest request = request(
                "GET", "/merchant/mall.goods/category_lists");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod routeHandler = handler(controller, "categoryLists");

        assertTrue(interceptor.preHandle(request, response, routeHandler));
        assertEquals(9, MerchantThreadLocal.getAdminId());
        assertEquals(17, MerchantThreadLocal.getShopId());
        interceptor.afterCompletion(request, response, routeHandler, null);

        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void goodsCategoryListsRejectsRoleWithoutAnyDeclaredMenuPermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of(
                enabledMenu("mall.Goods/lists"),
                enabledMenu("mall.Goods/add:edit")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.goodsComment/lists"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/mall.goods/category_lists"),
                response,
                handler(new MallGoodsController(), "categoryLists")));

        assertTrue(response.getContentAsString().contains("\"code\":0"));
        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void shopGoodsCategoryOtherListUsesCallingGoodsMenuPermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.Goods/lists"));
        MallShopGoodsCategoryController controller = new MallShopGoodsCategoryController();
        MockHttpServletRequest request = request(
                "GET", "/merchant/mall.shopGoodsCategory/otherList");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod routeHandler = handler(controller, "otherList");

        assertTrue(interceptor.preHandle(request, response, routeHandler));
        interceptor.afterCompletion(request, response, routeHandler, null);

        assertExactMenuQuery("mall.Goods/lists");
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void shopGoodsCategoryOtherListRejectsUnrelatedRolePermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.goodsComment/lists"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/mall.shopGoodsCategory/otherList"),
                response,
                handler(new MallShopGoodsCategoryController(), "otherList")));

        assertTrue(response.getContentAsString().contains("\"code\":0"));
        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService).getAdminAccessList(9);
    }

    @Test
    void anonymousAdministratorCannotReadShellAnnouncement() throws Exception {
        AnnouncementController controller = new AnnouncementController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/mall.announcement/newest"),
                response, handler(controller, "newest")));

        assertTrue(response.getContentAsString().contains("\"code\":-1"));
        verify(mallShopAdminMapper, never()).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void announcementManagementUsesRoleAssignableCombinedPermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("mall.announcement/lists:detail")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("mall.announcement/lists:detail"));
        AnnouncementController controller = new AnnouncementController();

        for (String methodName : List.of("lists", "detail")) {
            HandlerMethod routeHandler = handler(controller, methodName);
            MockHttpServletRequest request = request(
                    "GET", "/merchant/mall.announcement/" + methodName);
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertTrue(interceptor.preHandle(
                    request, response, routeHandler));
            interceptor.afterCompletion(request, response, routeHandler, null);
        }

        verify(mallShopMenuMapper, times(2)).selectList(any());
        verify(mallShopAdminMenuService, times(2)).getAdminAccessList(9);
    }

    @Test
    void anonymousBootstrapRoutesRemainRejected() throws Exception {
        IndexController controller = new IndexController();
        MockHttpServletResponse menuResponse = new MockHttpServletResponse();
        MockHttpServletResponse selfResponse = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/index/menu_list"),
                menuResponse, handler(controller, "menuList")));
        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/index/mySelf"),
                selfResponse, handler(controller, "mySelf")));

        assertTrue(menuResponse.getContentAsString().contains("\"code\":-1"));
        assertTrue(selfResponse.getContentAsString().contains("\"code\":-1"));
        verify(mallShopAdminMapper, never()).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void missingOrDisabledAdministratorCannotUseBootstrapRoutes() throws Exception {
        authenticate("9");
        MallShopAdmin disabled = admin(9, 17, 0);
        disabled.setStatus(0);
        when(mallShopAdminMapper.selectOne(any())).thenReturn(null, disabled);
        IndexController controller = new IndexController();

        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/index/menu_list"),
                new MockHttpServletResponse(), handler(controller, "menuList")));
        assertFalse(interceptor.preHandle(
                request("GET", "/merchant/index/mySelf"),
                new MockHttpServletResponse(), handler(controller, "mySelf")));

        verify(mallShopAdminMapper, times(2)).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void similarBootstrapPathStillRequiresSeededPermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());

        boolean allowed = interceptor.preHandle(
                request("GET", "/merchant/index/menu_list/delete"),
                new MockHttpServletResponse(),
                handler(new FakeBootstrapHandler(), "delete"));

        assertFalse(allowed);
        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void userControllerUsesSeededPermissionModuleAlias() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(
                List.of(enabledMenu("user.user/lists")),
                List.of(enabledMenu("user.user/detail")),
                List.of(enabledMenu("user.user/edit")),
                List.of());
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(
                List.of("user.user/lists"),
                List.of("user.user/detail"),
                List.of("user.user/edit"));
        UserController controller = new UserController();
        List<RouteCase> routes = List.of(
                new RouteCase("GET", "/merchant/user/lists", controller, "list"),
                new RouteCase("GET", "/merchant/user/detail", controller, "detail"),
                new RouteCase("POST", "/merchant/user/edit", controller, "edit"),
                new RouteCase("POST", "/merchant/user/adjustMoney", controller, "adjustWallet"));

        for (int index = 0; index < routes.size(); index++) {
            RouteCase route = routes.get(index);
            HandlerMethod routeHandler = handler(route.controller(), route.handlerMethod());
            assertEquals(route.path(), ReflectionTestUtils.invokeMethod(
                    interceptor, "resolveCanonicalRoute", routeHandler));
            MockHttpServletRequest request = request(route.httpMethod(), route.path());
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean allowed = interceptor.preHandle(request, response, routeHandler);
            if (index < 3) {
                assertTrue(allowed, route.path());
                interceptor.afterCompletion(request, response, routeHandler, null);
            } else {
                assertFalse(allowed, route.path());
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<QueryWrapper<MallShopMenu>> captor =
                ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(mallShopMenuMapper, times(4)).selectList(captor.capture());
        for (QueryWrapper<MallShopMenu> query : captor.getAllValues()) {
            query.getSqlSegment();
            assertTrue(query.getParamNameValuePairs().containsValue("user.user/%"));
        }
        verify(mallShopAdminMenuService, times(3)).getAdminAccessList(9);
    }

    @Test
    void authenticatedInfrastructureRoutesAreExactAndSkipPermissionQueries() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));

        for (RouteCase route : infrastructureRoutes()) {
            HandlerMethod routeHandler = handler(route.controller(), route.handlerMethod());
            assertEquals(route.path(), ReflectionTestUtils.invokeMethod(
                    interceptor, "resolveCanonicalRoute", routeHandler));
            MockHttpServletRequest request = request(route.httpMethod(), route.path());
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertTrue(interceptor.preHandle(request, response, routeHandler), route.path());
            assertEquals(9, MerchantThreadLocal.getAdminId());
            assertEquals(17, MerchantThreadLocal.getShopId());
            interceptor.afterCompletion(request, response, routeHandler, null);
        }

        assertEquals(14, infrastructureRoutes().size());
        verify(mallShopAdminMapper, times(14)).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void anonymousInfrastructureRoutesRemainRejected() throws Exception {
        List<RouteCase> representatives = List.of(
                new RouteCase("POST", "/merchant/upload/image",
                        new UploadController(), "image"),
                new RouteCase("GET", "/merchant/area/lists",
                        new AreaController(), "lists"),
                new RouteCase("POST", "/merchant/file/delete",
                        new FileController(), "del"));

        for (RouteCase route : representatives) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(
                    request(route.httpMethod(), route.path()), response,
                    handler(route.controller(), route.handlerMethod())));
            assertTrue(response.getContentAsString().contains("\"code\":-1"));
        }

        verify(mallShopAdminMapper, never()).selectOne(any());
        verify(mallShopMenuMapper, never()).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
    }

    @Test
    void similarInfrastructurePathStillRequiresSeededPermission() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any())).thenReturn(List.of());

        boolean allowed = interceptor.preHandle(
                request("GET", "/merchant/file/delete/all"),
                new MockHttpServletResponse(),
                handler(new FakeInfrastructureHandler(), "all"));

        assertFalse(allowed);
        verify(mallShopMenuMapper).selectList(any());
        verify(mallShopAdminMenuService, never()).getAdminAccessList(any());
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void unresolvedProtectedHandlerFailsClosed() throws Exception {
        boolean allowed = interceptor.preHandle(
                request("GET", "/merchant/unresolved"), new MockHttpServletResponse(),
                fallbackHandler);

        assertFalse(allowed);
        assertEquals(0, MerchantThreadLocal.getAdminId());
    }

    @Test
    void resolvesEveryDeclaredMerchantAndAdminapiHandler() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        List<String> unresolved = new ArrayList<>();
        int protectedHandlerCount = 0;

        for (Resource resource : resolver.getResources(
                "classpath*:com/houjiang/merchant/controller/**/*.class")) {
            String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata().getClassName();
            if (className.contains("$")) {
                continue;
            }
            Class<?> controllerType = Class.forName(
                    className, false, getClass().getClassLoader());
            if (!AnnotatedElementUtils.hasAnnotation(controllerType, RestController.class)
                    || !declaresProtectedNamespace(controllerType)) {
                continue;
            }
            for (Method method : controllerType.getDeclaredMethods()) {
                if (AnnotatedElementUtils.findMergedAnnotation(
                        method, RequestMapping.class) == null) {
                    continue;
                }
                protectedHandlerCount++;
                String canonicalRoute = ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute", controllerType, method);
                if (canonicalRoute == null
                        || !(canonicalRoute.startsWith("/merchant/")
                        || canonicalRoute.startsWith("/adminapi/"))) {
                    unresolved.add(controllerType.getSimpleName() + "#" + method.getName());
                }
            }
        }

        assertTrue(protectedHandlerCount > 0);
        assertTrue(unresolved.isEmpty(), () -> "Unresolved protected handlers: " + unresolved);
        assertEquals(
                "/adminapi/channel/oa/callback",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        ChannelOaCallBackController.class,
                        findMethod(ChannelOaCallBackController.class, "authGet")));
        assertEquals(
                "/adminapi/channel/oa/callback",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        ChannelOaCallBackController.class,
                        findMethod(ChannelOaCallBackController.class, "post")));
        assertEquals(
                "/merchant/user/lists",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        UserController.class, findMethod(UserController.class, "list")));
        assertEquals(
                "/merchant/upload/image",
                ReflectionTestUtils.invokeMethod(
                        interceptor, "resolveCanonicalRoute",
                        UploadController.class, findMethod(UploadController.class, "image")));
        assertEquals(
                "user.user/lists",
                MerchantInterceptor.permissionAccessForCanonical("user/lists"));
        assertEquals(
                "mall.Goods/lists",
                MerchantInterceptor.permissionAccessForCanonical("mall.Goods/lists"));
        assertTrue(MerchantInterceptor.menuTokenCoversCanonicalAccess(
                "user.user/lists", "user.user/lists"));
    }

    @Test
    void afterCompletionClearsMerchantContext() throws Exception {
        MerchantThreadLocal.put("adminId", 9);
        MerchantThreadLocal.put("shopId", 17);

        interceptor.afterCompletion(
                new MockHttpServletRequest(), new MockHttpServletResponse(), fallbackHandler, null);

        assertEquals(0, MerchantThreadLocal.getAdminId());
        assertEquals(0, MerchantThreadLocal.getShopId());
    }

    @Test
    void demoPostRejectionClearsContextImmediately() throws Exception {
        when(environment.getProperty("merchant.production")).thenReturn("true");
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 1));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/add:edit")));
        when(mallShopAdminMenuService.getAdminAccessList(9)).thenReturn(List.of("*"));

        boolean allowed = interceptor.preHandle(
                request("POST", "/merchant/mall.Goods/add"), new MockHttpServletResponse(),
                handler(new MallGoodsController(), "add"));

        assertFalse(allowed);
        assertEquals(0, MerchantThreadLocal.getAdminId());
        assertEquals(0, MerchantThreadLocal.getShopId());
    }

    @Test
    void permissionRejectionClearsContextImmediately() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/detail"));

        boolean allowed = interceptor.preHandle(
                request("GET", GOODS_PATH), new MockHttpServletResponse(),
                handler(new MallGoodsController(), "list"));

        assertFalse(allowed);
        assertEquals(0, MerchantThreadLocal.getAdminId());
        assertEquals(0, MerchantThreadLocal.getShopId());
    }

    @Test
    void successfulRequestKeepsContextUntilCompletion() throws Exception {
        authenticate("9");
        when(mallShopAdminMapper.selectOne(any())).thenReturn(admin(9, 17, 0));
        when(mallShopMenuMapper.selectList(any()))
                .thenReturn(List.of(enabledMenu("mall.Goods/lists")));
        when(mallShopAdminMenuService.getAdminAccessList(9))
                .thenReturn(List.of("mall.Goods/lists"));
        MockHttpServletRequest request = request("GET", GOODS_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handler(new MallGoodsController(), "list");

        assertTrue(interceptor.preHandle(request, response, handler));
        assertEquals(9, MerchantThreadLocal.getAdminId());
        assertEquals(17, MerchantThreadLocal.getShopId());

        interceptor.afterCompletion(request, response, handler, null);
        assertEquals(0, MerchantThreadLocal.getAdminId());
        assertEquals(0, MerchantThreadLocal.getShopId());
    }

    @Test
    void nonNumericUserIdIsRejectedAsInvalidToken() throws Exception {
        authenticate("not-a-number");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request("GET", GOODS_PATH), response,
                handler(new MallGoodsController(), "list"));

        assertFalse(allowed);
        assertTrue(response.getContentAsString().contains("\"code\":-1"));
        assertEquals(0, MerchantThreadLocal.getAdminId());
        verify(mallShopAdminMapper, never()).selectOne(any());
    }

    @Test
    void menuListReturnsMenusForCurrentAdministrator() {
        JSONArray expectedMenus = new JSONArray();
        expectedMenus.add(Map.of("id", 101, "name", "Goods"));
        when(mallShopAdminMenuService.getShopAdminMenuList(9)).thenReturn(expectedMenus);
        MerchantThreadLocal.put("adminId", 9);
        IndexController controller = new IndexController();
        ReflectionTestUtils.setField(
                controller, "iMallShopAdminMenuService", mallShopAdminMenuService);

        AjaxResult<JSONArray> result = controller.menuList();

        assertSame(expectedMenus, result.getData());
        verify(mallShopAdminMenuService).getShopAdminMenuList(9);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void assertMenuQuery(String expectedModule) {
        ArgumentCaptor<QueryWrapper<MallShopMenu>> captor =
                ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(mallShopMenuMapper).selectList(captor.capture());
        QueryWrapper<MallShopMenu> query = captor.getValue();
        query.getSqlSegment();
        assertTrue(query.getParamNameValuePairs().containsValue(expectedModule + "/%"));
        assertTrue(query.getParamNameValuePairs().containsValue(1));
        assertTrue(query.getParamNameValuePairs().containsValue(0));
        assertEquals("access", query.getSqlSelect());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void assertExactMenuQuery(String expectedToken) {
        ArgumentCaptor<QueryWrapper<MallShopMenu>> captor =
                ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(mallShopMenuMapper).selectList(captor.capture());
        QueryWrapper<MallShopMenu> query = captor.getValue();
        assertTrue(query.getSqlSegment().contains("access IN"));
        assertTrue(query.getParamNameValuePairs().containsValue(expectedToken));
        assertTrue(query.getParamNameValuePairs().containsValue(1));
        assertTrue(query.getParamNameValuePairs().containsValue(0));
        assertEquals("access", query.getSqlSelect());
    }

    private void assertNoPermission(LoginException exception) {
        assertEquals(ErrorEnum.NO_PERMISSION.getCode(), exception.getCode());
        assertEquals("无权限访问该功能", exception.getMsg());
    }

    private void authenticate(String userId) {
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));
    }

    private MallShopAdmin admin(int id, int shopId, int isSuper) {
        MallShopAdmin admin = new MallShopAdmin();
        admin.setId(id);
        admin.setShopId(shopId);
        admin.setName("merchant-" + id);
        admin.setStatus(1);
        admin.setIsSuper(isSuper);
        return admin;
    }

    private MallShopMenu enabledMenu(String access) {
        MallShopMenu menu = new MallShopMenu();
        menu.setAccess(access);
        menu.setStatus(1);
        menu.setDeleteTime(0L);
        return menu;
    }

    private MockHttpServletRequest request(String method, String uri) {
        return new MockHttpServletRequest(method, uri);
    }

    private HandlerMethod handler(Object bean, String methodName) {
        Method method = findMethod(bean.getClass(), methodName);
        return new HandlerMethod(bean, method);
    }

    private Method findMethod(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private boolean declaresProtectedNamespace(Class<?> controllerType) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(
                controllerType, RequestMapping.class);
        if (mapping == null) {
            return false;
        }
        String[] paths = mapping.path().length > 0 ? mapping.path() : mapping.value();
        return Arrays.stream(paths)
                .map(path -> path.startsWith("/") ? path : "/" + path)
                .anyMatch(path -> path.equals("/merchant")
                        || path.startsWith("/merchant/")
                        || path.equals("/adminapi")
                        || path.startsWith("/adminapi/"));
    }

    private List<RouteCase> infrastructureRoutes() {
        UploadController upload = new UploadController();
        AreaController area = new AreaController();
        FileController file = new FileController();
        return List.of(
                new RouteCase("POST", "/merchant/upload/image", upload, "image"),
                new RouteCase("POST", "/merchant/upload/file", upload, "file"),
                new RouteCase("POST", "/merchant/upload/video", upload, "video"),
                new RouteCase("GET", "/merchant/area/lists", area, "lists"),
                new RouteCase("GET", "/merchant/area/lists_all", area, "listAll"),
                new RouteCase("POST", "/merchant/area/tencent_search", area, "tencentSearch"),
                new RouteCase("GET", "/merchant/file/lists", file, "fileList"),
                new RouteCase("POST", "/merchant/file/rename", file, "rename"),
                new RouteCase("POST", "/merchant/file/move", file, "move"),
                new RouteCase("POST", "/merchant/file/delete", file, "del"),
                new RouteCase("GET", "/merchant/file/listCate", file, "cateList"),
                new RouteCase("POST", "/merchant/file/addCate", file, "cateAdd"),
                new RouteCase("POST", "/merchant/file/editCate", file, "cateRename"),
                new RouteCase("POST", "/merchant/file/delCate", file, "cateDel"));
    }

    private record RouteCase(
            String httpMethod, String path, Object controller, String handlerMethod) {
    }

    private static final class TestInterceptorRegistry extends InterceptorRegistry {
        List<Object> registered() {
            return getInterceptors();
        }
    }

    @RequestMapping("/merchant/foo/login")
    private static final class FakeLoginHandler {
        @GetMapping("/bar")
        public void bar() {
        }
    }

    @RequestMapping("/merchant/index/menu_list")
    private static final class FakeBootstrapHandler {
        @GetMapping("/delete")
        public void delete() {
        }
    }

    @RequestMapping("/merchant/file/delete")
    private static final class FakeInfrastructureHandler {
        @GetMapping("/all")
        public void all() {
        }
    }

    private static final class UnmappedHandler {
        public void handle() {
        }
    }
}
