// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  push: vi.fn(),
  request: {
    post: vi.fn(),
    get: vi.fn()
  },
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
}))

vi.mock('vue-router', () => ({ useRouter: () => ({ push: mocks.push }) }))
vi.mock('@/utils/request', () => ({ default: mocks.request }))
vi.mock('@/i18n-element', () => ({ useI18n: () => ({ t: (value) => value }) }))
vi.mock('element-plus', () => ({
  ElMessage: mocks.message,
  ElMessageBox: { prompt: vi.fn(), confirm: vi.fn() }
}))
vi.mock('@element-plus/icons-vue', () => {
  const icon = { template: '<span />' }
  return { Plus: icon, Edit: icon, Delete: icon, Message: icon, Link: icon, Setting: icon, Tools: icon }
})

import WebsiteSMS from './WebsiteSMS.vue'

const passthrough = { template: '<div v-bind="$attrs"><slot /></div>' }
const form = {
  template: '<form v-bind="$attrs"><slot /></form>',
  methods: { resetFields: vi.fn() }
}
const button = {
  emits: ['click'],
  template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'
}

const mountPage = () => mount(WebsiteSMS, {
  global: {
    stubs: {
      'el-card': passthrough,
      'el-table': passthrough,
      'el-table-column': { template: '<div />' },
      'el-tag': passthrough,
      'el-switch': true,
      'el-button': button,
      'el-icon': passthrough,
      'el-dialog': passthrough,
      'el-form': form,
      'el-form-item': passthrough,
      'el-select': true,
      'el-option': true,
      'el-input': true,
      'el-input-number': true,
      'el-link': button
    }
  }
})

describe('WebsiteSMS', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.request.post.mockResolvedValue({ code: 1, data: [] })
    mocks.request.get.mockResolvedValue({ code: 1, data: {} })
    vi.spyOn(window, 'open').mockReturnValue({ closed: false })
    sessionStorage.clear()
  })

  it('opens template settings without closing the edit dialog', async () => {
    const wrapper = mountPage()
    await flushPromises()
    wrapper.vm.editDialogVisible = true

    await wrapper.vm.goToMessageTemplate()

    expect(window.open).toHaveBeenCalledWith('/setting/message/temlate', '_blank')
    expect(wrapper.vm.editDialogVisible).toBe(true)
    expect(mocks.push).not.toHaveBeenCalled()
  })

  it('does not render or require a region field for aliyun', async () => {
    const wrapper = mountPage()
    await flushPromises()
    wrapper.vm.editForm.smsType = 'aliyun'
    wrapper.vm.editForm.smsSign = '测试签名'
    wrapper.vm.editForm.smsSecretId = 'secret-id'
    wrapper.vm.editForm.smsSecretKey = 'secret-key'
    wrapper.vm.editForm.smsGeneral = 'SMS_001'
    wrapper.vm.editForm.smsRegion = ''
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).not.toContain('地域')

    await wrapper.vm.saveSmsConfig()

    expect(mocks.request.post).toHaveBeenCalledWith('/web/base/sms/updateSmsConfigMap', expect.not.objectContaining({ smsRegion: expect.anything() }))
    expect(mocks.message.error).not.toHaveBeenCalledWith('请选择地域')
  })

  it('stores the form draft before falling back to the current page', async () => {
    window.open.mockReturnValue(null)
    const wrapper = mountPage()
    await flushPromises()
    wrapper.vm.editDialogTitle = '编辑短信配置'
    wrapper.vm.editForm.smsType = 'aliyun'
    wrapper.vm.editForm.smsSign = '测试签名'
    wrapper.vm.editDialogVisible = true

    await wrapper.vm.goToMessageTemplate()

    expect(mocks.push).toHaveBeenCalledWith('/setting/message/temlate')
    expect(JSON.parse(sessionStorage.getItem('website-sms-edit-draft'))).toMatchObject({
      title: '编辑短信配置',
      form: { smsType: 'aliyun', smsSign: '测试签名' }
    })
    expect(mocks.message.warning).toHaveBeenCalled()
  })
})
