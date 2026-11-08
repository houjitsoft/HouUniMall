// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { defineComponent, markRaw, reactive } from 'vue'
import source from './HeaderDailyTodo.vue?raw'

const fixture = vi.hoisted(() => ({ dashboard: null, refresh: vi.fn() }))
vi.mock('@/stores/dashboard', () => ({ useDashboardStore: () => fixture.dashboard }))
import HeaderDailyTodo from './HeaderDailyTodo.vue'

const panel = markRaw(defineComponent({
  setup(_, { expose }) { expose({ refresh: fixture.refresh }) },
  template: '<input class="todo-test-draft" />'
}))
const mountEntry = () => mount(HeaderDailyTodo, {
  global: { stubs: {
    'el-tooltip': { template: '<div><slot /></div>' },
    'el-icon': { template: '<span><slot /></span>' },
    'el-button': { template: '<button><slot /></button>' },
    'el-dialog': {
      props: ['modelValue'], emits: ['open', 'update:modelValue'],
      watch: { modelValue(value) { if (value) this.$emit('open') } },
      template: '<section v-show="modelValue" role="dialog"><button class="close" @click="$emit(\'update:modelValue\', false)">关闭</button><slot /></section>'
    }
  } }
})

describe('顶栏待办布局契约', () => {
  it('限制真实弹窗宽高并通过宿主 deep 样式保持桌面两栏比例', () => {
    expect(source).toContain('width="min(1100px, calc(100vw - 32px))"')
    expect(source).toContain('height: min(600px, calc(90dvh - 148px))')
    expect(source).toMatch(/:deep\(\.daily-todo-panel \.panel-layout\)\s*\{\s*grid-template-columns: minmax\(340px, 2fr\) minmax\(0, 3fr\)/)
    expect(source).toContain('grid-template-columns: minmax(300px, 2fr) minmax(0, 3fr)')
    expect(source).toMatch(/:deep\(\.daily-todo-panel \.composer-date\.el-date-editor\)\s*\{ width: 100% !important;/)
    expect(source).toMatch(/:deep\(\.daily-todo-panel \.list-scroll\)\s*\{\s*max-height: none;\s*overflow-y: auto;/)
  })
  it('手机切为上下布局并允许弹窗内容纵向滚动', () => {
    const mobile = source.slice(source.indexOf('@media (max-width: 768px)'))
    expect(mobile).toContain('max-height: calc(90dvh - 148px)')
    expect(mobile).toContain('overflow-y: auto')
    expect(mobile).toContain('grid-template-columns: minmax(0, 1fr)')
    expect(mobile).toContain('grid-template-rows: auto auto')
  })
})

describe('header daily todo launcher', () => {
  beforeEach(() => {
    fixture.dashboard = reactive({ todoPanel: panel })
    fixture.refresh.mockReset()
  })
  it('does not mount the panel until clicked and preserves drafts across closing', async () => {
    const wrapper = mountEntry()
    expect(wrapper.find('.todo-test-draft').exists()).toBe(false)
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()
    expect(fixture.refresh).not.toHaveBeenCalled()
    await wrapper.get('.todo-test-draft').setValue('未提交草稿')
    expect(wrapper.get('[aria-expanded="true"]').exists()).toBe(true)
    await wrapper.get('.close').trigger('click')
    expect(wrapper.get('[aria-expanded="false"]').exists()).toBe(true)
    expect(wrapper.get('[role="dialog"]').isVisible()).toBe(false)
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()
    expect(wrapper.get('.todo-test-draft').element.value).toBe('未提交草稿')
    expect(fixture.refresh).toHaveBeenCalledTimes(1)
    wrapper.unmount()
  })
  it('hides the launcher and unmounts panel when plugin capability disappears', async () => {
    const wrapper = mountEntry()
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()
    fixture.dashboard.todoPanel = null
    await flushPromises()
    expect(wrapper.find('[aria-label="我的每日待办"]').exists()).toBe(false)
    expect(wrapper.find('.todo-test-draft').exists()).toBe(false)
    wrapper.unmount()
  })
  it('插件能力恢复后保持关闭并以全新面板打开', async () => {
    const wrapper = mountEntry()
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()
    await wrapper.get('.todo-test-draft').setValue('旧草稿')

    fixture.dashboard.todoPanel = null
    await flushPromises()
    fixture.dashboard.todoPanel = panel
    await flushPromises()

    expect(wrapper.get('[aria-expanded="false"]').exists()).toBe(true)
    expect(wrapper.find('.todo-test-draft').exists()).toBe(false)
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()
    expect(wrapper.get('.todo-test-draft').element.value).toBe('')
    expect(fixture.refresh).not.toHaveBeenCalled()
    wrapper.unmount()
  })
  it('面板挂载失败时显示错误并允许重新加载', async () => {
    const setup = vi.fn().mockImplementationOnce(() => {
      throw new Error('待办面板加载失败')
    }).mockImplementation(() => () => '重试成功')
    fixture.dashboard.todoPanel = markRaw(defineComponent({ setup, render: () => null }))
    const wrapper = mountEntry()
    await wrapper.get('[aria-label="我的每日待办"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain('待办面板加载失败，请重试。')
    await wrapper.get('[role="alert"] button').trigger('click')
    await flushPromises()
    expect(setup).toHaveBeenCalledTimes(2)
    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('重试成功')
    wrapper.unmount()
  })
  it('does not create an entry when OA panel is unavailable', () => {
    fixture.dashboard.todoPanel = null
    const wrapper = mountEntry()
    expect(wrapper.find('button').exists()).toBe(false)
    wrapper.unmount()
  })
})
