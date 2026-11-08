import request from '@/utils/request'

export function getSelectedStoreList(params) {
  return request.get('/web/mall/decorate/store/lists', { params })
}

export function getMsDataList(params) {
  return request.get('/web/mall/decorate/ms/lists', { params })
}

export function getDecorateArticle(params) {
  return request.get('/web/mall/decorate/article/lists', { params })
}

export function getDecorateHotGoods(params) {
  return request.get('/web/mall/decorate/hot_goods/lists', { params })
}
