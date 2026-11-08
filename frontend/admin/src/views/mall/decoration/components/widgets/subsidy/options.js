/**
 * 国补（国家补贴/以旧换新补贴）装修组件默认数据。
 *
 * name 必须与文件夹名 subsidy 一致（被 widgets/index.js 的正则推导为组件 key）。
 * content 下的字段即 attr.vue 绑定的配置项；这里只承载「展示配置」，
 * 真实的补贴金额/资格由后端 national-subsidy 插件按用户、地区、类目实时计算。
 */
export default () => ({
    title: '国家补贴',
    name: 'subsidy',
    content: {
        enabled: 1,
        // 商品来源：1 手动选择，2 指定分类
        get_type: 1,
        goodsLists: {
            list: [{}]
        },
        category_id: '',
        // 展示方式：1 大图 2 单列 3 两列 4 三列 5 横向滑动
        show_type: 1,
        // 显示内容：1 标题 2 价格 3 划线价 4 销量 5 商户名
        show_content: [1, 2, 3, 4],
        // 补贴说明文案（静态展示，真实抵扣以结算页为准）
        subsidy_tip: '政府补贴 结算时自动抵扣'
    },
    styles: {}
})
