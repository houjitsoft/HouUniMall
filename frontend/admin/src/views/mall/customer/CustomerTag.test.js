// @vitest-environment jsdom
import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
const api = vi.hoisted(() => ({ fetchMemberTagList: vi.fn(), saveMemberTag: vi.fn(), deleteMemberTag: vi.fn(), previewMemberSegment: vi.fn() }))
const messages = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), confirm: vi.fn() }))
vi.mock('@/api/user/portrait', () => api)
vi.mock('element-plus', () => ({ ElMessage: messages, ElMessageBox: { confirm: messages.confirm } }))
import CustomerTag from './CustomerTag.vue'
const mount = () => shallowMount(CustomerTag, { global: { stubs: { 'el-card': { template: '<div><slot /><slot name="header" /></div>' }, 'el-table': true, 'el-dialog': true, 'el-form': true, 'el-pagination': true, 'el-alert': true, 'el-button': true }, directives: { loading: {} }, config: { warnHandler: () => {} } } })
const row = { tagId: 4, tagName: '高复购', tagColor: '#123456', tagWeight: 8, tagDescribe: '真实标签', memberCount: 2 }
describe('商城用户标签真实接口', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.fetchMemberTagList.mockResolvedValue({ code: 1, data: { list: [row] } })
    api.saveMemberTag.mockResolvedValue({ code: 1, data: 4 })
    api.deleteMemberTag.mockResolvedValue({ code: 1 })
    api.previewMemberSegment.mockResolvedValue({ code: 1, data: { total: 2, records: [{ userId: '1' }, { userId: '2' }] } })
    messages.confirm.mockResolvedValue('confirm')
  })
  it('挂载读取真实字典，不补演示数据，查询和分页基于返回记录', async () => {
    const w = mount(); await flushPromises()
    expect(api.fetchMemberTagList).toHaveBeenCalledOnce()
    expect(w.vm.tableData).toEqual([row])
    w.vm.searchForm.name = '不存在'; w.vm.handleSearch()
    expect(w.vm.tableData).toEqual([])
    w.unmount()
  })
  it('编辑提交名称颜色权重说明，并重新查询数据库', async () => {
    const w = mount(); await flushPromises()
    w.vm.handleEdit(row); w.vm.form.tagName = '更新'; await w.vm.handleSubmit()
    expect(api.saveMemberTag).toHaveBeenCalledWith({ tagId: 4, tagName: '更新', tagColor: '#123456', tagWeight: 8, tagDescribe: '真实标签' })
    expect(api.fetchMemberTagList).toHaveBeenCalledTimes(2)
    w.unmount()
  })
  it('保存失败不假报成功，不关闭表单', async () => {
    const w = mount(); await flushPromises()
    api.saveMemberTag.mockResolvedValue({ code: 0, message: '写库失败' })
    w.vm.handleEdit(row); await w.vm.handleSubmit()
    expect(messages.success).not.toHaveBeenCalled()
    expect(w.vm.dialogVisible).toBe(true)
    expect(messages.error).toHaveBeenCalledWith('写库失败')
    w.unmount()
  })
  it('删除先确认，成功后读取列表；取消不调用写接口', async () => {
    const w = mount(); await flushPromises()
    messages.confirm.mockRejectedValueOnce('cancel')
    await w.vm.handleDelete(row)
    expect(api.deleteMemberTag).not.toHaveBeenCalled()
    await w.vm.handleDelete(row)
    expect(api.deleteMemberTag).toHaveBeenCalledWith(4)
    expect(api.fetchMemberTagList).toHaveBeenCalledTimes(2)
    w.unmount()
  })
  it('查看客户调用标签圈人真实名单，不再只弹提示', async () => {
    const w = mount(); await flushPromises()
    w.vm.handleViewCustomers(row); await flushPromises()
    expect(api.previewMemberSegment).toHaveBeenCalledWith({ tagIds: [4], page: 1, size: 10 })
    expect(w.vm.memberTotal).toBe(2)
    expect(w.vm.members).toHaveLength(2)
    w.unmount()
  })
  it('读取失败明确报错，不伪装为成功空表', async () => {
    api.fetchMemberTagList.mockRejectedValue(new Error('连接失败'))
    const w = mount(); await flushPromises()
    expect(w.vm.loadError).toBe('连接失败')
    expect(w.vm.tableData).toEqual([])
    w.unmount()
  })
})
