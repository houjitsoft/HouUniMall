<template>
    <div
        class="user-info flex items-center px-[25px]"
        :style="{
            background: content.bgConfig == 1 ? '' : `url(${getImageUrl(content.image)})`,
            height: '115px',
            'background-position': 'top',
            'background-size': '100% auto'
        }"
    >
        <img src="./images/default_avatar.png" class="w-[60px] h-[60px]" alt="" />
        <div class="text-white text-[18px] ml-[10px]">未登录</div>
    </div>
</template>
<script setup>
import useAppStore from '@/stores/modules/app'

const appStore = useAppStore()
/**
 * 图片地址拼接。不调 appStore.getImageUrl()：该 Pinia action 在装修编辑器
 * 这个懒加载 chunk 里取不到（报 "o.getImageUrl is not a function"）。
 * 直接读 state 的 oss_domain 自己拼，逻辑与 store action 一致。
 */
const getImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    let domain = ''
    try {
        domain = appStore.config?.oss_domain || ''
    } catch (e) {
        domain = ''
    }
    return domain + url
}

const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    }
})
</script>

<style lang="scss" scoped>
.user-info {
    background: url(./images/my_topbg.png);
    height: 115px;
    background-position: bottom;
    background-size: 100% auto;
}
</style>
