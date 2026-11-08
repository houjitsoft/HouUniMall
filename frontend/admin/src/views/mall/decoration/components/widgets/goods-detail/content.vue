<template>
    <div class="goods-detail h-[360px]">
        <img src="./images/tab2.png" class="absolute top-0 left-0 z-10 w-full" />
        <img src="./images/good-image.png" class="w-full" />
    </div>
    <div class="">
        <div
            v-if="
                content.activeIndex == 1 ||
                (content.activeIndex == 2 && content.vip_style.style == 1)
            "
            class="p-3"
        >
            <span class="text-[#FD463E]">
                <span class="text-xs">¥</span>
                <span class="text-2xl">709.</span>
                <span class="text-xs">99</span>
                <span class="text-xs ml-1">会员价</span>
            </span>
            <span class="line-through text-[#999] text-xs ml-2">原价¥899</span>
        </div>
        <div
            v-if="content.activeIndex == 2 && content.vip_style.style == 2"
            class="bg-[#f9879f] p-3 rounded-lg h-[120px]"
            :style="{
                backgroundImage:
                    content.vip_style.image_type == 1
                        ? ''
                        : `url(${getImageUrl(content.vip_style.image)})`
            }"
        >
            <div class="text-white flex justify-between items-center">
                <div class="text-xl">{{ content.vip_style.title }}</div>
                <div class="text-xs">距离活动结束 24:35:46</div>
            </div>
            <div class="bg-white p-4 rounded-lg mt-2">
                <span class="text-[#FD463E]">
                    <span class="text-xs">¥</span>
                    <span class="text-2xl">709.</span>
                    <span class="text-xs">99</span>
                </span>
                <span class="line-through text-[#999] text-xs ml-2">原价¥899</span>
            </div>
        </div>
        <div
            v-if="content.activeIndex == 3"
            class="bg-[#e1293d] p-3 rounded-lg h-[120px]"
            :style="{
                backgroundImage:
                    content.seckill_style.image_type == 1
                        ? ''
                        : `url(${getImageUrl(content.seckill_style.image)})`
            }"
        >
            <div class="text-white flex justify-between items-center">
                <div class="text-xl">{{ content.seckill_style.title }}</div>
                <div class="text-xs">距离活动结束 24:35:46</div>
            </div>
            <div class="bg-white p-4 rounded-lg mt-2">
                <span class="text-[#FD463E]">
                    <span class="text-xs">¥</span>
                    <span class="text-2xl">709.</span>
                    <span class="text-xs">99</span>
                </span>
                <span class="line-through text-[#999] text-xs ml-2">原价¥899</span>
            </div>
        </div>
        <div
            v-if="content.activeIndex == 4"
            class="bg-[#f08e31] p-3 rounded-lg h-[120px]"
            :style="{
                backgroundImage:
                    content.team_style.image_type == 1
                        ? ''
                        : `url(${getImageUrl(content.team_style.image)})`
            }"
        >
            <div class="text-white flex justify-between items-center">
                <div class="text-xl">{{ content.team_style.title }}</div>
                <div class="text-xs">距离活动结束 24:35:46</div>
            </div>
            <div class="bg-white p-4 rounded-lg mt-2">
                <span class="text-[#FD463E]">
                    <span class="text-xs">¥</span>
                    <span class="text-2xl">709.</span>
                    <span class="text-xs">99</span>
                </span>
                <span class="line-through text-[#999] text-xs ml-2">原价¥899</span>
            </div>
        </div>
        <div class="mt-1 px-3">
            <span class="text-xl font-medium">
                白月光商场同款JNBY/江南布衣东女装24冬连帽云朵羽绒服女5O 0C12340
            </span>
        </div>
        <div class="mt-1 flex justify-between text-xs text-[#999] p-3">
            <div v-if="content.info.includes(1)">已售1685</div>
            <div v-if="content.info.includes(2)">库存2644</div>
            <div v-if="content.info.includes(3)">预览8989</div>
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
