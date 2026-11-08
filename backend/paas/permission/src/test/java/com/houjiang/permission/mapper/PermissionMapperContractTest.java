package com.houjiang.permission.mapper;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionMapperContractTest {

    @Test
    void roleMenuListUsesMyBatisDynamicScript() throws Exception {
        String sql = selectSql(RoleMenuMapper.class.getMethod("getMenuIdsByRoleIds", java.util.List.class));

        assertTrue(sql.startsWith("<script>"));
        assertTrue(sql.endsWith("</script>"));
        assertTrue(sql.contains("<foreach"));
    }

    @Test
    void livePermissionCheckUsesDynamicScriptAndExcludesDisabledOrDeletedAssignments() throws Exception {
        String sql = selectSql(PermissionMenuMapper.class.getMethod(
                "checkPermission", Long.class, String.class, java.util.List.class));

        assertTrue(sql.startsWith("<script>"));
        assertTrue(sql.endsWith("</script>"));
        assertTrue(sql.contains("m.is_disable = 0"));
        assertTrue(sql.contains("hou_permission_role r"));
        assertTrue(sql.contains("r.delete_time IS NULL"));
    }

    @Test
    void userRoleLookupExcludesSoftDeletedRoles() throws Exception {
        String sql = selectSql(UserRoleMapper.class.getMethod("getRoleIdsByUserId", Long.class));

        assertTrue(sql.contains("hou_permission_role r"));
        assertTrue(sql.contains("r.delete_time IS NULL"));
    }

    private static String selectSql(Method method) {
        Select select = method.getAnnotation(Select.class);
        return String.join("", Arrays.asList(select.value()));
    }
}
