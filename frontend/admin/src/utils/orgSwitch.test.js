import { describe, expect, it, vi } from 'vitest'
import { switchOrganizationAndReload } from './orgSwitch'

describe('switchOrganizationAndReload', () => {
  it('persists a different organization before reloading', () => {
    const calls = []
    const changed = switchOrganizationAndReload('2', {
      readCurrentOrgId: () => '1',
      writeCurrentOrgId: (orgId, persist) => calls.push(['write', orgId, persist]),
      reload: () => calls.push(['reload'])
    })

    expect(changed).toBe(true)
    expect(calls).toEqual([['write', '2', true], ['reload']])
  })

  it('does not reload when selecting the current organization', () => {
    const writeCurrentOrgId = vi.fn()
    const reload = vi.fn()

    expect(switchOrganizationAndReload('1', {
      readCurrentOrgId: () => '1',
      writeCurrentOrgId,
      reload
    })).toBe(false)
    expect(writeCurrentOrgId).not.toHaveBeenCalled()
    expect(reload).not.toHaveBeenCalled()
  })

  it('ignores an empty organization', () => {
    const writeCurrentOrgId = vi.fn()
    const reload = vi.fn()

    expect(switchOrganizationAndReload('  ', {
      readCurrentOrgId: () => '1',
      writeCurrentOrgId,
      reload
    })).toBe(false)
    expect(writeCurrentOrgId).not.toHaveBeenCalled()
    expect(reload).not.toHaveBeenCalled()
  })
})
