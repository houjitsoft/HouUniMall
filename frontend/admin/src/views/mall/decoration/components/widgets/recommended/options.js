export default () => ({
    title: '推荐商品',
    name: 'recommended',
    content: {
        enabled: 1,
        get_type: 1,
        goodsLists: {
            list: [{}]
        },
        showNums: 10,
        sort_type: 1,
        sort: ''
    },
    styles: {}
})
