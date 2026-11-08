package com.houjiang.merchant.controller.monitor;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.ServerResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("adminapi/setting.system.system")
@Tag(name = "监控服务管理")
public class MonitorServerController {

    /**
     * 服务器信息
     *
     * @author Amos
     * @return AjaxResult<Map<String, Object>>
     */
    @Log(title = "服务监控")
    @GetMapping("/info")
    @Operation(summary="服务监控")
    public AjaxResult<Map<String, Object>> info() {
        ServerResult server = new ServerResult();
        return AjaxResult.success(server.copyTo());
    }

}
