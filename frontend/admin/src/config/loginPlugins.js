/**
 * 登录期插件白名单
 *
 * 登录页处于「未登录」状态，拿不到租户已安装插件列表（该接口需要 token），
 * 因此声明了登录方式的插件需要在登录期单独装载一次。
 *
 * 这里列出允许在登录页注入入口的插件（code + version）。产物由前端静态托管：
 *   /plugin-assets/{code}/{version}/index.mjs
 *
 * 后续若后端提供匿名的「公开登录方式清单」接口（/web/plugin/public/login-providers），
 * 应改为优先读接口、本白名单降级为兜底（见 utils/loginPluginLoader.js）。
 */
// [OSS 裁剪] 开源包不随附 unilogin 前端产物（dist/plugin-assets 下只有 demo），
// 而登录页会无条件 import `/plugin-assets/{code}/{version}/index.mjs`——声明了却没产物，
// 每次打开登录页都被 SPA 回退成 index.html，控制台刷
// 「Failed to load module script ... MIME type text/html」（2026-10-28 开源评估线实锤）。
// 没随包交付的插件就不要在白名单里声明；将来随包发布时把这一项加回来即可。
// 主仓保留 unilogin（生产静态目录里有该产物）。
export const LOGIN_STAGE_PLUGINS = []

export default LOGIN_STAGE_PLUGINS
