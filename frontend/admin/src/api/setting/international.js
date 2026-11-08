import request from '@/utils/request'

export const isApiSuccess = (res) => res && (res.code === 1 || res.code === 200)

export const LANGUAGE_OPTIONS = [
  { value: 'en', label: 'English', flag: '🇺🇸' },
  { value: 'ja', label: '日本語', flag: '🇯🇵' },
  { value: 'ko', label: '한국어', flag: '🇰🇷' },
  { value: 'fr', label: 'Français', flag: '🇫🇷' },
  { value: 'de', label: 'Deutsch', flag: '🇩🇪' }
]

function mapCountry(row) {
  if (!row) return row
  return {
    id: row.id,
    name: row.countryName ?? row.name,
    phoneCode: row.internationalDialingCode ?? row.phoneCode,
    isCommon: row.countryCommonUse === 1 || row.isCommon === true,
    countryCommonUse: row.countryCommonUse,
    deleteType: row.deleteType,
    initial: row.countryInitial
  }
}

function mapLanguage(row) {
  if (!row) return row
  return {
    id: row.id,
    cnText: row.cnText,
    languageCode: row.languageCode,
    translation: row.translation ?? row.correctedTranslation ?? row.originalTranslation,
    originalTranslation: row.originalTranslation,
    correctedTranslation: row.correctedTranslation,
    isCorrected: row.isCorrected === 1,
    translationSource: row.translationSource
  }
}

export const internationalApi = {
  async listCurrency({ remark, page = 1, size = 10 }) {
    const res = await request.post('/web/base/currency/list', {
      remark: remark || undefined,
      page,
      limit: size
    })
    if (!isApiSuccess(res)) return { list: [], total: 0 }
    const data = res.data || {}
    return {
      list: data.list || data.records || [],
      total: data.totalCount ?? data.total ?? 0
    }
  },

  async saveCurrency(payload) {
    return request.post('/web/base/currency/save', { ...payload })
  },

  async deleteCurrency(id) {
    return request.post('/web/base/currency/delete', { id })
  },

  async listCountry({ name, page = 1, size = 50 }) {
    const res = await request.post('/web/base/country/list', {
      countryName: name || undefined,
      name: name || undefined,
      page,
      limit: size
    })
    if (!isApiSuccess(res)) return { list: [], total: 0 }
    const data = res.data || {}
    const raw = data.list || data.records || (Array.isArray(data) ? data : [])
    const list = raw.map(mapCountry)
    return {
      list,
      total: data.totalCount ?? data.total ?? list.length
    }
  },

  async saveCountry(form) {
    const payload = {
      id: form.id,
      countryName: form.name,
      name: form.name,
      internationalDialingCode: form.phoneCode,
      phoneCode: form.phoneCode,
      countryCommonUse: form.isCommon ? 1 : 2,
      isCommon: form.isCommon
    }
    return request.post('/web/base/country/save', payload)
  },

  async deleteCountry(id) {
    return request.post('/web/base/country/delete', { id })
  },

  async listLanguage({ keyword, languageCode, page = 1, size = 20 }) {
    const res = await request.post('/web/base/language/list', {
      keyword: keyword || undefined,
      languageCode,
      language: languageCode,
      page,
      limit: size
    })
    if (!isApiSuccess(res)) return { list: [], total: 0 }
    const data = res.data || {}
    const raw = data.list || data.records || (Array.isArray(data) ? data : [])
    return {
      list: raw.map(mapLanguage),
      total: data.totalCount ?? data.total ?? raw.length
    }
  },

  async correctTranslation({ id, languageCode, correctedText }) {
    return request.post('/web/base/language/correct', {
      id,
      languageCode,
      language: languageCode,
      correctedTranslation: correctedText,
      correctedText
    })
  }
}
