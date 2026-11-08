package com.houjiang.bffweb.feign;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import static org.junit.jupiter.api.Assertions.*;

class PluginFeignOwnershipTest {
    @Test
    void fixedPluginClientsUseOwningServicesAndPreservePluginAdapterPaths() {
        Map<Class<?>, String> owners = Map.ofEntries(
                Map.entry(OaServiceFeign.class, "oa"), Map.entry(OaContractServiceFeign.class, "oa"),
                Map.entry(MesServiceFeign.class, "mes"), Map.entry(FinanceServiceFeign.class, "finance"),
                Map.entry(CmsServiceFeign.class, "cms"), Map.entry(UcmsServiceFeign.class, "cms"),
                Map.entry(WebsiteLeadServiceFeign.class, "cms"), Map.entry(ActivityServiceFeign.class, "order"),
                Map.entry(WebposServiceFeign.class, "webpos"), Map.entry(ImServiceFeign.class, "im"),
                Map.entry(CrmServiceFeign.class, "crm"), Map.entry(UcrmServiceFeign.class, "crm"),
                Map.entry(LogServiceFeign.class, "logging"), Map.entry(GeoServiceFeign.class, "geo"),
                Map.entry(PmServiceFeign.class, "pm"), Map.entry(RiderServiceFeign.class, "rider"),
                Map.entry(SupplierServiceFeign.class, "supplier"), Map.entry(OrderfoodServiceFeign.class, "orderfood"));
        owners.forEach((type, owner) -> {
            FeignClient client = type.getAnnotation(FeignClient.class);
            assertEquals(owner, client.name().isBlank() ? client.value() : client.name(), type.getSimpleName());
            assertEquals("${feign." + owner + ".url:}", client.url(), type.getSimpleName());
            assertTrue(client.path().equals("/plugin") || client.path().startsWith("/plugin/"), type.getSimpleName());
        });
    }
}
