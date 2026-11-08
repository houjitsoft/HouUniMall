<template>
    <div class="subsidy-widget">
        <!-- 模块标题 + 静态补贴说明 -->
        <div class="subsidy-widget__header flex items-center justify-between px-2 pt-2">
            <div class="text-[16px] font-medium">{{ content.title || '国家补贴' }}</div>
            <div v-if="content.subsidy_tip" class="text-[12px]" style="color: #b91c1c">
                {{ content.subsidy_tip }}
            </div>
        </div>

        <!-- 单列 -->
        <div class="flex flex-wrap p-2 justify-between" v-if="content.show_type == 2">
            <div
                v-for="item in content.goodsLists.list"
                :key="item.id"
                class="bg-white mb-2 rounded-lg w-full h-[100px] flex relative"
            >
                <div class="subsidy-badge">国家补贴</div>
                <el-image :src="getImageUrl(item.image)" class="w-[100px] h-[100px] rounded-lg" fit="cover">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="p-2 flex-1 flex flex-col justify-between">
                    <div class="text_hidden" v-if="content.show_content.includes(1)">
                        {{ item.name || '商品名称' }}
                    </div>
                    <div>
                        <div class="flex items-center">
                            <span class="text-[10px]">¥</span>
                            <span class="ml-[2px]">{{ item.price || 0 }}</span>
                            <span
                                class="text-[10px] ml-2 line-through"
                                v-if="content.show_content.includes(3)"
                                style="color: #999; font-size: 10px"
                                >¥</span
                            >
                            <span
                                class="ml-[2px] line-through"
                                v-if="content.show_content.includes(3)"
                                style="color: #999; font-size: 10px"
                            >
                                {{ item.max_line_price || 0 }}
                            </span>
                            <span
                                class="ml-auto"
                                style="color: #999; font-size: 10px"
                                v-if="content.show_content.includes(4)"
                            >
                                已售{{ item.sale_num || 0 }}
                            </span>
                        </div>
                        <div class="text_hidden" v-if="content.show_content.includes(5)">
                            <span style="color: #999; font-size: 10px">
                                {{ item.shopName || '店铺名称' }}
                            </span>
                            <span class="ml-1" style="font-size: 10px"> 进店 </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 大图 / 两列 / 三列 / 横向滑动 -->
        <div
            class="flex flex-wrap p-2 w-full"
            :class="{ 'pt-0': content.show_type == 1, 'justify-between': content.show_type == 3 }"
            v-else
        >
            <div
                v-for="(item, index) in content.goodsLists.list"
                :key="item.id"
                class="bg-white mb-2 rounded-lg mr-[3px] relative"
                :class="
                    content.show_type == 1
                        ? 'w-full'
                        : content.show_type == 3
                        ? 'w-[49%]'
                        : 'w-[32%]'
                "
                v-show="(index < 3 && content.show_type == 5) || content.show_type != 5"
            >
                <div class="subsidy-badge subsidy-badge--sm">国补</div>
                <el-image
                    :src="getImageUrl(item.image)"
                    class="w-full rounded-t-lg"
                    fit="cover"
                    :class="
                        content.show_type == 1
                            ? 'h-[300px]'
                            : content.show_type == 3
                            ? 'h-[200px]'
                            : 'h-[130px]'
                    "
                >
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="text_hidden px-2" v-if="content.show_content.includes(1)">
                    {{ item.name || '商品名称' }}
                </div>
                <div class="px-2 flex items-center pb-2 w-full">
                    <div class="w-full">
                        <span class="text-[10px]">¥</span>
                        <span class="ml-[2px]">{{ item.price || 0 }}</span>
                        <span
                            class="text-[10px] ml-2 line-through"
                            v-if="content.show_content.includes(3)"
                            style="color: #999; font-size: 10px"
                            >¥</span
                        >
                        <span
                            class="ml-[2px] line-through"
                            v-if="content.show_content.includes(3)"
                            style="color: #999; font-size: 10px"
                        >
                            {{ item.max_line_price || 0 }}
                        </span>
                        <div
                            class="ml-auto"
                            style="color: #999; font-size: 10px"
                            v-if="content.show_type == 4 || content.show_type == 5"
                            v-show="content.show_content.includes(4)"
                        >
                            已售{{ item.sale_num || 0 }}
                        </div>
                        <span
                            class=""
                            style="color: #999; font-size: 10px"
                            v-else
                            v-show="content.show_content.includes(4)"
                        >
                            已售{{ item.sale_num || 0 }}
                        </span>
                        <div
                            class="text_hidden"
                            v-if="content.show_content.includes(5)"
                            v-show="
                                content.show_type == 1 ||
                                content.show_type == 2 ||
                                content.show_type == 3
                            "
                        >
                            <span style="color: #999; font-size: 10px">
                                {{ item.shopName || '店铺名称' }}
                            </span>
                            <span class="ml-1" style="font-size: 10px"> 进店 </span>
                        </div>
                    </div>
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
.subsidy-badge {
    position: absolute;
    top: 6px;
    left: 6px;
    z-index: 2;
    padding: 1px 6px;
    border-radius: 3px;
    background: #dc2626;
    color: #fff;
    font-size: 12px;
    line-height: 18px;
    font-weight: 600;
}
.subsidy-badge--sm {
    font-size: 10px;
    padding: 0 4px;
    line-height: 16px;
}
</style>
