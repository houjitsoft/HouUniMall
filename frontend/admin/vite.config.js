import { fileURLToPath, URL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs'


import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
// import vueDevTools from 'vite-plugin-vue-devtools'

import {
  SITE_EXTENSION_ASSET_DIR,
  collectSiteExtensionInputs,
  isSiteExtensionEntryName
} from './src/extensions/buildInputs.js'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const repoPublicTemplates = path.resolve(fileURLToPath(new URL('.', import.meta.url)), '../../public/templates')
  const siteExtensionsDir = fileURLToPath(new URL('./src/extensions', import.meta.url))
  // 站点级二开：把 src/extensions/{hostService}/{code}/index.js 登记为独立构建输入（独立 chunk）。
  // 官方 index.html 入口（name=main）保持不动，官方 src/views 与路由不被覆盖。
  const siteExtensionInputs = collectSiteExtensionInputs(siteExtensionsDir)

  const hostAssets = { revisions: {} }
  return {
    define: { __HOST_PLUGIN_FRONTEND_REVISIONS__: JSON.stringify(hostAssets.revisions) },
    plugins: [
      // UnoCSS 需在 vue() 之前注册，保证 SFC style 里的 @apply 被正确处理
      UnoCSS(),
      vue(),

      // vueDevTools(), // 暂时禁用，避免 Node.js 版本兼容问题
      {
        name: 'serve-repo-public-templates',
        configureServer(server) {
          server.middlewares.use('/templates', (req, res, next) => {
            const rawPath = decodeURIComponent((req.url || '/').split('?')[0])
            const relative = rawPath.replace(/^\/+/, '')
            if (!relative || relative.includes('..')) {
              res.statusCode = 400
              res.end('Bad Request')
              return
            }
            const filePath = path.join(repoPublicTemplates, relative)
            if (!filePath.startsWith(repoPublicTemplates)) {
              res.statusCode = 403
              res.end('Forbidden')
              return
            }
            fs.readFile(filePath, (err, data) => {
              if (err) {
                next()
                return
              }
              if (filePath.endsWith('.xlsx')) {
                res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
              } else if (filePath.endsWith('.csv')) {
                res.setHeader('Content-Type', 'text/csv; charset=utf-8')
              }
              res.end(data)
            })
          })
        }
      }
    ],
    resolve: {
      dedupe: ['vue'],
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '@decoration': fileURLToPath(new URL('../uniapp/src/components', import.meta.url)),
        // 站点级二开扩展目录别名（构建期静态装配通道，见 src/extensions/README.md）
        '@ext': fileURLToPath(new URL('./src/extensions', import.meta.url))
      },
    },
    optimizeDeps: {
      include: ['vue', 'vue-router', 'pinia', 'element-plus']
    },
    test: {
      // tests/*.test.mjs 使用 Node 原生 test runner，由 npm test 流程单独执行；
      // 从 Vitest 中排除，避免被当作“无 test suite”而误报失败。
      exclude: ['node_modules/**', 'dist/**', 'tests/**/*.test.mjs']
    },
    build: {
      // 2 GB 发布机避免在全部 chunk 常驻时并发计算 gzip 大小导致 OOM。
      reportCompressedSize: false,
      // 输出到本端 dist 目录（deploy/Dockerfile.frontend 的 OUT_DIR 与此对齐）
      outDir: 'dist',
      // 允许清理项目根目录外的输出目录
      emptyOutDir: true,
      // 确保JSON文件被正确处理
      assetsInlineLimit: 0,
      rollupOptions: {
        // 明确指定入口点，避免扫描测试HTML文件
        input: {
          main: fileURLToPath(new URL('./index.html', import.meta.url)),
          // 站点级二开扩展：每个 {code} 一个独立入口 → 独立 chunk
          ...siteExtensionInputs
        },
        // 应用构建默认 false 会把「无引用」的扩展入口摇成空 chunk；
        // 站点级扩展入口需要保留其 default 导出（宿主运行期装载），故设为 exports-only。
        preserveEntrySignatures: 'exports-only',
        output: {
          // 确保打包后的文件结构清晰
          manualChunks: {
            'vendor': ['vue', 'vue-router', 'pinia'],
            'element-plus': ['element-plus', '@element-plus/icons-vue'],
            'i18n': ['vue-i18n']
          },
          // 站点级扩展 chunk 与官方入口分目录存放，便于识别与回归比对
          entryFileNames: (chunkInfo) =>
            isSiteExtensionEntryName(chunkInfo.name)
              ? `${SITE_EXTENSION_ASSET_DIR}/[name]-[hash].js`
              : 'assets/[name]-[hash].js',
          chunkFileNames: 'assets/[name]-[hash].js',
          // 确保locale文件被正确打包
          assetFileNames: (assetInfo) => {
            if (assetInfo.name && assetInfo.name.endsWith('.json')) {
              return 'assets/locales/[name]-[hash][extname]'
            }
            return 'assets/[name]-[hash][extname]'
          }
        }
      }
    },
    server: {
      port: 13001,
      host: '127.0.0.1',
      // 允许的域名列表（用于开发环境直接请求外部API）
      allowedHosts: ['localhost', '127.0.0.1'],
      // 开发环境下将 /web 代理到后端服务
      proxy: {
        '/web': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:8391',
          changeOrigin: true,
        }
      },
      // HMR 配置
      // 禁用 HMR（因为 WebSocket 不可用）
      // 如果需要热更新，请配置支持 WebSocket 的反向代理
      hmr: false,
      // 严格端口检查（禁用，允许通过代理访问）
      strictPort: false,
    },
  }
})
