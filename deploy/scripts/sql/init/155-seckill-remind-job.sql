-- 155 限时秒杀「开抢前预约」提醒：消息模板种子 + job 调度注册（order-seckill 插件配套核心改动）
-- 依赖：hou_base_message_template（base 消息模板系统，列以 BaseMessageTemplateMapper.xml 为准）、
--       hou_job_platform（job 服务 Quartz 调度表，ScheduledStarted 启动时按 status=0 注册）。
-- 幂等：模板按 template_code 判存、job 按 invoke_target 判存。
-- 说明：hou_job_platform 新增行后需重启 job 服务才会注册调度（ScheduledStarted 是 CommandLineRunner）；
--       也可在「定时任务」后台手动新增同名任务，invoke_target 与本脚本一致。
--
-- 【短信开关】当前默认 sms_status=0（只发站内信），因为短信需先在短信服务商申请并审核通过
--   「秒杀开抢提醒」模板，拿到模板号写入 sms_template_id 后才能启用；未配置模板号时若把
--   sms_status 置 1，sendMessageTemplate 的短信分支会失败并把整条提醒判为「提醒失败」。
--   待服务商模板就绪后，执行本文件底部「启用短信」两条 UPDATE（填模板号 + 打开开关）即可。

-- 1) 秒杀开抢提醒消息模板（id=28，对应 CommonType.messageTemplate.CODE_SECKILL_REMIND）
--    template_type=1 会员消息（站内信按 userId 投递）；占位符 {1}=商品名 {2}=开抢时间。
INSERT INTO `hou_base_message_template`
    (`id`, `template_code`, `template_name`, `template_type`,
     `mail_status`, `mail_template_content`,
     `sms_status`, `sms_template_content`, `sms_template_id`,
     `wx_status`, `email_status`)
SELECT 28, 'seckill_open_remind', '秒杀开抢提醒', 1,
       1, '您预约的秒杀商品「{1}」将于 {2} 开抢，手慢无，快去抢购吧！',
       0, '您预约的秒杀「{1}」将于{2}开抢，点击抢购', '',
       0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `hou_base_message_template` WHERE `template_code` = 'seckill_open_remind'
);

-- 2) 注册 job 调度（每分钟扫描一次，开抢前 5 分钟内的预约发提醒）
INSERT INTO `hou_job_platform`
    (`job_name`, `job_group`, `invoke_target`, `cron_expression`, `status`, `create_time`, `remark`)
SELECT '秒杀开抢预约提醒', 'DEFAULT', 'com.houjiang.job.job.SeckillSubscribeRemindJob',
       '0 * * * * ?', '0', NOW(), '开抢前5分钟向预约用户发站内信（+短信）提醒'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `hou_job_platform` WHERE `invoke_target` = 'com.houjiang.job.job.SeckillSubscribeRemindJob'
);

-- ==== 短信服务商模板就绪后启用短信（把 ? 换成服务商审核通过的模板号）====
-- UPDATE `hou_base_message_template` SET `sms_template_id` = 'SMS_XXXXXXXXX' WHERE `template_code` = 'seckill_open_remind';
-- UPDATE `hou_base_message_template` SET `sms_status` = 1 WHERE `template_code` = 'seckill_open_remind';
