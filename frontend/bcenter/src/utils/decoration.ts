import icon_notice from '@/assets/images/icon_notice.png'
import icon_banner from '@/assets/images/icon_banner.png'
import icon_nav from '@/assets/images/icon_nav.png'
import icon_choice from '@/assets/images/icon_choice.png'
import icon_cube from '@/assets/images/icon_cube.png'
import icon_goods from '@/assets/images/icon_goods.png'
import icon_title from '@/assets/images/icon_title.png'
import icon_blank from '@/assets/images/icon_blank.png'
export const decorateMenu = [
    {
        title: '基础组件',
        name: 'basics',
        children: [
            { title: '公告', name: 'notice', icon: icon_notice },
            { title: '轮播图', name: 'banner', icon: icon_banner },
            { title: '导航组', name: 'nav', icon: icon_nav },
            { title: '选项卡', name: 'choice', icon: icon_choice },
            { title: '热卖推荐', name: 'hot', icon: icon_choice },
            { title: '图片魔方', name: 'cube', icon: icon_cube },
            { title: '商品列表', name: 'goodslists', icon: icon_goods },
            { title: '推荐商品', name: 'goodsrecomded', icon: icon_goods }
        ]
    },
    {
        title: '工具组件',
        name: 'tools',
        children: [
            { title: '标题栏', name: 'title', icon: icon_title },
            { title: '辅助空白', name: 'blank', icon: icon_blank }
        ]
    }
]
