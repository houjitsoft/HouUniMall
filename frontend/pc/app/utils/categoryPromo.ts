/** 首页四栏品类促销带：默认数据与装修 cube 映射 */

export interface CategoryPromoCard {
  key?: string | number
  eyebrow: string
  slogan: string
  cta?: string
  image: string
  background: string
  path?: string
  link?: any
}

export const DEFAULT_CATEGORY_PROMO: CategoryPromoCard[] = [
  {
    key: 'office',
    eyebrow: '电脑办公',
    slogan: '搜罗爆款好货',
    image: '/uploads/mall-demo/promo-office.png',
    background: 'linear-gradient(135deg, #f6c93d 0%, #e9a61a 100%)',
    path: '/product/list?kws=电脑办公'
  },
  {
    key: 'camera',
    eyebrow: '捕捉光影瞬间',
    slogan: '好物天天低价',
    image: '/uploads/mall-demo/promo-camera.png',
    background: 'linear-gradient(135deg, #d9b48a 0%, #c79a6b 100%)',
    path: '/product/list?kws=数码相机'
  },
  {
    key: 'milk',
    eyebrow: '牛奶乳品',
    slogan: '冬季福利购',
    image: '/uploads/mall-demo/promo-milk.png',
    background: 'linear-gradient(135deg, #e8a0a8 0%, #d57c8a 100%)',
    path: '/product/list?kws=牛奶乳品'
  },
  {
    key: 'beauty',
    eyebrow: '妆出彩',
    slogan: '搜罗全球美妆',
    image: '/uploads/mall-demo/promo-lipstick.png',
    background: 'linear-gradient(135deg, #4eb6b0 0%, #2f9a94 100%)',
    path: '/product/list?kws=美妆'
  }
]

const absImage = (url = '') => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const resolvePath = (link: any) => {
  if (!link) return ''
  if (typeof link === 'string') return link
  if (link.path) return link.path
  if (link.url) return link.url
  if (link.query?.id && (link.type === 'goods' || link.type === 'product')) return `/product/${link.query.id}`
  if (link.query?.id && link.type === 'article') return `/information/${link.query.id}`
  if (link.query?.catId || link.query?.id) return `/product/list?catId=${link.query.catId || link.query.id}`
  return ''
}

const isWeakDemoImage = (url = '') => {
  const u = String(url)
  return !u || u.includes('banner-0') || u.includes('goods-01.svg') || u.endsWith('.html')
}

/** 从装修 cube 模块生成四栏卡片；不足时回退默认 */
export const buildCategoryPromoCards = (cubeModule?: any): CategoryPromoCard[] => {
  const raw = Array.isArray(cubeModule?.content?.data) ? cubeModule.content.data : []
  const mapped = raw.slice(0, 4).map((item: any, index: number) => {
    const fallback = DEFAULT_CATEGORY_PROMO[index] || DEFAULT_CATEGORY_PROMO[0]
    const image = absImage(item?.image || item?.commonImage || '')
    return {
      key: item?.id || fallback.key || index,
      eyebrow: item?.name || item?.title || item?.eyebrow || fallback.eyebrow,
      slogan: item?.slogan || item?.desc || item?.subTitle || fallback.slogan,
      cta: item?.cta || item?.buttonText || '立即进入',
      image: isWeakDemoImage(image) ? fallback.image : image,
      background: item?.background || item?.bgColor || fallback.background,
      path: resolvePath(item?.link) || fallback.path,
      link: item?.link
    } as CategoryPromoCard
  })
  return mapped.length >= 4 ? mapped : DEFAULT_CATEGORY_PROMO
}
