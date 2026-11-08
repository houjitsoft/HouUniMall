// 系统设置路由模块
export default {
  path: '/setting',
  meta: { title: '系统设置', icon: 'Setting' },
  children: [
    {
      path: '',
      redirect: '/setting/website'
    },
    // 权限管理路由
    {
      path: '/setting/permission/menu',
      name: 'MenuManagement',
      component: () => import('@/views/setting/MenuManagement.vue'),
      meta: { title: '菜单管理' }
    },
    {
      path: '/setting/permission/role',
      name: 'RoleManagement',
      component: () => import('@/views/setting/RoleManagement.vue'),
      meta: { title: '角色管理' }
    },
    {
      path: '/setting/permission/management',
      name: 'PermissionManagement',
      component: () => import('@/views/setting/PermissionManagement.vue'),
      meta: { title: '权限管理' }
    },
    {
      path: '/setting/permission/org/structure',
      name: 'OrgStructure',
      component: () => import('@/views/setting/OrgStructure.vue'),
      meta: { title: '公司信息' }
    },
    {
      path: '/setting/permission/org/department',
      name: 'DeptManagement',
      component: () => import('@/views/setting/DeptManagement.vue'),
      meta: { title: '部门管理' }
    },
    {
      path: '/setting/permission/org/post',
      name: 'JobManagement',
      component: () => import('@/views/setting/JobManagement.vue'),
      meta: { title: '岗位管理' }
    },
    {
      path: '/setting/permission/org/company',
      name: 'OrganizationManagement',
      component: () => import('@/views/setting/OrganizationManagement.vue'),
      meta: { title: '组织架构' }
    },
    
    // 工作流管理路由
    {
      path: '/setting/workflow/design',
      name: 'WorkflowDesign',
      component: () => import('@/views/setting/WorkflowDesign.vue'),
      meta: { title: '流程设计' }
    },
    {
      path: '/setting/workflow/instance',
      name: 'WorkflowInstance',
      component: () => import('@/views/setting/WorkflowInstance.vue'),
      meta: { title: '流程实例' }
    },
    {
      path: '/setting/workflow/monitor',
      name: 'WorkflowMonitor',
      component: () => import('@/views/setting/WorkflowMonitor.vue'),
      meta: { title: '流程监控' }
    },
    {
      path: '/setting/permission/user',
      name: 'UserPermissions',
      component: () => import('@/views/setting/UserPermissions.vue'),
      meta: { title: '用户管理' }
    },
    {
      path: '/setting/permission/member',
      name: 'MemberPermissionManagement',
      component: () => import('@/views/setting/MemberManagement.vue'),
      meta: { title: '会员管理' }
    },
    {
      path: '/setting/permission/employee',
      redirect: '/setting/permission/member'
    },
    // 网站设置路由
    {
      path: '/setting/website',
      name: 'WebsiteSettings',
      component: () => import('@/views/setting/WebsiteSettings.vue'),
      meta: { title: '网站设置' }
    },
    {
      path: '/setting/website/basic',
      name: 'WebsiteBasic',
      component: () => import('@/views/setting/WebsiteBasic.vue'),
      meta: { title: '基础配置' }
    },
    {
      path: '/setting/seo',
      name: 'SettingSeo',
      component: () => import('@/views/setting/SettingSeo.vue'),
      meta: { title: 'SEO配置' }
    },
    {
      path: '/setting/system',
      name: 'SettingSystem',
      component: () => import('@/views/setting/SettingSystem.vue'),
      meta: { title: '协议管理' }
    },
    {
      path: '/setting/website/seo',
      name: 'WebsiteSEO',
      component: () => import('@/views/setting/SettingSeo.vue'),
      meta: { title: 'SEO配置' }
    },
    {
      path: '/setting/website/theme',
      name: 'WebsiteTheme',
      component: () => import('@/views/setting/WebsiteTheme.vue'),
      meta: { title: '主题配置' }
    },
    {
      path: '/setting/website/email',
      name: 'WebsiteEmail',
      component: () => import('@/views/setting/WebsiteEmail.vue'),
      meta: { title: '邮件配置' }
    },
    {
      path: '/setting/website/timezone',
      name: 'WebsiteTimezone',
      component: () => import('@/views/setting/WebsiteTimezone.vue'),
      meta: { title: '时区配置' }
    },
    {
      path: '/setting/website/language',
      name: 'WebsiteLanguage',
      component: () => import('@/views/setting/WebsiteLanguage.vue'),
      meta: { title: '语言配置' }
    },
    {
      path: '/setting/system/queue',
      name: 'SystemQueueManagement',
      component: () => import('@/views/setting/QueueManagement.vue'),
      meta: { title: '队列管理' }
    },
    {
      path: '/setting/system/scheduled-task',
      name: 'ScheduledTaskManagement',
      component: () => import('@/views/setting/ScheduledTaskManagement.vue'),
      meta: { title: '定时任务管理' }
    },
    {
      path: '/setting/website/queue',
      redirect: '/setting/system/queue'
    },
    {
      path: '/setting/website/task',
      redirect: '/setting/system/scheduled-task'
    },
    {
      path: '/setting/international',
      name: 'InternationalSettings',
      component: () => import('@/views/setting/InternationalSettings.vue'),
      meta: { title: '国际化设置' }
    },
    {
      path: '/setting/ai-config',
      name: 'AiConfig',
      component: () => import('@/views/setting/AiConfig.vue'),
      meta: { title: 'AI配置' }
    },
    {
      path: '/setting/aiconfig',
      redirect: '/setting/ai-config'
    },
    {
      path: '/setting/aicostingbalance',
      name: 'AiCostingBalance',
      component: () => import('@/views/setting/AiCostingBalance.vue'),
      meta: { title: '用量费用' }
    },
    {
      path: '/setting/ai-gpt-usage',
      name: 'AiGptUsage',
      component: () => import('@/views/setting/AiGptUsage.vue'),
      meta: { title: 'AI 用量' }
    },
    {
      path: '/setting/ai-recharge-callback',
      name: 'AiRechargeCallback',
      component: () => import('@/views/setting/AiRechargeCallback.vue'),
      meta: { title: 'AI Token 充值结果', layout: 'blank' }
    },
    {
      path: '/setting/website/protocol',
      name: 'ProtocolManagement',
      component: () => import('@/views/setting/ProtocolManagement.vue'),
      meta: { title: '协议管理' }
    },
    {
      path: '/setting/sms',
      name: 'WebsiteSMS',
      component: () => import('@/views/setting/WebsiteSMS.vue'),
      meta: { title: '短信配置' }
    },
    // 协议管理（协议定义与版本，协议文案在此维护，便于后续运营调整）
    {
      path: '/setting/agreement',
      name: 'Agreement',
      component: () => import('@/views/setting/Agreement.vue'),
      meta: { title: '协议管理' }
    },
    // 协议同意记录（注册/版本更新时的用户同意留痕，供合规审计与举证）
    {
      path: '/setting/agreement-consent',
      name: 'AgreementConsent',
      component: () => import('@/views/setting/AgreementConsent.vue'),
      meta: { title: '协议同意记录' }
    },
    // 系统配置路由
    {
      path: '/setting/system/params',
      name: 'SystemParams',
      component: () => import('@/views/setting/SystemParams.vue'),
      meta: { title: '系统参数' }
    },
    {
      path: '/setting/system/info',
      name: 'SystemInfo',
      component: () => import('@/views/setting/SystemInfo.vue'),
      meta: { title: '系统信息' }
    },
    {
      path: '/setting/system/basic',
      name: 'SystemBasic',
      component: () => import('@/views/setting/SystemBasic.vue'),
      meta: { title: '基础配置' }
    },
    {
      path: '/setting/system/database',
      name: 'SystemDatabase',
      component: () => import('@/views/setting/SystemDatabase.vue'),
      meta: { title: '数据库管理' }
    },
    {
      path: '/setting/sql',
      name: 'DatabaseBasic',
      component: () => import('@/views/setting/DatabaseBasic.vue'),
      meta: { title: '基本信息' }
    },
    {
      path: '/setting/sqlexec',
      name: 'SqlExec',
      component: () => import('@/views/setting/SqlExec.vue'),
      meta: { title: '查询执行' }
    },
    {
      path: '/setting/system/storage',
      name: 'SystemStorage',
      component: () => import('@/views/setting/SystemStorage.vue'),
      meta: { title: '存储管理' }
    },
    {
      path: '/setting/system/service',
      name: 'SystemService',
      component: () => import('@/views/setting/SystemService.vue'),
      meta: { title: '服务管理' }
    },
    {
      path: '/setting/system/performance',
      name: 'SystemPerformance',
      component: () => import('@/views/setting/SystemPerformance.vue'),
      meta: { title: '性能监控' }
    },
    {
      path: '/setting/cache',
      name: 'SystemCache',
      component: () => import('@/views/setting/SystemCache.vue'),
      meta: { title: '缓存管理' }
    },
    {
      path: '/setting/cache/setting',
      name: 'CacheSetting',
      component: () => import('@/views/setting/CacheSetting.vue'),
      meta: { title: '缓存设置' }
    },
    {
      path: '/setting/cache/clear',
      name: 'CacheClear',
      component: () => import('@/views/setting/CacheClear.vue'),
      meta: { title: '缓存清理' }
    },
    {
      path: '/setting/cache/statistics',
      name: 'CacheStatistics',
      component: () => import('@/views/setting/CacheStatistics.vue'),
      meta: { title: '缓存统计' }
    },
    {
      path: '/setting/cache/list',
      name: 'CacheList',
      component: () => import('@/views/setting/CacheList.vue'),
      meta: { title: '缓存列表' }
    },
    {
      path: '/setting/log',
      redirect: '/setting/log/analysis'
    },
    // 添加日志管理的子路由
    {
      path: '/setting/log/analysis',
      name: 'LogAnalysis',
      component: () => import('@/views/setting/LogAnalysis.vue'),
      meta: { title: '日志分析' }
    },
    {
      path: '/setting/log/business',
      name: 'BusinessLogs',
      component: () => import('@/views/setting/BusinessLogs.vue'),
      meta: { title: '业务日志' }
    },
    {
      path: '/setting/log/operation',
      name: 'OperationLogs',
      component: () => import('@/views/setting/OperationLogs.vue'),
      meta: { title: '操作日志' }
    },
    {
      path: '/setting/log/login',
      name: 'LoginLogs',
      component: () => import('@/views/setting/LoginLogs.vue'),
      meta: { title: '登录日志' }
    },
    {
      path: '/setting/log/access',
      name: 'AccessLogs',
      component: () => import('@/views/setting/AccessLogs.vue'),
      meta: { title: '访问日志' }
    },
    {
      path: '/setting/log/error',
      name: 'ErrorLogs',
      component: () => import('@/views/setting/ErrorLogs.vue'),
      meta: { title: '错误日志' }
    },
    {
      path: '/setting/log/system',
      name: 'SystemLogsDetail',
      component: () => import('@/views/setting/SystemLogsDetail.vue'),
      meta: { title: '系统日志' }
    },
    {
      path: '/setting/log/setting',
      name: 'LogSetting',
      component: () => import('@/views/setting/LogSetting.vue'),
      meta: { title: '日志设置' }
    },
    {
      path: '/setting/backup',
      name: 'SystemBackup',
      component: () => import('@/views/setting/SystemBackup.vue'),
      meta: { title: '备份管理' }
    },
    {
      path: '/setting/backup/setting',
      name: 'BackupSetting',
      component: () => import('@/views/setting/BackupSetting.vue'),
      meta: { title: '备份设置' }
    },
    {
      path: '/setting/backup/schedule',
      name: 'BackupSchedule',
      component: () => import('@/views/setting/BackupSchedule.vue'),
      meta: { title: '备份计划' }
    },
    {
      path: '/setting/backup/restore',
      name: 'BackupRestore',
      component: () => import('@/views/setting/BackupRestore.vue'),
      meta: { title: '备份恢复' }
    },
    {
      path: '/setting/backup/config',
      name: 'BackupConfig',
      component: () => import('@/views/setting/BackupConfig.vue'),
      meta: { title: '备份配置' }
    },
    {
      path: '/setting/backup/file',
      name: 'BackupFile',
      component: () => import('@/views/setting/BackupFile.vue'),
      meta: { title: '备份文件' }
    },
    {
      path: '/setting/backup/data',
      name: 'BackupData',
      component: () => import('@/views/setting/BackupData.vue'),
      meta: { title: '数据备份' }
    },
    // 安全设置路由
    {
      path: '/setting/security/login',
      redirect: '/setting/log/login'
    },
    {
      path: '/setting/security/operation',
      redirect: '/setting/log/operation'
    },
    {
      path: '/setting/security/api',
      name: 'SecurityAPI',
      component: () => import('@/views/setting/SecurityAPI.vue'),
      meta: { title: 'API安全' }
    },
    {
      path: '/setting/security/encryption',
      name: 'SecurityEncryption',
      component: () => import('@/views/setting/SecurityEncryption.vue'),
      meta: { title: '数据加密' }
    },
    {
      path: '/setting/security/setting',
      name: 'SecuritySetting',
      component: () => import('@/views/setting/SecuritySetting.vue'),
      meta: { title: '安全设置' }
    },
    {
      path: '/setting/security/password',
      name: 'SecurityPassword',
      component: () => import('@/views/setting/SecurityPassword.vue'),
      meta: { title: '密码安全' }
    },
    {
      path: '/setting/security/ip',
      name: 'SecurityIP',
      component: () => import('@/views/setting/SecurityIP.vue'),
      meta: { title: 'IP安全' }
    },
    {
      path: '/setting/security/scan',
      name: 'SecurityScan',
      component: () => import('@/views/setting/SecurityScan.vue'),
      meta: { title: '安全扫描' }
    },
    // 任务管理路由
    {
      path: '/setting/task/setting',
      name: 'TaskSetting',
      component: () => import('@/views/setting/TaskSetting.vue'),
      meta: { title: '任务设置' }
    },
    {
      path: '/setting/task/log',
      name: 'TaskLog',
      component: () => import('@/views/setting/TaskLog.vue'),
      meta: { title: '任务日志' }
    },
    {
      path: '/setting/task/list',
      name: 'TaskList',
      component: () => import('@/views/setting/TaskList.vue'),
      meta: { title: '任务列表' }
    },
    {
      path: '/setting/task/monitor',
      name: 'TaskMonitor',
      component: () => import('@/views/setting/TaskMonitor.vue'),
      meta: { title: '任务监控' }
    },
    {
      path: '/setting/task/schedule',
      name: 'TaskSchedule',
      component: () => import('@/views/setting/TaskSchedule.vue'),
      meta: { title: '任务调度' }
    },
    // 监控管理路由
    {
      path: '/setting/monitor/setting',
      name: 'MonitorSetting',
      component: () => import('@/views/setting/MonitorSetting.vue'),
      meta: { title: '监控设置' }
    },
    {
      path: '/setting/monitor/alert',
      name: 'MonitorAlert',
      component: () => import('@/views/setting/MonitorAlert.vue'),
      meta: { title: '告警管理' }
    },
    {
      path: '/setting/monitor/performance',
      name: 'MonitorPerformance',
      component: () => import('@/views/setting/MonitorPerformance.vue'),
      meta: { title: '性能监控' }
    },
    {
      path: '/setting/monitor/service',
      name: 'MonitorService',
      component: () => import('@/views/setting/MonitorService.vue'),
      meta: { title: '服务监控' }
    },
    {
      path: '/setting/monitor/system',
      name: 'MonitorSystem',
      component: () => import('@/views/setting/MonitorSystem.vue'),
      meta: { title: '系统监控' }
    },
    // API管理路由
    {
      path: '/setting/api/setting',
      name: 'ApiSetting',
      component: () => import('@/views/setting/ApiSetting.vue'),
      meta: { title: 'API设置' }
    },
    {
      path: '/setting/api/interface',
      name: 'ApiInterface',
      component: () => import('@/views/setting/ApiInterface.vue'),
      meta: { title: '接口管理' }
    },
    {
      path: '/setting/api/document',
      name: 'ApiDocument',
      component: () => import('@/views/setting/ApiDocument.vue'),
      meta: { title: 'API文档' }
    },
    {
      path: '/setting/api/test',
      name: 'ApiTest',
      component: () => import('@/views/setting/ApiTest.vue'),
      meta: { title: 'API测试' }
    },
    {
      path: '/setting/api/statistics',
      name: 'ApiStatistics',
      component: () => import('@/views/setting/ApiStatistics.vue'),
      meta: { title: 'API统计' }
    },
    // 导入导出路由
    {
      path: '/导出配置',
      name: 'ExportConfig',
      component: () => import('@/views/setting/ExportConfig.vue'),
      meta: { title: '导出配置' }
    },
    {
      path: '/导入配置',
      name: 'ImportConfig',
      component: () => import('@/views/setting/ImportConfig.vue'),
      meta: { title: '导入配置' }
    },
    {
      path: '/setting/thridparty',
      name: 'ThirdPartySettings',
      component: () => import('@/views/setting/ThirdPartySettings.vue'),
      meta: { title: '第三方集成设置' }
    },
    {
      path: '/setting/express',
      name: 'ExpressManagement',
      component: () => import('@/views/setting/express/ExpressManagement.vue'),
      meta: { title: '快递管理' }
    },
    {
      path: '/setting/express/company',
      name: 'ExpressCompany',
      component: () => import('@/views/setting/express/ExpressCompany.vue'),
      meta: { title: '快递公司' }
    },
    {
      path: '/setting/express/config',
      name: 'ExpressConfig',
      component: () => import('@/views/setting/express/ExpressConfig.vue'),
      meta: { title: '快递设置' }
    },
    {
      path: '/setting/sensitive',
      name: 'SensitiveWord',
      component: () => import('@/views/setting/SensitiveWord.vue'),
      meta: { title: '敏感词' }
    },
    {
      path: '/setting/storage',
      name: 'StorageSettings',
      component: () => import('@/views/setting/SystemStorage.vue'),
      meta: { title: '存储配置' }
    },
    // 消息通知设置路由
    {
      path: '/setting/message/setting',
      name: 'MessageChannel',
      component: () => import('@/views/setting/message/MessageChannel.vue'),
      meta: { title: '消息设置' }
    },
    {
      path: '/setting/message/temlate',
      name: 'MessageTemplate',
      component: () => import('@/views/setting/message/MessageTemplate.vue'),
      meta: { title: '消息模板' }
    },
    {
      path: '/setting/message/record',
      name: 'MessageRecord',
      component: () => import('@/views/setting/message/MessageRecord.vue'),
      meta: { title: '发送记录' }
    }
  ]
}
