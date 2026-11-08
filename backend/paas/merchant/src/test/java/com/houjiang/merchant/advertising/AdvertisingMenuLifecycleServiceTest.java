package com.houjiang.merchant.advertising;

import com.houjiang.merchant.service.AdvertisingMenuLifecycleService;
import com.houjiang.merchant.service.AdvertisingMenuLifecycleService.MenuGateway;
import com.houjiang.merchant.service.AdvertisingMenuLifecycleService.MenuRecord;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AdvertisingMenuLifecycleServiceTest {

    @Test
    void springCreatesServiceWithJdbcGatewayWhenTestConstructorAlsoExists() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class));
            context.register(AdvertisingMenuLifecycleService.class);
            context.refresh();

            assertNotNull(context.getBean(AdvertisingMenuLifecycleService.class));
        }
    }

    @Test
    void repeatedSyncCreatesOneEnabledAdvertisingMenuUnderMarketing() {
        InMemoryMenuGateway gateway = new InMemoryMenuGateway(marketingParent());
        AdvertisingMenuLifecycleService service = new AdvertisingMenuLifecycleService(gateway);

        int first = service.sync();
        int second = service.sync();

        assertEquals(first, second);
        assertEquals(1, gateway.advertisingRows().size());
        MenuRecord menu = gateway.advertisingRows().getFirst();
        assertEquals(41, menu.pid());
        assertEquals("广告投放", menu.name());
        assertEquals("advertising", menu.route());
        assertEquals("plugin:mall-advertising/merchant-applications", menu.component());
        assertEquals("advertising/applications:list:save:submit:withdraw", menu.access());
        assertEquals(1, menu.status());
        assertEquals(1, menu.showStatus());
        assertEquals(0L, menu.deleteTime());
    }

    @Test
    void disableHidesAndResyncRestoresTheSameMenu() {
        InMemoryMenuGateway gateway = new InMemoryMenuGateway(marketingParent());
        AdvertisingMenuLifecycleService service = new AdvertisingMenuLifecycleService(gateway);
        int id = service.sync();

        service.disable();
        MenuRecord disabled = gateway.byId(id);
        assertEquals(0, disabled.status());
        assertEquals(0, disabled.showStatus());
        assertEquals(0L, disabled.deleteTime());

        assertEquals(id, service.sync());
        MenuRecord restored = gateway.byId(id);
        assertEquals(1, restored.status());
        assertEquals(1, restored.showStatus());
        assertEquals(0L, restored.deleteTime());
        assertEquals(1, gateway.advertisingRows().size());
    }

    @Test
    void removeSoftDeletesAndLaterSyncRevivesWithoutDuplicate() {
        InMemoryMenuGateway gateway = new InMemoryMenuGateway(marketingParent());
        AdvertisingMenuLifecycleService service = new AdvertisingMenuLifecycleService(gateway);
        int id = service.sync();

        service.remove();
        MenuRecord removed = gateway.byId(id);
        assertEquals(0, removed.status());
        assertEquals(0, removed.showStatus());
        org.junit.jupiter.api.Assertions.assertTrue(removed.deleteTime() > 0);

        assertEquals(id, service.sync());
        assertEquals(1, gateway.advertisingRows().size());
        assertEquals(0L, gateway.byId(id).deleteTime());
    }

    @Test
    void missingMarketingParentFailsInsteadOfCreatingRootMenu() {
        AdvertisingMenuLifecycleService service = new AdvertisingMenuLifecycleService(
                new InMemoryMenuGateway(null));

        IllegalStateException error = assertThrows(IllegalStateException.class, service::sync);

        assertEquals("未找到商家端营销/运营父菜单", error.getMessage());
    }

    private static MenuRecord marketingParent() {
        return new MenuRecord(41, 0, 0, "营销管理", "Promotion", "marketing", 500,
                "", "", "", "marketing", 0, 1, 1, 1L, 1L, 0L);
    }

    private static final class InMemoryMenuGateway implements MenuGateway {
        private final MenuRecord parent;
        private final List<MenuRecord> rows = new ArrayList<>();
        private int nextId = 100;

        private InMemoryMenuGateway(MenuRecord parent) {
            this.parent = parent;
        }

        @Override
        public MenuRecord lockMarketingParent() {
            return parent;
        }

        @Override
        public MenuRecord findAdvertisingMenu() {
            return rows.stream().filter(row -> AdvertisingMenuLifecycleService.MENU_ACCESS.equals(row.access()))
                    .findFirst().orElse(null);
        }

        @Override
        public int insert(MenuRecord menu) {
            MenuRecord inserted = menu.withId(nextId++);
            rows.add(inserted);
            return inserted.id();
        }

        @Override
        public void update(MenuRecord menu) {
            for (int index = 0; index < rows.size(); index++) {
                if (rows.get(index).id().equals(menu.id())) {
                    rows.set(index, menu);
                    return;
                }
            }
            throw new IllegalStateException("menu not found");
        }

        List<MenuRecord> advertisingRows() {
            return rows.stream().filter(row -> AdvertisingMenuLifecycleService.MENU_ACCESS.equals(row.access()))
                    .toList();
        }

        MenuRecord byId(int id) {
            return rows.stream().filter(row -> row.id() == id).findFirst().orElseThrow();
        }
    }
}
