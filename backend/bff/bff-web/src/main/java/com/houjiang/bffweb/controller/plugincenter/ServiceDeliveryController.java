package com.houjiang.bffweb.controller.plugincenter;
import com.houjiang.bffweb.feign.PluginCenterServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/web/plugin-center/service")
public class ServiceDeliveryController {
    private final PluginCenterServiceFeign base;
    public ServiceDeliveryController(PluginCenterServiceFeign base) { this.base=base; }
    @PostMapping("/delivery") public ResponseResult delivery(@RequestBody Map<String,Object> request) {
        var result=base.serviceDelivery(request);
        if(result.getData() instanceof Map<?,?> bundle) for(String kind:java.util.List.of("source","runtime")) {
            if(bundle.get(kind) instanceof Map<?,?> raw && raw.get("url") instanceof String url
                    && url.startsWith("/base/plugin-center/artifact/raw?token="))
                ((Map<String,Object>)raw).put("url",url.replace("/base/plugin-center/","/web/plugin-center/"));
        }
        return result;
    }
    @PostMapping("/install") public ResponseResult install(@RequestBody Map<String,Object> request) { return base.serviceInstall(request); }
    @PostMapping("/upgrade") public ResponseResult upgrade(@RequestBody Map<String,Object> request) { return base.serviceUpgrade(request); }
    @PostMapping("/task/status") public ResponseResult status(@RequestBody Map<String,Object> request) { return base.serviceTaskStatus(request); }
    @PostMapping("/history") public ResponseResult history() { return base.serviceHistory(); }
}
