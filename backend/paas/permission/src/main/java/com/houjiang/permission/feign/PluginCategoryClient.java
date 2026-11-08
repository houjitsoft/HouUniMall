package com.houjiang.permission.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

/** base 是分类目录的唯一数据所有者。沿用调用者会话，禁止用服务身份绕过菜单编辑权限。 */
@FeignClient(name = "base", contextId = "permissionPluginCategoryClient")
public interface PluginCategoryClient {
    @PostMapping("/base/plugin-center/categories")
    ResponseResult<List<Map<String, Object>>> categories();

    @PostMapping("/base/plugin-center/catalog/category/update")
    ResponseResult<Void> updateCategory(@RequestBody Map<String, Object> request);
}
