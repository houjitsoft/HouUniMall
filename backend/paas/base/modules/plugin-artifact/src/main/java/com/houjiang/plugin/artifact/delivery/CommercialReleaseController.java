package com.houjiang.plugin.artifact.delivery;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.plugin.artifact.identity.PluginIdentityResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/base/plugin-center/publish")
public class CommercialReleaseController {
    private final CommercialReleaseService releases;
    @Value("${unimall.delivery.publishing-enabled:false}") private boolean publishingEnabled;
    public CommercialReleaseController(CommercialReleaseService releases) { this.releases = releases; }

    @PostMapping(value="/service-release", consumes="multipart/form-data")
    public ResponseResult<Object> publish(@RequestPart("metadata") String metadata,
            @RequestPart("source") MultipartFile source, @RequestPart("runtime") MultipartFile runtime) {
        if (!publishingEnabled || !SecurityContextUtils.hasPermission("shell:plugin:release:publish"))
            return ResponseResult.fail(403, "当前实例未开放官方服务发布或没有发布权限");
        var identity = PluginIdentityResolver.fromCurrentContext();
        if (identity == null) return ResponseResult.fail(401, "未登录");
        try { return ResponseResult.success(releases.publish(metadata, source, runtime, identity.getUserId())); }
        catch (Exception e) { return ResponseResult.fail(400, "发行失败：" + e.getMessage()); }
    }
}
