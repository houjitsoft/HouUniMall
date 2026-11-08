import LogAnalysis from './views/LogAnalysis.vue'
import LoginLogs from './views/LoginLogs.vue'
import OperationLogs from './views/OperationLogs.vue'
import AccessLogs from './views/AccessLogs.vue'
import BusinessLogs from './views/BusinessLogs.vue'
import ErrorLogs from './views/ErrorLogs.vue'
import SystemLogs from './views/SystemLogs.vue'
import LogSetting from './views/LogSetting.vue'
import { setPluginContext } from './context.js'

const routes = [
  ['/plugin/logging', LogAnalysis, 'Logging'],
  ['/plugin/logging/analysis', LogAnalysis, 'Analysis'],
  ['/plugin/logging/login', LoginLogs, 'Login Logs'],
  ['/plugin/logging/operation', OperationLogs, 'Operation Logs'],
  ['/plugin/logging/access', AccessLogs, 'Access Logs'],
  ['/plugin/logging/business', BusinessLogs, 'Business Logs'],
  ['/plugin/logging/error', ErrorLogs, 'Error Logs'],
  ['/plugin/logging/system', SystemLogs, 'System Logs'],
  ['/plugin/logging/setting', LogSetting, 'Settings']
]

export default {
  menus: [{ path: '/plugin/logging', title: '日志管理', icon: 'Document', sort: 3070 }],
  routes: routes.map(([path, component, title]) => ({
    path, component, meta: { title, pluginCode: 'logging' }
  })),
  setup(context) { setPluginContext(context) },
  teardown() { setPluginContext(null) }
}
