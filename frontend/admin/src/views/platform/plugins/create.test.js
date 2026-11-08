// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus, { ElMessage } from 'element-plus'
import { createRouter, createMemoryHistory } from 'vue-router'
import PluginCreate from './create.vue'
import { createPlugin } from '@/api/plugin-center/pluginCenter'

vi.mock('@/api/plugin-center/pluginCenter', () => ({
  createPlugin: vi.fn(),
  getPluginCategories: vi.fn(async () => [{ id: 87, name: '客户扩展分类' }])
}))

const makeRouter = async () => {
  const stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: stub },
      { path: '/platform/plugins', component: stub }
    ]
  })
  await router.push('/')
  await router.isReady()
  return router
}

const mountPage = async () => {
  const router = await makeRouter()
  const wrapper = mount(PluginCreate, { global: { plugins: [ElementPlus, router] } })
  await flushPromises()
  return wrapper
}

/** 模拟从插件中心形态选择弹窗带 runtime 跳转进入向导 */
const mountPageWithRuntime = async (runtime) => {
  const stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: stub },
      { path: '/platform/plugins', component: stub },
      { path: '/platform/plugins/create', component: stub }
    ]
  })
  await router.push({ path: '/platform/plugins/create', query: { runtime } })
  await router.isReady()
  const wrapper = mount(PluginCreate, { global: { plugins: [ElementPlus, router] } })
  await flushPromises()
  return wrapper
}

const findButton = (wrapper, text) =>
  wrapper.findAll('button').find((b) => b.text().includes(text))

