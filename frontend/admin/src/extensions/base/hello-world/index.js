import HelloWorldPage from './pages/HelloWorldPage.vue'

// registry.js 发现本目录；组件对象直接交给宿主包装，无需自建动态装载器。
export default {
  code: 'hello-world', hostService: 'base', name: 'Hello World 二开', version: '1.0.0',
  routes: [{ path: '/ext/hello-world', name: 'site_hello_world_index', component: HelloWorldPage, title: 'Hello World' }],
  menus: []
}
