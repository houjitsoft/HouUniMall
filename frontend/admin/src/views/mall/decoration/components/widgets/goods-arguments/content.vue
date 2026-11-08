<template>
    <div class="p-2">
        <div class="bg-white p-2 rounded-lg">
            <div class="flex" v-if="content.info.includes(1)">
                <div class="text-[#999]">配送</div>
                <div class="ml-2">
                    <div>快递：6.00</div>
                    <div class="form-tips">配送至:广州市 天河区 河滨路街道</div>
                </div>
            </div>
            <div class="flex" v-if="content.info.includes(2)">
                <div class="text-[#999] shrink-0">选择</div>
                <div class="ml-2">
                    <div>已选:双胆旗舰P1至尊版，银色，60L，双胆旗舰P1至尊版，银色</div>
                </div>
            </div>
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
.a {
    background-image: url();
}
</style>
