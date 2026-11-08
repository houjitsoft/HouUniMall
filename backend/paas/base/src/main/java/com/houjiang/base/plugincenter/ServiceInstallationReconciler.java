package com.houjiang.base.plugincenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;

/** Installation completes even when the purchaser closes the browser. */
@Slf4j
@Component
public class ServiceInstallationReconciler {
    private final ServiceInstallerClient installer;
    private final ServiceInstallationService installations;
    public ServiceInstallationReconciler(ServiceInstallerClient installer,ServiceInstallationService installations) {
        this.installer=installer;this.installations=installations;
    }
    @Scheduled(fixedDelayString="${unimall.installer.reconcile-delay-ms:15000}")
    public void reconcile() {
        String instance=installer.configuredInstance();
        if(instance.isBlank()) return;
        try {
            for(var task:installations.pending(instance)) {
                String id=String.valueOf(task.get("task_id"));
                try {
                    Object receipt=installer.call(instance,"/tasks/"+id,null);
                    if(receipt instanceof Map<?,?> result) installations.reconcile(instance,id,result);
                } catch(RuntimeException error) { log.warn("独立服务安装任务待重新核对: task={}",id); }
            }
        } catch(RuntimeException error) { log.warn("独立服务安装状态扫描未完成，请检查迁移与安装代理配置"); }
    }
}
