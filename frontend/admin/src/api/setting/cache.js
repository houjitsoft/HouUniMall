import request from '@/utils/request'

export function getCacheSettings() {
  return request.post('/web/base/cache/settings/get')
}

export function saveCacheSettings(data) {
  return request.post('/web/base/cache/settings/save', data)
}

export function testCacheConnection() {
  return request.post('/web/base/cache/settings/test')
}
