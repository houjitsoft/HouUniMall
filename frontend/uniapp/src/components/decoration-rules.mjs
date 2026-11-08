export const supportedWidgets = ['search', 'banner', 'middle-banner', 'notice', 'nav', 'cube', 'title', 'blank', 'goodslists', 'subsidy', 'seckill', 'team', 'shoplists', 'choice', 'news']
export function flattenWidgets(data) {
    return (Array.isArray(data) ? data.flat() : []).filter(Boolean)
}
export function visibleItems(items) {
    return (Array.isArray(items) ? items : []).filter(x => x && (x.is_show == null || ['1', '3'].includes(String(x.is_show))))
}
export function goodsItems(items) {
    return (Array.isArray(items) ? items : []).filter(x => x && (x.id || x.commonId || x.name || x.commonName)).map(x => ({
        ...x, id: x.id ?? x.commonId ?? x.goodsId, name: x.name ?? x.commonName ?? x.goodsName,
        image: x.image ?? x.commonImage ?? x.goodsImage, price: x.price ?? x.commonPrice ?? x.seckillPrice,
        max_line_price: x.max_line_price ?? x.commonMarketPrice ?? x.goodsPrice,
        sale_num: x.sale_num ?? x.commonSalenum ?? 0
    }))
}
export function widgetStyle(styles = {}) {
    const result = {}
    // 只接受展示属性，不把任意对象当作 HTML 或脚本执行。
    const keys = ['color', 'background', 'backgroundColor', 'backgroundImage', 'fontFamily', 'fontSize', 'fontWeight', 'lineHeight', 'textAlign', 'padding', 'paddingTop', 'paddingRight', 'paddingBottom', 'paddingLeft', 'margin', 'marginTop', 'marginRight', 'marginBottom', 'marginLeft', 'borderRadius', 'border', 'height', 'minHeight', 'gap']
    for (const [raw, value] of Object.entries(styles || {})) {
        const key = raw.replace(/_([a-z])/g, (_, c) => c.toUpperCase()).replace(/-([a-z])/g, (_, c) => c.toUpperCase())
        if (!keys.includes(key) || !['string', 'number'].includes(typeof value)) continue
        if (/url\s*\(\s*['"]?\s*(?:javascript|data:text\/html)/i.test(String(value))) continue
        result[key] = typeof value === 'number' && !['fontWeight', 'lineHeight'].includes(key) ? `${value}px` : value
    }
    return result
}
/**
 * tea 模板的实拍素材池。
 *
 * <p>这一版模板是对齐 tea.houjit.com 的编辑式版式，视觉全靠真实照片撑：
 * 130% 竖构图、无卡片、大留白，线稿占位图放进去一眼就露怯。
 * 因此模板自带演示素材统一换成实拍图（photo-01…photo-12，随包放在
 * public/uploads/mall-demo/tea）。商家自己上传的商品图不含这些键，天然不受影响。
 */
const teaPhotoPool = Array.from({ length: 12 }, (_, i) => `/uploads/mall-demo/tea/photo-${String(i + 1).padStart(2, '0')}.jpg`)

/**
 * tea 模板实拍兜底图（按位序轮流取）。
 *
 * <p>存量库里的演示商品图指向的对象存储文件可能已被删除（返回 404），
 * `<image>` 取不到图时不报错、直接把背景置空，整块卡片就变成灰色占位。
 * 列表页按位序取实拍池兜底，保证 tea 版式里每张卡都有图。
 *
 * @param {number} index 卡片位序（从 0 开始）
 * @param {string} prefix 需要补的站点前缀（H5 部署在子目录时非空）
 * @returns {string} 图片地址
 */
export function teaPhotoAt(index, prefix = '') {
    const i = Math.abs(Number(index) || 0) % teaPhotoPool.length
    return prefix + teaPhotoPool[i]
}

/**
 * 演示商品图在 tea 模板下的替换表。
 *
 * <p>键只取文件名，pc/demo/goods-xx.svg 与 uploads/mall-demo/goods-xx.svg
 * 两种写法都能命中；商家自己上传的图片不含这些文件名，天然不受影响。
 * 16 个位序素材 + 具名素材轮流取实拍池，保证同一屏里的相邻商品不同图。
 */
export const teaGoodsAssets = Object.fromEntries([
    'goods-01.svg', 'goods-02.svg', 'goods-03.svg', 'goods-04.svg', 'goods-05.svg', 'goods-06.svg',
    'goods-07.svg', 'goods-08.svg', 'goods-09.svg', 'goods-10.svg', 'goods-11.svg', 'goods-12.svg',
    'goods-13.svg', 'goods-14.svg', 'goods-15.svg', 'goods-16.svg',
    'goods-blazer.svg', 'goods-cream.svg', 'goods-dress.svg', 'goods-ear.svg', 'goods-fryer.svg',
    'goods-jeans.svg', 'goods-laptop.svg', 'goods-lipstick.svg', 'goods-mask.svg', 'goods-pad.svg',
    'goods-phone.svg', 'goods-shirt.svg', 'goods-sunscreen.svg', 'goods-tee.svg', 'goods-vacuum.svg',
    'goods-watch.svg'
].map((key, i) => [key, teaPhotoPool[i % teaPhotoPool.length]]))

export const teaTheme = {
    name: '奢侈品简单风格', className: 'tea_theme', templateCode: 'tea', color: '#b0805a', dark_color: '#1d2420',
    bg_color: '#f4f1ea', nav_bg_color: '#f4f1ea', nav_text_color: '#221e17',
    variable: { '--theme-color': '#b0805a', '--theme-dark-color': '#1d2420', '--theme-light-color': '#e6e2d9', '--theme-disabled-color': '#d8cfc0', '--tea-gold': '#b0805a' }
}

/**
 * tea 模板自带的版式文案。
 *
 * <p>对齐 tea.houjit.com 的编辑式首页：首屏需要眉标 + 大标题 + 副题 + 两个引导按钮，
 * 每个区块需要「眉标 / 标题 / 说明」三段式区块头，
 * 而装修数据里的 banner / goodslists 只存图片和商品，没有这些文字位。
 * 因此模板给一套默认文案，装修页里写了 title 组件或字段时以页面为准。
 *
 * <p>文案刻意不写具体品牌名：模板会用在任何商户的商城里，品牌名一律取站点名。
 */
export const teaContent = {
    hero: {
        eyebrow: '精选产区 · 少量批次 · 会员制',
        title: '一方风物 · 一期一会',
        sub: '高山茶 · 咖啡 · 匠心器物',
        primary: { text: '探索臻品', link: '/pages/information/information' },
        secondary: { text: '循邀入会', link: '/member/plus/plus' },
        footLeft: '向下探索',
        footRight: '高山茶 · 咖啡 · 匠心器物',
        /**
         * 多页轮播的每页文案：第 1 页与上面的 hero 完全一致，后续页按 banner 顺序取。
         * 装修页里的 banner 图数量多于文案时，多出来的页复用最后一组文案，不会留空。
         */
        slides: [
            {
                eyebrow: '精选产区 · 少量批次 · 会员制',
                title: '一方风物 · 一期一会',
                sub: '高山茶 · 咖啡 · 匠心器物',
                primary: { text: '探索臻品', link: '/pages/information/information' },
                secondary: { text: '循邀入会', link: '/member/plus/plus' }
            },
            {
                eyebrow: '春摘 · 海拔 1800 米',
                title: '山场之气 · 尽在一盏',
                sub: '单株采摘 · 手工揉捻 · 小批次',
                primary: { text: '查看山场', link: '/pages/information/information' },
                secondary: { text: '循邀入会', link: '/member/plus/plus' }
            },
            {
                eyebrow: '器物 · 亦是风物',
                title: '以器承茶 · 以静养心',
                sub: '手作陶器 · 粗陶与白瓷',
                primary: { text: '浏览器物', link: '/pages/information/information' },
                secondary: { text: '循邀入会', link: '/member/plus/plus' }
            }
        ]
    },
    sections: {
        goodslists: { eyebrow: 'NEW ARRIVAL · 本期风物', title: '新一期风物', desc: '小产区、少批次，售完不补。' },
        choice: { eyebrow: 'SELECTION · 臻选', title: '臻选风物', desc: '按场景分组的当季推荐。' },
        seckill: { eyebrow: 'LIMITED · 限时', title: '限时风物', desc: '限量批次，售完即止。' },
        team: { eyebrow: 'TOGETHER · 同行', title: '同行共购', desc: '与同频的人一起成团。' },
        subsidy: { eyebrow: 'BENEFIT · 礼遇', title: '会员礼遇', desc: '为会员准备的专属价。' },
        cube: { eyebrow: 'LOOKBOOK · 影像', title: '以影像记录风物', desc: '器物与茶汤的光影，留给慢下来的人。' },
        shoplists: { eyebrow: 'BOUTIQUE · 店铺', title: '精选店铺', desc: '' },
        news: { eyebrow: 'JOURNAL · 手记', title: '风物手记', desc: '' }
    },
    /**
     * 首页（tea 版式）的整页文案。
     *
     * <p>对齐 tea.houjit.com 首页的六个区块：首屏 → 会员条 → 公告 → 新品展示 → 风物影像
     * → 风物目录 → 瀹集 → 会员权益 → 页脚。区块顺序与文案全部由模板固定，
     * 装修页只提供首屏图与公告文字（见 TeaHome 组件的 loadDecoration）。
     *
     * <p>文案刻意不写具体品牌名：品牌名一律取站点名（/web/mall/index/config 的 website.name）。
     */
    home: {
        hero: {
            slides: [
                {
                    eyebrow: 'SINCE 2019 · 小产区高山茶与咖啡',
                    titleLines: ['一方风物，', '一席知音'],
                    sub: '茶 · 咖啡 · 器具 · 纪念品',
                    primary: { text: '探索风物', link: '/pages/search/search' },
                    secondary: { text: '循邀入会', link: '/member/plus/plus' }
                },
                {
                    eyebrow: '春摘 · 海拔 1800 米',
                    titleLines: ['山场之气，', '尽在一盏'],
                    sub: '单株采摘 · 手工揉捻 · 小批次',
                    primary: { text: '查看风物', link: '/pages/search/search' },
                    secondary: { text: '循邀入会', link: '/member/plus/plus' }
                },
                {
                    eyebrow: '器物 · 亦是风物',
                    titleLines: ['以器承茶，', '以静养心'],
                    sub: '手作陶器 · 粗陶与白瓷',
                    primary: { text: '浏览器物', link: '/pages/search/search' },
                    secondary: { text: '循邀入会', link: '/member/plus/plus' }
                }
            ],
            footLeft: '向下探索',
            footRight: '高山茶 · 咖啡 · 匠心器物'
        },
        member: { text: '臻味唯呈会员 · 静候知音', cta: '循邀入会', link: '/member/plus/plus' },
        new: {
            eyebrow: 'NEW · 新品展示',
            title: '新一期风物',
            desc: '新品依会员等级分档开放；限量批次按等级先后排序，额度与窗口以实际发布为准。'
        },
        film: {
            eyebrow: 'FILM · 风物影像',
            title: '以影像记录风物',
            desc: '瀹集现场与产区风物，以影像先于文字抵达。',
            cta: '前往视频号',
            link: '/pages/video/video',
            items: [
                { image: '/uploads/mall-demo/tea/photo-07.jpg', duration: '02:14', title: '风物短片 · 演示', state: '待发布' },
                { image: '/uploads/mall-demo/tea/photo-03.jpg', duration: '03:40', title: '瀹集回顾 · 演示', state: '待发布' }
            ]
        },
        collection: {
            eyebrow: 'COLLECTION · 风物目录',
            title: '不做 SKU，只做版本',
            desc: '以真实产区与批次记录风物，不以价格促销定义稀缺。',
            cta: '查看全部臻品',
            link: '/pages/search/search'
        },
        ritual: {
            eyebrow: 'RITUAL · 瀹集',
            title: '瀹集 · 会山河',
            desc: '给同频的人一个坐下来的理由，场次与参与方式以发布信息为准。',
            rows: [
                { tag: '品鉴', title: '风物品鉴 · 场次样例', meta: ['时间待发布', '席位待配置'] },
                { tag: '邀约', title: '匠人工作坊 · 邀约样例', meta: ['时间待发布', '席位待配置'] }
            ]
        },
        tier: {
            eyebrow: 'PRIVATE · 会员权益',
            title: '等级只解锁体验',
            cta: '查看我的等级',
            link: '/member/plus/plus',
            items: [
                { name: '五星', note: '专属顾问、生日礼遇，具体内容待确认' },
                { name: '七星', note: '首发瀹集、节气礼遇、限量批次优先购，规则待确认' },
                { name: '九星', note: '新品预览、优先购、匠人工作坊与年度编号款，规则待确认' }
            ]
        },
        footer: {
            title: '珍藏风物，让日常成为礼遇。',
            links: [
                { text: '探索臻品', path: '/pages/search/search' },
                { text: '瀹集雅会', path: '/pages/activity/activity' },
                { text: '专属顾问', path: '/member/plus/plus' },
                { text: '会员中心', path: '/pages/personal/personal' }
            ]
        },
        card: {
            labels: ['TEA', 'COFFEE', 'WARE', 'GIFT'],
            member: '会员价 · 入会呈鉴',
            unlock: '入会解锁会员价',
            unlockLink: '/member/plus/plus',
            fallbackDesc: '产区与批次样例 · 支持溯源',
            soldOut: '已售完 · 待补',
            empty: '商品上架后依批次在此陈列'
        }
    },
    /**
     * 商品列表页（内页）的编辑式文案。
     *
     * <p>内页没有装修记录，标题/眉标只能由模板给默认值；分类名、商品数量等仍取接口数据。
     */
    list: {
        navTitle: '选购',
        note: '风物目录 · 按产区与批次陈列，售完不补',
        eyebrow: 'COLLECTION',
        title: '风物目录',
        subtitle: '茶 · 咖啡 · 器具 · 纪念品',
        all: '全部',
        // 卡片底部那行会员提示：参考站卡片只写会员价、不亮价，
        // 我们是通贩站，价格照常展示，这行只做「入会更优」的引导。
        member: '会员价 · 入会呈鉴'
    },
    /**
     * 瀹集页（pages/activity）的文案。
     *
     * <p>参考站同页明确写着「演示活动 · 非真实场次，不接受报名」，即场次尚未接入真实数据；
     * 这里照实保持同一口径：不编造场次、不伪造受邀状态，申请/邀请两个动作统一说明「暂未接入」。
     * 后台接入真实活动数据后，只需把 cards 换成接口结果，结构与样式不用动。
     */
    ritualPage: {
        navTitle: '瀹集',
        eyebrow: 'RITUAL · 瀹集',
        title: '瀹集 · 会山河',
        desc: '瀹集分公开申请与定向邀请，参与资格、时间与席位以实际发布为准。',
        filters: [
            { key: 'all', label: '全部' },
            { key: 'apply', label: '申请瀹集' },
            { key: 'invite', label: '邀请瀹集' }
        ],
        demoNote: '演示活动 · 非真实场次，不接受报名',
        mine: {
            title: '我的邀请',
            body: '邀请查询、生成与发送暂未接入，不表示您收到邀请。下方仅可制作本页临时预览，不保存、不发送。',
            action: '邀请服务说明'
        },
        cards: [
            {
                key: 'apply',
                eyebrow: '申请瀹集 · 品鉴（演示）',
                threshold: '参与门槛：待配置',
                title: '风物品鉴 · 场次样例',
                body: '仅供界面审阅，不是真实活动，不接受报名。',
                meta: ['日期：时间待发布', '地点：待发布'],
                seats: '席位待配置',
                action: '申请参加',
                preview: ''
            },
            {
                key: 'invite',
                eyebrow: '邀请瀹集 · 邀约',
                threshold: '参与门槛：待配置',
                title: '匠人工作坊 · 邀约样例',
                body: '仅供界面审阅，不表示当前用户受邀。',
                meta: ['日期：时间待发布', '地点：待发布'],
                seats: '席位待配置',
                action: '查看邀请（待接入）',
                preview: '制作邀请单预览（未发送）'
            }
        ],
        /** 所有按钮统一弹这一个说明：不假装成功、不伪造受邀状态 */
        modal: {
            title: '邀请服务暂未接入',
            content: '现有接口未提供邀请生成、发送、查询和参加确认。无法判断是否受邀，此操作不会生成或确认真实邀请。',
            confirm: 'OK'
        }
    },
    /** 商品详情页（内页）的编辑式文案：区块眉标用大写英文，与列表页同一套排版语言 */
    detail: {
        note: '产地与批次随货附卡 · 支持溯源',
        spec: 'SPECIFICATION',
        detail: 'DETAIL',
        recommend: 'MORE · 同期风物'
    }
}

/**
 * 移动端页面模板注册表（与后台「模板选择」的模板库一一对应）。
 *
 * <p>模板唯一权威定义在后台 base 服务 MobilePageTemplates / mall 镜像 MobileTemplateViews
 * （code/name/summary/themeColor/style 五字段），选择结果持久化在
 * hou_base_decorate_page.page_template_code，经 /web/mall/index/data 下发到本文件消费。
 * 本注册表负责把 code 翻译成 uniapp 里真实可见的样式（页面底色 + 各装修组件默认风格）。
 *
 * <p>widgets 里的 styles/content 只补模板默认值，装修页里已保存的显式配置永远优先。
 */
export const pageTemplates = {
    simple: {
        code: 'simple', name: '简约白', style: 'single-column', color: '#19c7a5',
        bg_color: '#f7f8fa',
        widgets: {
            banner: { styles: { margin: '0 12px', borderRadius: '8px' } },
            nav: { styles: { color: '#1a1a1a' }, content: { type: 3, show_nums: 5 } },
            title: { styles: { padding: '20px 12px 8px' }, content: { title_color: '#1a1a1a', subtitle_color: '#19c7a5', style: 1 } },
            goodslists: { styles: { padding: '0 12px 28px', color: '#333333' }, content: { show_type: 1, show_content: [1, 2, 4] } }
        }
    },
    vivid: {
        code: 'vivid', name: '活力橙', style: 'double-column', color: '#f2994a',
        bg_color: '#fff7f0',
        widgets: {
            banner: { styles: { margin: '0 10px', borderRadius: '10px' } },
            nav: { styles: { color: '#7a3d10' }, content: { type: 3, show_nums: 4 } },
            title: { styles: { padding: '18px 10px 6px' }, content: { title_color: '#7a3d10', subtitle_color: '#f2994a', style: 1 } },
            goodslists: { styles: { padding: '0 10px 24px', color: '#4a2c12' }, content: { show_type: 2, show_content: [1, 2, 3, 4] } }
        }
    },
    premium: {
        code: 'premium', name: '高端黑', style: 'banner-first', color: '#1f2733',
        bg_color: '#14181d',
        widgets: {
            banner: { styles: { margin: '0', borderRadius: '0' } },
            nav: { styles: { color: '#e8eaed', background: '#1f2733' }, content: { type: 3, show_nums: 5 } },
            title: { styles: { padding: '26px 16px 10px' }, content: { title_color: '#f2f4f6', subtitle_color: '#9aa4af', style: 2 } },
            goodslists: { styles: { padding: '0 16px 32px', color: '#e6e9ec' }, content: { show_type: 1, show_content: [1, 2] } }
        }
    },
    tea: {
        code: 'tea', name: '奢侈品简单风格', style: 'tea-brand', color: '#b0805a',
        bg_color: teaTheme.bg_color,
        /**
         * 演示素材替换表。
         *
         * <p>只在页面仍引用<b>内置演示图</b>时生效（banner-0X / goods-0X / nav-* 这些 svg），
         * 商家自己上传的图片一律不动。否则换了模板只是换配色，
         * 满屏红蓝备占位图看上去仍不像这个模板。
         */
        demoAssets: {
            banner: {
                'pc/demo/banner-01.svg': '/uploads/mall-demo/tea/photo-01.jpg',
                'pc/demo/banner-02.svg': '/uploads/mall-demo/tea/photo-05.jpg',
                'pc/demo/banner-03.svg': '/uploads/mall-demo/tea/photo-02.jpg',
                'mall-demo/banner-01.svg': '/uploads/mall-demo/tea/photo-01.jpg',
                'mall-demo/banner-02.svg': '/uploads/mall-demo/tea/photo-05.jpg',
                'mall-demo/banner-03.svg': '/uploads/mall-demo/tea/photo-02.jpg'
            },
            'middle-banner': {
                'pc/demo/banner-01.svg': '/uploads/mall-demo/tea/photo-09.jpg',
                'mall-demo/banner-01.svg': '/uploads/mall-demo/tea/photo-09.jpg'
            },
            nav: {
                'mall-demo/nav-news.svg': '/uploads/mall-demo/tea/nav-news.svg',
                'mall-demo/nav-settings.svg': '/uploads/mall-demo/tea/nav-settings.svg',
                'mall-demo/nav-fav.svg': '/uploads/mall-demo/tea/nav-fav.svg',
                'mall-demo/nav-about.svg': '/uploads/mall-demo/tea/nav-about.svg',
                'mall-demo/nav-service.svg': '/uploads/mall-demo/tea/nav-service.svg'
            },
            // 店铺货品 / 店铺头像走真实店铺数据，作为 goodslists 与 shoplists 的共用素材表
            shoplists: teaGoodsAssets,
            goodslists: teaGoodsAssets,
            // 图立方是方格，用竖版素材；沿用横版 banner 会在方格里被裁掉大半
            cube: {
                'pc/demo/banner-01.svg': '/uploads/mall-demo/tea/photo-07.jpg',
                'pc/demo/banner-02.svg': '/uploads/mall-demo/tea/photo-03.jpg',
                'pc/demo/banner-03.svg': '/uploads/mall-demo/tea/photo-09.jpg',
                'mall-demo/banner-01.svg': '/uploads/mall-demo/tea/photo-07.jpg',
                'mall-demo/banner-02.svg': '/uploads/mall-demo/tea/photo-03.jpg',
                'mall-demo/banner-03.svg': '/uploads/mall-demo/tea/photo-09.jpg',
                'mall-demo/goods-01.svg': '/uploads/mall-demo/tea/photo-12.jpg'
            }
        },
        widgets: {
            banner: { styles: { margin: '0' } },
            nav: { styles: { padding: '26px 24px 6px' }, content: { type: 1, show_nums: 5, show_rows: 1 } },
            // 图立方在 tea 版式里是深色影像区：两列错落，不再按 4 号方格拼图
            cube: { content: { image_type: 4 } },
            title: { styles: { padding: '0' }, content: { title_color: '#191917', subtitle_color: '#87745b', style: 2 } },
            goodslists: { styles: { padding: '0' }, content: { show_type: 3, show_content: [1, 2] } }
        }
    }
}

/**
 * 模板可覆盖的整页皮肤（作为 .dc-tpl-<code> 的 CSS 补充，见 homeIndex.vue 样式块）。
 *
 * <p>只记录「模板必须保证的视觉底线」，具体样式仍写在样式表里，
 * 便于设计调整时不改逻辑。
 */
export const templateSkin = {
    tea: {
        pageBg: '#f4f1ea',
        titleFont: 'Georgia, "Songti SC", "STSong", serif',
        titleColor: '#221e17',
        accent: '#b0805a'
    }
}

/**
 * 每套模板在代码与数据库中的存储位置（前台「模板预览」角标与后台卡片共同展示）。
 */
export const templateStorage = {
    implement: 'uniapp/src/components/decoration-rules.mjs (pageTemplates)',
    persist: 'hou_base_decorate_page.page_template_code',
    define: 'base/MobilePageTemplates + mall/MobileTemplateViews'
}

/**
 * 按模板 code / 网站风格解析当前生效模板。
 *
 * @param {Object} websiteStyle 网站风格（/web/mall/index/config 的 mall_style）
 * @param {Object} template     模板描述（{ templateCode } 或后台下发的完整 templateView）
 * @returns {Object|null} 命中的模板注册项；未套用模板时，仅网站风格本身选了「奢侈品简单风格」才返回 tea
 */
export function currentTemplate(websiteStyle = {}, template = {}) {
    const code = template.templateCode ?? template.code
    if (code) return pageTemplates[String(code).trim().toLowerCase()] || null
    if (websiteStyle.current === 'tea_theme') return pageTemplates.tea
    return null
}

export function isTeaStyle(websiteStyle = {}, template = {}) {
    return currentTemplate(websiteStyle, template)?.code === 'tea'
}

/**
 * 模板可以接管的「版式字段」。
 *
 * <p>模板的职责是决定版式（几列、几行、几图、居中还是左对齐），
 * 页面里保存的则是内容（商品、图片、文案、跳转）。
 * 这两类字段一旦混在一起按「页面优先」合并，模板就会被页面里早先保存的
 * 版式值压死，后台换了模板前台也看不出变化。
 */
export const LAYOUT_KEYS = ['show_type', 'show_nums', 'show_rows', 'style', 'image_type', 'columns', 'text_type', 'type']

/**
 * 按模板给装修组件套版式。
 *
 * <p>合并规则：**内容以页面为准，版式以模板为准**。
 * 样式（padding/margin/圆角等）同样以模板为准——这正是「换模板」看得见的地方。
 *
 * @param {Object} widget       页面装修组件
 * @param {Object} websiteStyle 网站风格（mall_style）
 * @param {Object} template     模板描述
 * @returns {Object} 套用模板后的组件
 */
export function themedWidget(widget, websiteStyle = {}, template = {}) {
    const tpl = currentTemplate(websiteStyle, template)
    if (!tpl) return widget
    const defaults = tpl.widgets[widget.name] || {}
    const content = { ...(defaults.content || {}), ...(widget.content || {}) }
    // 版式字段一律以模板为准
    LAYOUT_KEYS.forEach((key) => {
        if (defaults.content && key in defaults.content) content[key] = defaults.content[key]
    })
    return { ...widget, styles: { ...widgetStyle(widget.styles), ...(defaults.styles || {}) }, content: themeDemoAssets(content, tpl, widget.name) }
}

/**
 * 把内置演示图换成模板自带的素材。
 *
 * <p>匹配规则：图片地址里含有演示素材键（如 {@code pc/demo/banner-01.svg}）就整体替换；
 * 商家上传的真实图片不含这些键，天然不受影响。
 *
 * @param {Object} content    组件内容
 * @param {Object} tpl        模板注册项
 * @param {string} widgetName 组件名（banner / cube / middle-banner…），不同组件用不同素材
 * @returns {Object} 替换后的内容（原对象不变）
 */
export function themeDemoAssets(content, tpl, widgetName = '') {
    const map = demoAssetMap(tpl, widgetName)
    if (!map || !content || typeof content !== 'object') return content
    const walk = (node) => {
        if (Array.isArray(node)) return node.map(walk)
        if (!node || typeof node !== 'object') return node
        const out = {}
        for (const [key, value] of Object.entries(node)) {
            out[key] = key === 'image' || key === 'bg' || key === 'bg_img' ? swapDemoUrl(value, map) : walk(value)
        }
        return out
    }
    return walk(content)
}

/**
 * 取某个组件在模板下的演示素材替换表。
 *
 * @param {Object} tpl        模板注册项
 * @param {string} widgetName 组件名
 * @returns {Object|null} 形如 { 'goods-tee.svg': '/uploads/mall-demo/tea/goods-tee.svg' }
 */
export function demoAssetMap(tpl, widgetName = '') {
    const all = tpl?.demoAssets
    return (all && (all[widgetName] || all.default)) || null
}

/**
 * 按替换表把一张内置演示图换成模板素材。
 *
 * @param {string} url 原图片地址
 * @param {Object} map 替换表
 * @returns {string} 替换后的地址；不是演示图则原样返回
 */
export function swapDemoUrl(url, map) {
    if (typeof url !== 'string' || !url || !map) return url
    for (const [key, target] of Object.entries(map)) {
        if (!url.includes(key)) continue
        // 保留原图片的协议与域名，小程序 / 独立域名部署时也能取到
        const m = url.match(/^(https?:\/\/[^/]+)/)
        return m ? m[1] + target : target
    }
    return url
}

/**
 * 取当前模板下某组件的演示素材替换表（供不走 themedWidget 的运行时数据使用，
 * 例如店铺列表里的货品图来自 /web/mall/front/shop/list）。
 *
 * @param {Object} websiteStyle 网站风格
 * @param {Object} template     模板描述
 * @param {string} widgetName   组件名
 * @returns {Object|null}
 */
export function templateDemoMap(websiteStyle = {}, template = {}, widgetName = '') {
    return demoAssetMap(currentTemplate(websiteStyle, template), widgetName)
}

/**
 * 页面根节点的模板 class（dc-tpl-tea 等），供样式表按模板整体换肤。
 *
 * @param {Object} websiteStyle 网站风格
 * @param {Object} template     模板描述
 * @returns {string} 形如 "dc-tpl-tea"；未套用模板时为空串
 */
export function templateClass(websiteStyle = {}, template = {}) {
    const tpl = currentTemplate(websiteStyle, template)
    return tpl ? `dc-tpl-${tpl.code}` : ''
}

export function pageStyle(meta = {}, websiteStyle = {}, template = {}) {
    const tpl = currentTemplate(websiteStyle, template)
    const selected = tpl || websiteStyle.list?.find(item => item.className === websiteStyle.current)
    const theme = selected || (websiteStyle.current === 'tea_theme' ? teaTheme : websiteStyle)
    const mode = String(meta.title_type ?? '')
    // 旧数据未记录开关时保留显式背景；“无”必须忽略残留颜色和图片。
    const custom = mode === '2' || (mode === '' && Boolean(meta.bg_color || meta.bg_img || meta.bg_image))
    // title_type=1 表示页面沿用主题默认色。页面一旦套用了模板，模板底色优先于该默认色，
    // 否则模板的暖象牙 / 深色底永远显示不出来；显式自定义背景（custom）仍然优先级最高。
    const inherit = !custom && (Boolean(tpl) || mode !== '1')
    const image = custom ? meta.bg_img || meta.bg_image : inherit ? theme.bg_img || theme.bg_image : ''
    const color = custom ? meta.bg_color : inherit ? theme.bg_color || theme.variable?.['--theme-color'] || theme.color : ''
    return { backgroundColor: color || '#f8f8f8', backgroundImage: image ? `url("${String(image).replace(/["\\\r\n]/g, '')}")` : 'none', backgroundSize: '100% auto', backgroundRepeat: 'no-repeat' }
}
export async function loadDecorationRuntime(post) {
    const [legacy, shops] = await Promise.allSettled([
        post('/web/mall/front/index/legacyHome', {}),
        post('/web/mall/front/shop/list', { page: 1, limit: 30 })
    ])
    const runtime = { modules: {}, shops: [], errors: [] }
    if (legacy.status === 'fulfilled' && legacy.value?.code === 1) {
        try {
            const page = JSON.parse(legacy.value.data.pageData)
            for (const module of page.moduleList || []) runtime.modules[module.moduleSign] = module.moduleJson
        } catch { runtime.errors.push('活动数据解析失败') }
    } else runtime.errors.push('活动数据加载失败')
    if (shops.status === 'fulfilled' && shops.value?.code === 1) runtime.shops = shops.value.data?.lists || []
    else runtime.errors.push('店铺数据加载失败')
    return runtime
}
