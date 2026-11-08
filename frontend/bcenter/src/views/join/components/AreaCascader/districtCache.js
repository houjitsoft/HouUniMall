import { queryDistinct } from '@/api/join'

const cache = new Map()
const inflight = new Map()

function mapDistrictList(list) {
  return (list || []).map((d) => ({
    name: d.districtName,
    id: d.districtId,
    parentId: d.districtParentId,
  }))
}

export function seedRootDistricts(list) {
  if (!list?.length || cache.has(0)) return
  cache.set(0, mapDistrictList(list))
}

export function fetchDistrictChildren(parentId = 0) {
  if (cache.has(parentId)) {
    return Promise.resolve(cache.get(parentId))
  }
  if (inflight.has(parentId)) {
    return inflight.get(parentId)
  }
  const promise = queryDistinct({ districtParentId: parentId })
    .then((res) => {
      if (!res || res.code === 2) {
        cache.set(parentId, [])
        return []
      }
      const children = mapDistrictList(res.data)
      cache.set(parentId, children)
      return children
    })
    .catch(() => {
      cache.set(parentId, [])
      return []
    })
    .finally(() => {
      inflight.delete(parentId)
    })
  inflight.set(parentId, promise)
  return promise
}

/** 模块加载时预取省级数据，减少首次打开等待 */
export const preloadRootDistricts = fetchDistrictChildren(0)
