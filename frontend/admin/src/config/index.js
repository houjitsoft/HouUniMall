/**
 * 环境配置文件
 * 统一管理所有环境变量配置
 * 通过import.meta.env访问环境变量
 * 
 * 架构说明：
 * - 前端应用通过环境变量配置不同的API地址
 * - 开发环境使用外部API，生产环境使用内部API
 * 
 * @author Amos
 * @date 2024-01-01
 */

const config = {
  // 应用基础配置
  APP_TITLE: import.meta.env.VITE_APP_TITLE || 'HouUniMall 管理后台',
  APP_PORT: import.meta.env.VITE_APP_PORT || 13001,

  // API基础URL配置 - 直接请求后端接口，不使用代理
  BASE_API: import.meta.env.VITE_APP_BASE_API || 'http://x991.shangliansoft.cn:18080',
  
  // 开发环境配置
  DEV_MODE: import.meta.env.DEV,
  PROD_MODE: import.meta.env.PROD,
}

// 导出配置
export default config

// 导出常用的配置项
export const {
  APP_TITLE,
  APP_PORT,
  BASE_API,
  DEV_MODE,
  PROD_MODE
} = config