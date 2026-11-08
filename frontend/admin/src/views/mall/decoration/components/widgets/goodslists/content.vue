<template>
    <div class="">
        <div class="flex flex-wrap p-2 justify-between" v-if="content.show_type == 2">
            <div
                v-for="item in content.goodsLists.list"
                :key="item.id"
                class="bg-white mb-2 rounded-lg w-full h-[100px] flex"
            >
                <el-image :src="item.image" class="w-[100px] h-[100px] rounded-lg" fit="cover">
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
                            <span class="text-[10px]" v-if="content.show_content.includes(2)"
                                >¥</span
                            >
                            <span class="ml-[2px]" v-if="content.show_content.includes(2)">
                                {{ item.price || 0 }}
                            </span>
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
                <!-- <div class="self-end p-2">
                    <div
                        class="add-btn ml-auto"
                        v-if="content.cart_btn && content.btn_type == 1 && content.cart_style == 1"
                    >
                        +
                    </div>
                    <div
                        class="flex justify-center items-center cart-btn ml-auto self-end"
                        v-if="content.cart_btn && content.btn_type == 1 && content.cart_style == 2"
                    >
                        <img src="./icon.png" />
                    </div>
                </div> -->
            </div>
        </div>
        <div
            class="flex flex-wrap p-2 w-full"
            :class="{ 'pt-0': content.show_type == 1, 'justify-between': content.show_type == 3 }"
            v-else
        >
            <div
                v-for="(item, index) in content.goodsLists.list"
                :key="item.id"
                class="bg-white mb-2 rounded-lg mr-[3px]"
                :class="
                    content.show_type == 1
                        ? 'w-full'
                        : content.show_type == 3
                        ? 'w-[49%]'
                        : 'w-[32%]'
                "
                v-show="(index < 3 && content.show_type == 5) || content.show_type != 5"
            >
                <el-image
                    :src="item.image"
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
                        <span class="text-[10px]" v-if="content.show_content.includes(2)">¥</span>
                        <span class="ml-[2px]" v-if="content.show_content.includes(2)">
                            {{ item.price || 0 }}
                        </span>
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
                    <!-- <div
                        class="add-btn ml-auto self-end"
                        v-if="content.cart_btn && content.btn_type == 1 && content.cart_style == 1"
                    >
                        +
                    </div>
                    <div
                        class="flex justify-center items-center cart-btn ml-auto self-end"
                        v-if="content.cart_btn && content.btn_type == 1 && content.cart_style == 2"
                    >
                        <img src="./icon.png" />
                    </div>
                    <div
                        class="flex justify-center items-center ml-auto self-end rounded-[50%]"
                        v-if="content.cart_btn && content.btn_type == 2"
                    >
                        <el-image
                            :src="getImageUrl(content.cart_image)"
                            class="w-[20px] h-[20px] rounded-[50%]"
                            fit="cover"
                        >
                            <template #error>
                                <div
                                    class="w-full h-full flex justify-center items-center bg-[#ebf2ff]"
                                >
                                    <el-icon :size="28"><PictureFilled /></el-icon>
                                </div>
                            </template>
                        </el-image>
                    </div> -->
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import DecorationImg from '../../decoration-img.vue'
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
.add-btn {
    width: 20px;
    height: 20px;
    line-height: 17px;
    border-radius: 50%;
    background-color: #1a69fc;
    text-align: center;
    color: white;
    font-size: 16px;
}
.cart-btn {
    width: 20px;
    height: 20px;
    line-height: 17px;
    border-radius: 50%;
    background-color: #1a69fc;
}
</style>
