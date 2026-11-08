import { describe, expect, it } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'

const view = fs.readFileSync(path.resolve('src/views/setting/OrgStructure.vue'), 'utf8')
const permissionResources = fs.readFileSync(path.resolve('src/views/setting/PermissionManagement.vue'), 'utf8')
const api = fs.readFileSync(path.resolve('src/api/setting/organization.js'), 'utf8')

describe('组织管理权限闭环', () => {
  it('按钮、事件和资源使用细粒度权限码', () => {
    for (const code of [
      'shell:organization:add',
      'shell:organization:view',
      'shell:organization:edit',
      'shell:organization:delete',
      'shell:organization:batch-delete',
      'shell:organization:export',
      'shell:organization:enable',
      'shell:organization:disable'
    ]) {
      expect(view).toContain(code)
      expect(permissionResources).toContain(code)
    }
    expect(view).toContain("ensurePermission(isEdit.value ? 'shell:organization:edit' : 'shell:organization:add')")
    expect(view).toContain('const canStatus =')
  })

  it('组织删除、批删和状态接口与 BFF 路径一致', () => {
    expect(api).toContain('/web/base/organization/deleteOrganization')
    expect(api).toContain('/web/base/organization/batchDeleteOrganization')
    expect(api).toContain('/web/base/organization/updateOrganizationStatus')
  })
})
