package com.houjiang.userms.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserInfoMapperXmlTest {

    @Test
    void loginLookupMustNotReferenceMissingShopIdColumnOnHouUserInfo() throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream("mapper/UserInfoMapper.xml")) {
            assertNotNull(input, "UserInfoMapper.xml must be available on the test classpath");
            String mapperXml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertFalse(mapperXml.contains("uui.shop_id is null"),
                    "hou_user_info has no shop_id column; this condition breaks every password login");
        }
    }
}
