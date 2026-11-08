export default () => ({
    title: '选项卡',
    name: 'choice',
    content: {
        enabled: 1,
        data: [
            {
                title: '口碑推荐',
                sub_title: '好货专区',
                goods_source: 1,
                goods_num: 10,
                goodsLists: {
                    list: [
                        {
                            id: '',
                            image: '',
                            name: '',
                            price: ''
                        }
                    ]
                }
            }
        ],
        sort_type: 1,
        use_sort: 1,
        choice_type: 1,
        show_type: 1
    },
    styles: {}
})
