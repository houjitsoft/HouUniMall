// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'

vi.mock('@/api/setting/agreement', () => ({
  agreementApi: {
    pending: vi.fn().mockResolvedValue({
      code: 1,
      data: {
        pending: [
          {
            code: 'OPEN_SOURCE_EULA',
            name: '开源软件使用许可协议（EULA）',
            scene: 'install',
            required: 1,
            versionNo: 'v1.0.0'
          },
          {
            code: 'USER_SERVICE_AGREEMENT',
            name: '本站用户服务协议',
            scene: 'register',
            required: 1,
            versionNo: 'v2.0.0'
          }
        ]
      }
    }),
    reagree: vi.fn()
  }
}))

vi.mock('@/utils/showLegalAgreement', () => ({
  resolveAgreementContent: vi.fn().mockResolvedValue('协议正文'),
  resetAgreementCache: vi.fn()
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn() }
}))

import AgreementReagreeDialog from './AgreementReagreeDialog.vue'

const DialogStub = {
  props: ['modelValue'],
  template: '<section v-if="modelValue"><slot /><slot name="footer" /></section>'
}

describe('AgreementReagreeDialog', () => {
  it('shows site-operator agreements but never the installer EULA', async () => {
    const wrapper = mount(AgreementReagreeDialog, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-checkbox': { template: '<label><slot /></label>' },
          'el-link': { template: '<span><slot /></span>' },
          'el-tag': { template: '<span><slot /></span>' },
          'el-button': { template: '<button><slot /></button>' }
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('本站用户服务协议')
    expect(wrapper.text()).not.toContain('开源软件使用许可协议')
  })
})
