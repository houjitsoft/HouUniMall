export default () => ({
    title: '我的订单',
    name: 'user-order',
    disabled: 0,

    content: {
        title: '我的订单',
        data: [
            {
                image: '',
                name: '待付款',
                display: true,
                link: '/packageA/pages/order_list/order_list?type=0'
            },
            {
                image: '',
                name: '待发货',
                display: true,
                link: '/packageA/pages/order_list/order_list?type=1'
            },
            {
                image: '',
                name: '待收货',
                display: true,
                link: '/packageA/pages/order_list/order_list?type=2'
            },
            {
                image: '',
                name: '商品评价',
                display: true,
                link: '/packageA/pages/comment_list/comment_list'
            },
            {
                image: '',
                name: '售后退款',
                display: true,
                link: '/packageA/pages/after_sales/after_sales'
            }
        ]
    },
    styles: {}
})
