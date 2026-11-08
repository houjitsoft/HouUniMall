package com.houjiang.base.plugincenter;

import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.plugin.artifact.delivery.CommercialReleaseService;
import com.houjiang.plugin.artifact.download.PluginArtifactDownloadService;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.identity.PluginIdentityResolver;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.LinkedHashMap;

/** Public service lifecycle commands terminate at a configured installation agent, never PF4J. */
@RestController
@RequestMapping("/base/plugin-center/service")
public class ServiceDeliveryController {
    private final CommercialReleaseService releases;
    private final PluginArtifactDownloadService downloads;
    private final ServiceInstallationService installations;
    private final ServiceInstallerClient agent;
    public ServiceDeliveryController(CommercialReleaseService releases, PluginArtifactDownloadService downloads,
            ServiceInstallationService installations, ServiceInstallerClient agent) {
        this.releases=releases;this.downloads=downloads;this.installations=installations;this.agent=agent;
    }

    @PostMapping("/delivery")
    public ResponseResult<Object> delivery(@RequestBody Map<String,Object> request) {
        try {
            String kind=request.get("artifactKind")==null?null:String.valueOf(request.get("artifactKind"));
            return ResponseResult.success(bundle(text(request,"pluginCode"),text(request,"version"),identity(),kind));
        }
        catch (DownloadDeniedException e) { return ResponseResult.fail(e.getCode(),e.getMessage()); }
        catch (Exception e) { return ResponseResult.fail(400,e.getMessage()); }
    }

    @PostMapping("/install")
    public ResponseResult<Object> install(@RequestBody Map<String,Object> request) {
        var denied = PluginManageGate.check(true); if (denied != null) return denied;
        try {
            PluginIdentity identity = identity();
            Map<String,Object> bundle = bundle(text(request,"pluginCode"),text(request,"version"),identity);
            return ResponseResult.success(submit(identity,bundle));
        } catch (DownloadDeniedException e) { return ResponseResult.fail(e.getCode(),e.getMessage()); }
        catch (Exception e) { return ResponseResult.fail(503,"服务安装未提交："+e.getMessage()); }
    }

    @PostMapping("/upgrade")
    public ResponseResult<Object> upgrade(@RequestBody Map<String,Object> request) {
        var denied = PluginManageGate.check(true); if (denied != null) return denied;
        try { return ResponseResult.success(submit(identity(),bundle(text(request,"pluginCode"),text(request,"version"),identity()))); }
        catch (DownloadDeniedException e) { return ResponseResult.fail(e.getCode(),e.getMessage()); }
        catch (Exception e) { return ResponseResult.fail(503,"服务升级未提交："+e.getMessage()); }
    }

    @PostMapping("/task/status")
    public ResponseResult<Object> status(@RequestBody Map<String,Object> request) {
        try {
            String id = text(request,"taskId"); if (!id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("任务编号无效");
            PluginIdentity site = identity();
            installations.requireTask(site.getInstanceId(),id);
            Object result = agent.call(site.getInstanceId(), "/tasks/"+id, null);
            if (result instanceof Map<?, ?> task) installations.reconcile(site.getInstanceId(),id,task);
            return ResponseResult.success(result);
        } catch (Exception e) { return ResponseResult.fail(503,"无法查询安装任务："+e.getMessage()); }
    }

    @PostMapping("/history")
    public ResponseResult<Object> history() {
        try { return ResponseResult.success(agent.call(identity().getInstanceId(), "/tasks", null)); }
        catch (Exception e) { return ResponseResult.fail(503,"无法查询安装记录："+e.getMessage()); }
    }

    private Map<String,Object> bundle(String code,String version,PluginIdentity identity) {
        return bundle(code,version,identity,null);
    }
    private Map<String,Object> bundle(String code,String version,PluginIdentity identity,String kind) {
        if(kind!=null && !java.util.List.of("source","runtime").contains(kind))throw new IllegalArgumentException("不支持的制品类型");
        var release = kind==null ? releases.authorize(code,version,identity) : releases.authorize(code,version,kind.equals("source")?"source":"oci",identity);
        var result = new LinkedHashMap<String,Object>();
        result.put("pluginCode",code); result.put("version",version); result.put("hostService",code);
        result.put("sourceLicense","Apache-2.0");
        result.put("release", Map.of("payload",release.payload(),"signature",release.signature(),"kid",release.kid()));
        if(kind==null || kind.equals("source"))result.put("source", downloads.issueDownloadUrl(code,version,"source",identity,null,null));
        if(kind==null || kind.equals("runtime"))result.put("runtime", downloads.issueDownloadUrl(code,version,"oci",identity,null,null));
        return result;
    }

    private Object submit(PluginIdentity identity,Map<String,Object> bundle) {
        Object response=agent.call(identity.getInstanceId(),"/tasks",bundle);
        if(!(response instanceof Map<?,?> task)) throw new IllegalStateException("安装代理返回格式无效");
        installations.submitted(identity.getInstanceId(),String.valueOf(bundle.get("pluginCode")),String.valueOf(bundle.get("version")),task);
        return response;
    }
    private static PluginIdentity identity() {
        var identity = PluginIdentityResolver.fromCurrentContext();
        if(identity==null || !identity.hasInstance())throw new DownloadDeniedException(3001,"未识别登录站点");
        return identity;
    }
    private static String text(Map<String,Object> request,String key) {
        Object value=request.get(key); if(value==null || value.toString().isBlank())throw new IllegalArgumentException(key+" 不能为空");
        return value.toString().trim();
    }
}
