// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  request: {
    get: vi.fn(),
    post: vi.fn()
  },
  message: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

vi.mock('@/utils/request', () => ({ default: mocks.request }))
vi.mock('element-plus', () => ({ ElMessage: mocks.message }))
vi.mock('@element-plus/icons-vue', () => ({ Monitor: { template: '<span />' } }))

import WebsiteBasic from './WebsiteBasic.vue'

const passthrough = { template: '<div><slot /></div>' }
const formItem = {
  props: ['label'],
  template: '<div class="form-item"><span>{{ label }}</span><slot /></div>'
}
const switchStub = {
  props: ['modelValue'],
  emits: ['update:modelValue'],
  template: '<button class="switch" :data-value="String(modelValue)" @click="$emit(\'update:modelValue\', !modelValue)" />'
}
const buttonStub = {
  emits: ['click'],
  template: '<button class="button" @click="$emit(\'click\')"><slot /></button>'
}

const mountPage = () => mount(WebsiteBasic, {
  global: {
    stubs: {
      'el-card': passthrough,
      'el-form': passthrough,
      'el-form-item': formItem,
      'el-switch': switchStub,
      'el-button': buttonStub,
      'el-icon': passthrough
    }
  }
})

describe('WebsiteBasic AI assistant switch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    mocks.request.get.mockResolvedValue({
      code: 1,
      data: {
        internationalizationEnabled: true,
        multiOrganizationEnabled: true
      }
    })
    mocks.request.post.mockResolvedValue({ code: 1, data: {} })
  })

  it('shows the AI shopping assistant switch and defaults it to enabled when the config is absent', async () => {
    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('AI 导购入口')
    expect(wrapper.findAll('.switch')).toHaveLength(3)
    expect(wrapper.findAll('.switch')[2].attributes('data-value')).toBe('true')
  })

  it('saves the AI shopping assistant value with the other website switches', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.findAll('.switch')[2].trigger('click')
    await wrapper.findAll('.button')[0].trigger('click')
    await flushPromises()

    expect(mocks.request.post).toHaveBeenCalledWith('/web/base/sys-config/website-switches', {
      internationalizationEnabled: true,
      multiOrganizationEnabled: true,
      aiAssistantEnabled: false
    })
  })
})
