package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: runs against a disposable schema initialized with core + commercial migrations. */
@EnabledIfEnvironmentVariable(named="UNIMALL_TEST_JDBC_URL",matches=".+")
class PaidMaintenanceMysqlTest {
    JdbcTemplate jdbc;TransactionTemplate tx;PluginLicenseService licenses;String site;
    @BeforeEach void setup() throws Exception {
        String url=System.getenv("UNIMALL_TEST_JDBC_URL");
        if(!url.contains("127.0.0.1:") || !url.contains("/unimall_delivery_test?"))throw new IllegalArgumentException("Only the isolated local test schema is allowed");
        var ds=new DriverManagerDataSource(url,"root","");jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        var factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(ds);
        var config=new MybatisConfiguration();config.setMapUnderscoreToCamelCase(true);config.addMapper(MarketPluginLicenseMapper.class);factory.setConfiguration(config);
        licenses=new PluginLicenseService(new SqlSessionTemplate(factory.getObject()).getMapper(MarketPluginLicenseMapper.class));
        site="test-"+UUID.randomUUID();
    }
    @AfterEach void cleanup() {
        jdbc.update("DELETE FROM hou_market_plugin_license WHERE instance_id=?",site);
        jdbc.update("DELETE FROM hou_service_entitlement_lock WHERE instance_id=?",site);
    }
    LocalDateTime date(Object value) {return value instanceof LocalDateTime d?d:((java.sql.Timestamp)value).toLocalDateTime();}
    void buy(String order,boolean sandbox) {tx.executeWithoutResult(status->licenses.issuePaidOrder("erp",1L,site,site+order,sandbox));}
    @Test void concurrentFirstPurchasesGrantTwentyFourMonthsAndRepeatedOrderIsIdempotent() throws Exception {
        var pool=Executors.newFixedThreadPool(2);var ready=new CountDownLatch(2);var start=new CountDownLatch(1);
        try {
            var first=pool.submit(()->{ready.countDown();start.await();buy("A",false);return true;});
            var second=pool.submit(()->{ready.countDown();start.await();buy("B",false);return true;});
            assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();assertTrue(first.get(15,TimeUnit.SECONDS));assertTrue(second.get(15,TimeUnit.SECONDS));
        } finally {pool.shutdownNow();}
        buy("A",false);
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM hou_market_plugin_license WHERE instance_id=?",Integer.class,site));
        var dates=jdbc.queryForMap("SELECT MIN(create_time) purchased,MAX(maintenance_until) until_at FROM hou_market_plugin_license WHERE instance_id=?",site);
        LocalDateTime purchased=date(dates.get("purchased"));
        assertEquals(purchased.plusMonths(24),date(dates.get("until_at")));
    }
    @Test void middleRefundRecomputesLaterGrantAndSandboxDoesNotMaskPurchase() {
        buy("A",false);buy("B",false);buy("C",false);buy("sandbox",true);
        var original=licenses.findValidPaid("erp",site);assertNotNull(original);
        tx.executeWithoutResult(status->licenses.refundPaidOrder("erp",site,site+"B","test refund"));
        var revised=licenses.findValidPaid("erp",site);assertNotNull(revised);
        assertEquals(original.getMaintenanceUntil().minusMonths(12),revised.getMaintenanceUntil());
        assertEquals(0,revised.getPurchaseSandbox());assertNull(revised.getExpireTime());
        assertNull(licenses.findValidPaid("oa",site));assertNull(licenses.findValidPaid("erp","someone-else"));
    }
}
