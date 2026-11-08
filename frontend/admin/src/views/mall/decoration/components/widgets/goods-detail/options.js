export default () => ({
    title: '商品详情',
    name: 'goods-detail',
    content: {
        enabled: 1,
        navigation_bar: 1,
        top_menu: [1, 2, 3],
        top_share: 1,
        goods_image: 1,
        activeIndex: 1,
        vip_style: {
            style: 1,
            title: '会员专属价',
            image_type: 1,
            image: ''
        },
        seckill_style: {
            title: '限时秒杀',
            image_type: 1,
            image: ''
        },
        team_style: {
            title: '超值拼团',
            image_type: 1,
            image: ''
        },
        info: [1, 2, 3]
    },
    canMove: 0,

    styles: {}
})
