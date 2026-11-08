/**
 * 演示站资讯兜底数据（hou_base_information_* 表为空时，保证头条/资讯中心可展示）
 * ID 与首页商城快报 notice.lists 的 link.id 对齐：1/2/3/5/6
 */
const COVER = (name: string) =>
    `["/uploads/mall-demo/${name}"]`

export const DEMO_INFORMATION_CLASSIFY = [
    {
        classifyId: 1,
        classifyName: '商城动态',
        classifyParentId: 0,
        classifySort: 1,
        classifyLevel: 1,
        classifyShowStatus: 1,
        childMenu: [
            {
                classifyId: 11,
                classifyName: '平台公告',
                classifyParentId: 1,
                classifySort: 1,
                classifyLevel: 2,
                classifyShowStatus: 1
            }
        ]
    },
    {
        classifyId: 2,
        classifyName: '优惠活动',
        classifyParentId: 0,
        classifySort: 2,
        classifyLevel: 1,
        classifyShowStatus: 1,
        childMenu: []
    }
]

const now = () => {
    const d = new Date()
    const p = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

export const DEMO_INFORMATION_LIST = [
    {
        informationId: 1,
        classifyId: 2,
        classifyName: '优惠活动',
        informationTitle: '春季焕新 99 折起 精选好物限时购',
        informationCover: COVER('nav-news.svg'),
        informationCoverList: ['/uploads/mall-demo/nav-news.svg'],
        informationDesc:
            '<p>商城春季焕新季正式开启，精选服饰、美妆、数码好物低至 99 折，活动期间每日上新，先到先得。</p><p>登录会员还可叠加优惠券，快来选购心仪商品吧。</p>',
        informationStatus: 1,
        pageView: 1280,
        isRecommend: 1,
        releaseTime: now(),
        releaseSource: 'UniMall商城'
    },
    {
        informationId: 2,
        classifyId: 2,
        classifyName: '优惠活动',
        informationTitle: '数码专场直降加码 10 款爆品再减价',
        informationCover: COVER('banner-01.svg'),
        informationCoverList: ['/uploads/mall-demo/banner-01.svg'],
        informationDesc:
            '<p>手机、耳机、笔记本等数码爆品限时直降，叠加店铺优惠后到手价更低，支持正品保障与运费险。</p>',
        informationStatus: 1,
        pageView: 960,
        isRecommend: 1,
        releaseTime: now(),
        releaseSource: 'UniMall商城'
    },
    {
        informationId: 3,
        classifyId: 1,
        classifyName: '商城动态',
        informationTitle: '平台升级公告：配送覆盖再扩 400 城',
        informationCover: COVER('nav-service.svg'),
        informationCoverList: ['/uploads/mall-demo/nav-service.svg'],
        informationDesc:
            '<p>为提升购物体验，平台已完成物流节点扩容，新增 400 余城次日达/隔日达服务，具体以结算页提示为准。</p>',
        informationStatus: 1,
        pageView: 742,
        isRecommend: 1,
        releaseTime: now(),
        releaseSource: 'UniMall商城'
    },
    {
        informationId: 5,
        classifyId: 1,
        classifyName: '商城动态',
        informationTitle: '积分商城换新上线 好物随心兑',
        informationCover: COVER('nav-fav.svg'),
        informationCoverList: ['/uploads/mall-demo/nav-fav.svg'],
        informationDesc:
            '<p>积分商城完成改版，兑换流程更简洁，热门积分礼品每周更新，会员购物即可累积积分。</p>',
        informationStatus: 1,
        pageView: 588,
        isRecommend: 0,
        releaseTime: now(),
        releaseSource: 'UniMall商城'
    },
    {
        informationId: 6,
        classifyId: 2,
        classifyName: '优惠活动',
        informationTitle: '美妆节预告：大牌满减券即将开抢',
        informationCover: COVER('nav-about.svg'),
        informationCoverList: ['/uploads/mall-demo/nav-about.svg'],
        informationDesc:
            '<p>美妆节倒计时开启，大牌满减券、买赠礼盒即将放出，建议提前收藏心愿单，开抢不错过。</p>',
        informationStatus: 1,
        pageView: 1102,
        isRecommend: 1,
        releaseTime: now(),
        releaseSource: 'UniMall商城'
    }
]

export const normalizeInformationNavUrl = (url: string) => {
    const u = String(url || '').trim()
    if (u === '/article/list' || u.startsWith('/article/list?')) {
        return u.replace(/^\/article\/list/, '/information')
    }
    return u || '/'
}

export const findDemoInformation = (id: string | number) =>
    DEMO_INFORMATION_LIST.find((item) => Number(item.informationId) === Number(id)) || null
