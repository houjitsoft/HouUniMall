package com.houjiang.userms.service;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OAuthMemberServiceTest {
    JdbcTemplate jdbc;
    OAuthMemberService service;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE hou_user_info (user_id varchar(32) PRIMARY KEY,user_mobile varchar(50) DEFAULT '1',password char(80) NOT NULL,international_dialing_code varchar(50),user_name varchar(225),user_state int NOT NULL,user_integral int,user_count_login int NOT NULL,user_is_new int NOT NULL,user_nickname varchar(100),create_time timestamp,is_delete int,is_business int,is_admin int,follower_num int,follow_num int,member_create_time timestamp,register_terminal varchar(32))");
        jdbc.execute("CREATE TABLE hou_user_info_detail (user_id varchar(32) PRIMARY KEY,user_avatar varchar(255),user_gender int NOT NULL,user_reg_time bigint NOT NULL,user_mobile_verify int NOT NULL,user_parent_id varchar(18),user_platform int,validate_long_term int)");
        jdbc.execute("CREATE TABLE hou_user_bind_connect (bind_id varchar(100),bind_type int,user_id varchar(18) NOT NULL DEFAULT '0',bind_nickname varchar(255),bind_avator varchar(255),bind_openid varchar(200),bind_unionid varchar(255),is_auto_login int,PRIMARY KEY(bind_id,bind_type))");
        jdbc.execute("CREATE TABLE hou_user_oauth_identity (channel varchar(32),provider_app_id varchar(128),openid varchar(200),unionid varchar(255),user_id bigint NOT NULL,legacy_bind_id varchar(100) UNIQUE,PRIMARY KEY(channel,provider_app_id,openid))");
        ProxyFactory factory = new ProxyFactory(new OAuthMemberTransaction(jdbc, "", "app1"));
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(ds), new AnnotationTransactionAttributeSource()));
        service = new OAuthMemberService((OAuthMemberTransaction) factory.getProxy());
    }

    Map<String, String> request() {
        return new HashMap<>(Map.of("channel", "wechat", "providerAppId", "app1", "openid", "open1",
                "unionid", "union1", "nickname", "昵称", "registerTerminal", "houocr", "allowCreate", "true"));
    }

    int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Test
    void firstAndRepeatKeepMobileUnverifiedAndNoPrivileges() {
        Map<String, Object> first = service.getOrCreate(request());
        assertEquals(true, first.get("created"));
        Map<String, Object> user = jdbc.queryForMap("SELECT * FROM hou_user_info");
        assertNull(user.get("user_mobile"));
        assertEquals("oauth_" + first.get("userId"), user.get("user_name"));
        assertEquals(0, user.get("is_admin"));
        assertEquals(0, jdbc.queryForObject("SELECT user_mobile_verify FROM hou_user_info_detail", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT bind_type FROM hou_user_bind_connect", Integer.class));
        assertTrue(jdbc.queryForObject("SELECT bind_id FROM hou_user_bind_connect", String.class).startsWith("oauth_"));
        Map<String, String> repeat = request();
        repeat.put("allowCreate", "false");
        assertEquals(Map.of("userId", first.get("userId"), "created", false), service.getOrCreate(repeat));
        assertEquals(1, count("hou_user_info"));
        assertEquals(1, count("hou_user_info_detail"));
        assertEquals(1, count("hou_user_bind_connect"));
    }

    @Test
    void concurrentRequestsReturnOneWinnerWithoutOrphans() throws Exception {
        try (var pool = Executors.newFixedThreadPool(8)) {
            CountDownLatch start = new CountDownLatch(1);
            var futures = new ArrayList<java.util.concurrent.Future<Map<String, Object>>>();
            for (int i = 0; i < 8; i++) futures.add(pool.submit(() -> {
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return service.getOrCreate(request());
            }));
            start.countDown();
            var results = new ArrayList<Map<String, Object>>();
            for (var future : futures) results.add(future.get(15, TimeUnit.SECONDS));
            assertEquals(1, results.stream().map(r -> r.get("userId")).distinct().count());
            assertEquals(1, results.stream().filter(r -> Boolean.TRUE.equals(r.get("created"))).count());
        }
        for (String table : new String[]{"hou_user_info", "hou_user_info_detail", "hou_user_bind_connect", "hou_user_oauth_identity"}) {
            assertEquals(1, count(table), table);
        }
    }

    @Test
    void bindInsertFailureRollsBackAllCoreRows() {
        jdbc.execute("ALTER TABLE hou_user_bind_connect ADD CONSTRAINT reject_bind CHECK (bind_type=1)");
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.getOrCreate(request()));
        assertEquals(0, count("hou_user_info"));
        assertEquals(0, count("hou_user_info_detail"));
        assertEquals(0, count("hou_user_oauth_identity"));
    }

    @Test
    void duplicateKeyWithoutIdentityWinnerIsNotSwallowed() {
        var identity = com.houjiang.userms.dto.OAuthIdentity.from(request());
        jdbc.update("INSERT INTO hou_user_bind_connect(bind_id,bind_type,user_id) VALUES (?,2,'999')", OAuthMemberTransaction.bindId(identity));
        assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> service.getOrCreate(request()));
        assertEquals(0, count("hou_user_info"));
    }

    @Test
    void scopedIdentityDoesNotMergeByUnionid() {
        Object first = service.getOrCreate(request()).get("userId");
        var other = request();
        other.put("providerAppId", "app2");
        assertNotEquals(first, service.getOrCreate(other).get("userId"));
        var mismatch = request();
        mismatch.put("unionid", "different");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(mismatch));
        assertEquals(2, count("hou_user_info"));
    }

    void legacy(int type) {
        jdbc.update("INSERT INTO hou_user_info(user_id,password,user_state,user_count_login,user_is_new,is_delete) VALUES ('42','',1,0,0,0)");
        jdbc.update("INSERT INTO hou_user_bind_connect(bind_id,bind_type,user_id,bind_openid,bind_unionid) VALUES ('weixin_open1',?,'42','open1','union1')", type);
    }

    @Test
    void adoptsLegacyWechatOnlyWithExactTypeAndConfirmedApp() {
        legacy(2);
        assertEquals(Map.of("userId", "42", "created", false), service.getOrCreate(request()));
        assertEquals(1, count("hou_user_bind_connect"));
        var other = request();
        other.put("providerAppId", "app2");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(other));
        assertEquals(1, count("hou_user_info"));
    }

    @Test
    void legacyUnionConflictNeverChangesBindingOwner() {
        legacy(2);
        var body = request();
        body.put("unionid", "wrong-union");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(body));
        assertEquals("42", jdbc.queryForObject("SELECT user_id FROM hou_user_bind_connect", String.class));
        assertEquals(0, count("hou_user_oauth_identity"));
    }

    @Test
    void disabledMemberIsNotRecreatedOrLoggedIn() {
        var first = service.getOrCreate(request());
        jdbc.update("UPDATE hou_user_info SET user_state=3 WHERE user_id=?", first.get("userId"));
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(request()));
        assertEquals(1, count("hou_user_info"));
    }

    @Test
    void wrongLegacyBindTypeCannotLoginOrRebind() {
        legacy(3);
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(request()));
        assertEquals(0, count("hou_user_oauth_identity"));
    }

    @Test
    void registeredIdentityCannotUseAnotherMembersBinding() {
        service.getOrCreate(request());
        jdbc.update("UPDATE hou_user_bind_connect SET user_id='999'");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(request()));
        assertEquals("999", jdbc.queryForObject("SELECT user_id FROM hou_user_bind_connect", String.class));
        assertEquals(1, count("hou_user_info"));
    }

    @Test
    void missingRealBindingFailsClosed() {
        service.getOrCreate(request());
        jdbc.update("DELETE FROM hou_user_bind_connect");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(request()));
        assertEquals(1, count("hou_user_info"));
    }

    @Test
    void detailInsertFailureRollsBackUser() {
        jdbc.execute("ALTER TABLE hou_user_info_detail ADD CONSTRAINT reject_detail CHECK (user_mobile_verify=1)");
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.getOrCreate(request()));
        for (String table : new String[]{"hou_user_info", "hou_user_info_detail", "hou_user_bind_connect", "hou_user_oauth_identity"}) {
            assertEquals(0, count(table), table);
        }
    }

    @Test
    void differentChannelsDoNotShareIdentity() {
        Object first = service.getOrCreate(request()).get("userId");
        var other = request();
        other.put("channel", "qq");
        assertNotEquals(first, service.getOrCreate(other).get("userId"));
        assertEquals(2, count("hou_user_info"));
    }

    @Test
    void nullOptionalValuesAreRejectedWithoutWrites() {
        for (String field : new String[]{"nickname", "avatar", "registerTerminal", "allowCreate"}) {
            var body = request();
            body.put(field, null);
            assertThrows(IllegalArgumentException.class, () -> service.getOrCreate(body), field);
        }
        assertThrows(IllegalArgumentException.class, () -> service.getOrCreate(null));
        assertEquals(0, count("hou_user_info"));
    }

    @Test
    void concurrentDifferentIdentitiesHaveDistinctValidUserIds() throws Exception {
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<java.util.concurrent.Future<Map<String, Object>>>();
            for (int i = 0; i < 24; i++) {
                var body = request();
                body.put("openid", "open-" + i);
                futures.add(pool.submit(() -> service.getOrCreate(body)));
            }
            var ids = new java.util.HashSet<Object>();
            for (var future : futures) {
                Object id = future.get(15, TimeUnit.SECONDS).get("userId");
                assertTrue(id.toString().matches("[1-9][0-9]{17}"));
                assertTrue(ids.add(id));
            }
        }
        assertEquals(24, count("hou_user_info"));
        assertEquals(24, count("hou_user_bind_connect"));
    }

    @Test
    void missingAllowCreateAndInvalidIdentityDoNotRegister() {
        var body = request();
        body.remove("allowCreate");
        assertThrows(IllegalStateException.class, () -> service.getOrCreate(body));
        body.put("openid", "");
        assertThrows(IllegalArgumentException.class, () -> service.getOrCreate(body));
        assertEquals(0, count("hou_user_info"));
    }
}
