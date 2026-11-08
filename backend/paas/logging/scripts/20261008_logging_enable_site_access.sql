-- 日志管理(logging) 站点授权与安装回执补齐（顶栏运行告警 unread-count 依赖产品闸）。
INSERT INTO hou_service_installation(instance_id,plugin_code,version,image_id,source_sha256,runtime_sha256,status,task_sequence,installed_at,updated_at)
SELECT 'u-640697115455213568','logging','1.0.0','unimall-logging:latest','-','-',1,1,NOW(),NOW()
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM hou_service_installation WHERE instance_id='u-640697115455213568' AND plugin_code='logging'
);
INSERT INTO hou_market_plugin_license(plugin_code,buyer_id,instance_id,license_key,license_type,max_instances,expire_time,status,create_time,state,bind_time,purchase_sandbox)
SELECT 'logging',1,'u-640697115455213568','HOU-LOGGING-640697115455213568','PAID',1,NULL,1,NOW(),'ACTIVATED',NOW(),0
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM hou_market_plugin_license WHERE plugin_code='logging' AND instance_id='u-640697115455213568'
);
