// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'
import { h } from 'vue'

describe('payment redirect interaction with notfound retry', () => {
  it('does not infinitely redirect /payment/dashboard', async () => {
    const paymentRoutes = {
      path: '/payment',
      redirect: '/payment/dashboard',
      children: [
        { path: 'dashboard', name: 'PaymentDashboard', component: { render: () => h('div') }, meta: { title: '工作台' } },
        { path: 'trade', name: 'PaymentTradeMissing?', component: undefined }
      ]
    }
    // directory-only menus like /payment/trade have no page in some deployments
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', name: 'Layout', component: { render: () => h('router-view') }, children: [paymentRoutes] },
        { path: '/:pathMatch(.*)*', name: 'NotFound', component: { render: () => h('div') }, meta: { title: '页面不存在' } }
      ]
    })
    const log = []
    router.beforeEach((to, from, next) => {
      log.push(to.fullPath + ':' + String(to.name))
      if (log.length > 30) {
        next(false)
        return
      }
      next()
    })
    await router.push('/payment/dashboard')
    expect(router.currentRoute.value.name).toBe('PaymentDashboard')
    expect(log.length).toBeLessThan(5)

    await router.push('/payment/trade').catch(() => {})
    // trade has no component - what happens?
    console.log('trade route', router.currentRoute.value.name, router.currentRoute.value.matched.map(m => m.path))
    expect(log.length).toBeLessThan(20)
  })
})
