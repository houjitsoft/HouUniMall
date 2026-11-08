// 默认不启用；用 VITE_SITE_EXTENSIONS=hello-world 构建时开启。
export default {
  code: 'hello-world', hostService: 'base', name: 'Hello World 二开', version: '1.0.0', enabled: false,
  routes: [{ path: '/ext/hello-world', title: 'Hello World' }]
}