/** 真实点击流走完前两步，进入「确认生成」 */
const walkToConfirm = async (wrapper) => {
  wrapper.vm.form.hostService = 'oa'
  wrapper.vm.form.cateId = 87
  await wrapper.find('.code-input input').setValue('visitor')
  await wrapper.find('input[placeholder="如：访客管理"]').setValue('访客管理')
  await findButton(wrapper, '下一步').trigger('click')
  await flushPromises()
  await findButton(wrapper, '下一步').trigger('click')
  await flushPromises()
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('创建插件向导页', () => {
  it('第一步渲染基础信息表单且默认值正确', async () => {
    const wrapper = await mountPage()
    expect(wrapper.text()).toContain('创建插件')
    expect(wrapper.text()).toContain('插件编码')
    expect(wrapper.text()).toContain('授权类型')
    const versionInput = wrapper.find('input[placeholder="1.0.0"]')
    expect(versionInput.element.value).toBe('1.0.0')
    // 分类必须由用户从服务端字典选择，不再默认为 Business
    const select = wrapper.findAll('.el-select').at(1)
    expect(select.text()).toContain('选择插件分类')
    expect(wrapper.vm.form.cateId).toBeNull()
    expect(wrapper.vm.categories).toEqual([{ id: 87, name: '客户扩展分类' }])
  })

  it('未传入形态时默认 standalone，归属已有服务的产品描述', async () => {
    const wrapper = await mountPage()
    expect(wrapper.vm.pluginRuntime).toBe('standalone')
    expect(wrapper.text()).toContain('随服务编译发布')
    expect(wrapper.text()).toContain('单独构建 JAR 由宿主加载')
    expect(wrapper.text()).toContain('两者都不新建服务、进程或端口')
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    wrapper.vm.form.code = 'visitor'
    wrapper.vm.step = 2
    await flushPromises()
    expect(wrapper.text()).toContain('plugins/oa/visitor/')
    expect(wrapper.text()).toContain('runtime: standalone')
    expect(wrapper.text()).not.toContain('type:')
  })

  it('弹窗选中的形态经 query 传入，并写入提交参数', async () => {
    createPlugin.mockResolvedValue({ code: 200, data: { code: 'visitor', serviceDir: '/w/plugins/oa/visitor', files: [], nextSteps: [] } })
    const wrapper = await mountPageWithRuntime('embedded')
    expect(wrapper.vm.pluginRuntime).toBe('embedded')

    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    await wrapper.find('.code-input input').setValue('visitor')
    await wrapper.find('input[placeholder="如：访客管理"]').setValue('访客管理')
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()

    expect(createPlugin).toHaveBeenLastCalledWith(expect.objectContaining({ runtime: 'embedded', hostService: 'oa' }))
  })

  it('不选择分类或提交未知分类 ID 时不能进入下一步', async () => {
    const wrapper = await mountPage()
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.code = 'visitor'
    wrapper.vm.form.name = '访客管理'
    for (const cateId of [null, 999]) {
      wrapper.vm.form.cateId = cateId
      await findButton(wrapper, '下一步').trigger('click')
      expect(wrapper.vm.step).toBe(0)
    }
  })

  it('非法编码被前端拦截且不进入下一步', async () => {
    const wrapper = await mountPage()
    const warnSpy = vi.spyOn(ElMessage, 'warning').mockImplementation(() => {})

    wrapper.vm.form.cateId = 87
    wrapper.vm.form.hostService = 'oa'
    const codeInput = wrapper.find('.code-input input')
    await codeInput.setValue('Visitor')
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()

    expect(warnSpy).toHaveBeenCalled()
    expect(wrapper.vm.step).toBe(0)
    warnSpy.mockRestore()
  })

  it('保留编码（与现有插件冲突）被拦截', async () => {
    const wrapper = await mountPage()
    const warnSpy = vi.spyOn(ElMessage, 'warning').mockImplementation(() => {})

    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    await wrapper.find('.code-input input').setValue('erp')
    await wrapper.find('.el-input').findAll('input') // noop keep order
    const nameInputs = wrapper.find('input[placeholder="如：访客管理"]')
    await nameInputs.setValue('重复编码')
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()

    expect(warnSpy).toHaveBeenCalled()
    expect(wrapper.vm.step).toBe(0)
    warnSpy.mockRestore()
  })

  it('合法编码可流转到服务与契约步骤', async () => {
    const wrapper = await mountPage()

    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    await wrapper.find('.code-input input').setValue('visitor-log')
    await wrapper.find('input[placeholder="如：访客管理"]').setValue('访客流水')
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()

    expect(wrapper.vm.step).toBe(1)
    expect(wrapper.text()).toContain('菜单路径')
    expect(wrapper.text()).toContain('适用端')
  })

  it('第三步展示将生成的文件清单与关键契约', async () => {
    const wrapper = await mountPage()
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    wrapper.vm.form.code = 'visitor'
    wrapper.vm.form.name = '访客管理'
    wrapper.vm.step = 2
    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('oa/visitor/')
    expect(text).not.toContain('pom.xml')
    expect(text).toContain('plugin.yaml')
    expect(text).not.toContain('deploy/Dockerfile')
    expect(text).toContain('hou_plugin_visitor_')
    expect(text).toContain('runtime: standalone')
    expect(wrapper.findAll('.file-tree li').length).toBe(4)
  })

  it('embedded 形态预览 runtime/java 源码与 pom，且不出现 type 字段', async () => {
    const wrapper = await mountPageWithRuntime('embedded')
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    wrapper.vm.form.code = 'visitor-log'
    wrapper.vm.form.name = '访客流水'
    wrapper.vm.step = 2
    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('runtime: embedded')
    expect(text).toContain('runtime/java/com/houjiang/plugin/visitor_log/VisitorLogPlugin.java')
    expect(text).toContain('pom.xml')
    expect(text).not.toContain('src/main/java')
    expect(text).not.toContain('type:')
  })

  it('提交成功后展示工程目录、文件数与下一步指引', async () => {
    createPlugin.mockResolvedValue({
      code: 200,
      data: {
        code: 'visitor',
        serviceDir: '/workspace/plugins/oa/visitor',
        files: ['pom.xml', 'plugin.yaml'],
        nextSteps: ['实现业务逻辑', '回填 routes']
      }
    })
    const wrapper = await mountPage()
    await walkToConfirm(wrapper)
    expect(wrapper.text()).toContain('将创建的工程')
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()

    expect(createPlugin).toHaveBeenCalledWith(expect.objectContaining({
      code: 'visitor',
      hostService: 'oa',
      menuPath: '/plugin/visitor',
      cateId: 87,
      category: '客户扩展分类'
    }))
    const text = wrapper.text()
    expect(text).toContain('插件 visitor 已创建')
    expect(text).toContain('/workspace/plugins/oa/visitor')
    expect(text).toContain('共 2 个文件')
    expect(text).toContain('实现业务逻辑')
  })

  it('提交失败时透传后端文案（编码冲突等）', async () => {
    createPlugin.mockRejectedValue({
      response: { data: { code: 409, message: '插件编码已存在于插件中心目录: visitor' } }
    })
    const errorSpy = vi.spyOn(ElMessage, 'error').mockImplementation(() => {})
    const wrapper = await mountPage()
    await walkToConfirm(wrapper)
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()

    expect(errorSpy).toHaveBeenCalledWith('插件编码已存在于插件中心目录: visitor')
    expect(wrapper.vm.created).toBeNull()
    errorSpy.mockRestore()
  })

  it('PAID 授权时提交价格按分换算且 FREE 时为 0', async () => {
    createPlugin.mockResolvedValue({ code: 200, data: { code: 'visitor', files: [], nextSteps: [] } })
    const wrapper = await mountPage()
    // PAID：步骤 1 切换授权类型并填价格
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    await wrapper.find('.code-input input').setValue('visitor')
    await wrapper.find('input[placeholder="如：访客管理"]').setValue('访客管理')
    await wrapper.findAll('.el-radio-button').find((b) => b.text().includes('付费')).find('input').setValue('PAID')
    await flushPromises()
    await wrapper.find('.el-input-number input').setValue('990')
    await walkToConfirm(wrapper)
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()
    expect(createPlugin).toHaveBeenLastCalledWith(expect.objectContaining({
      licenseType: 'PAID', priceCents: 99000
    }))

    // FREE：重新走流程
    await findButton(wrapper, '再创建一个').trigger('click')
    await flushPromises()
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    await wrapper.find('.code-input input').setValue('visitor')
    await wrapper.find('input[placeholder="如：访客管理"]').setValue('访客管理')
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()
    await findButton(wrapper, '下一步').trigger('click')
    await flushPromises()
    await findButton(wrapper, '创建插件').trigger('click')
    await flushPromises()
    expect(createPlugin).toHaveBeenLastCalledWith(expect.objectContaining({
      licenseType: 'FREE', priceCents: 0
    }))
  })
  it('归属服务必填且拒绝新增业务服务', async () => {
    const wrapper = await mountPage()
    wrapper.vm.form.code = 'visitor'
    wrapper.vm.form.name = '访客管理'
    wrapper.vm.form.cateId = 87
    for (const host of ['', 'mall', 'visitor']) {
      wrapper.vm.form.hostService = host
      await findButton(wrapper, '下一步').trigger('click')
      expect(wrapper.vm.step).toBe(0)
    }
    expect(createPlugin).not.toHaveBeenCalled()
  })

  it('embedded 预览生成扩展 JAR，使用同一服务目录', async () => {
    const wrapper = await mountPageWithRuntime('embedded')
    wrapper.vm.form.hostService = 'oa'
    wrapper.vm.form.cateId = 87
    wrapper.vm.form.code = 'visitor'
    wrapper.vm.step = 2
    await flushPromises()
    expect(wrapper.text()).toContain('plugins/oa/visitor/')
    expect(wrapper.text()).toContain('VisitorPlugin.java')
    expect(wrapper.text()).not.toContain('Application.java')
    expect(wrapper.findAll('.file-tree li').length).toBe(6)
  })

})
