# Hello World：真实前端二开入口

本目录由现有 `registry.js` 与 `buildInputs.js` 自动发现，遵循 `src/extensions/{hostService}/{code}/` 约定。

- `manifest.js`：code=hello-world，hostService=base，默认 `enabled:false`。
- `index.js`：提供实际路由与 Vue 组件。
- `pages/HelloWorldPage.vue`：显示 Hello World，不调用接口、不写数据。

在 `frontend/admin` 目录执行 `VITE_SITE_EXTENSIONS=hello-world npm run build`。登录宿主后由扩展注册机制挂载 `/ext/hello-world`，访问仍受宿主路由权限约束；按站点实际角色授予对应路径权限，不能通过示例绕过鉴权。

构建产物为 `dist/assets/extensions/ext_hello-world-*.js`。本例默认不注册菜单，不覆盖官方页面，也不自动发布到生产。后台构建通过不等于已完成真实登录态页面访问验收。
