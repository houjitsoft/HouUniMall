<template>
    <div class="p-2">
        <div
            class="h-[64px] p-3 flex w-full items-center bg-white rounded-lg"
            :style="{
                backgroundImage: content.showBg == 1 ? '' : `url(${getImageUrl(content.image)})`
            }"
            style="background-size: cover"
        >
            <div>
                <img src="./images/logo.png" alt="" />
            </div>
            <div class="ml-2">
                <div class="font-medium">顽刻olieskate</div>
                <div class="flex">
                    <div class="tip">自营</div>
                </div>
            </div>
            <div style="margin-left: auto" class="enter-shop">进店</div>
        </div>
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
.tip {
    background-color: #7c4d30;
    color: white;
    font-size: 9px;
    padding: 2px 5px;
    border-radius: 4px;
}
.enter-shop {
    width: 70px;
    height: 30px;
    line-height: 30px;
    text-align: center;
    border-radius: 20px;
    background: rgba(182, 123, 46, 0.1);
    color: #b67b2e;
}
</style>
