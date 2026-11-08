import { defineConfig, presetWind3 } from 'unocss'

/**
 * 装修组件（components/widgets/**）的模板与样式大量使用 Tailwind/Uno 风格的工具类
 * （flex / items-center / justify-center / text-[#999] / w-[100px] / @apply bg-primary ...）。
 * 这些类此前没有任何 CSS 引擎承接，全部静默失效，导致编辑器画布里的组件预览布局错乱。
 *
 * 这里接入 UnoCSS（preset-wind3），并把语义色 primary 指向 Element Plus 的主题变量，
 * 使 bg-primary / text-primary / border-primary 随主题色联动。
 */
export default defineConfig({
  presets: [
    presetWind3()
  ],
  theme: {
    colors: {
      primary: 'var(--el-color-primary)'
    }
  }
})
