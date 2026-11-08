import { describe, expect, it } from 'vitest'

import {
  isPhoneAccount,
  normalizeAccountInput,
  normalizePhoneInput
} from './accountType.js'

describe('normalizePhoneInput', () => {
  it('strips grouped spaces from pasted mainland mobile numbers', () => {
    expect(normalizePhoneInput('132 6270 8190')).toBe('13262708190')
    expect(normalizePhoneInput(' 132  6270  8190 ')).toBe('13262708190')
  })

  it('strips hyphens used as digit separators', () => {
    expect(normalizePhoneInput('132-6270-8190')).toBe('13262708190')
  })
})

describe('phone account with separators', () => {
  it('accepts spaced mobile numbers after normalize', () => {
    expect(isPhoneAccount('132 6270 8190')).toBe(true)
    expect(normalizeAccountInput('132 6270 8190')).toBe('13262708190')
  })
})
