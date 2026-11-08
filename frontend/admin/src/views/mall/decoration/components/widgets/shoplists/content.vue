<template>
    <div class="p-2">
        <div class="flex bg-white p-2 rounded-t-lg">
            <div v-if="content.title_type == 1">
                {{ content.title }}
            </div>
            <div v-else>
                <el-image
                    :src="getImageUrl(content.titile_img)"
                    class="h-[20px] w-[56px]"
                    fit="cover"
                >
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="14"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="ml-auto" :style="{ color: content.right_color }" v-if="content.right_btn">
                {{ content.right_title }}
            </div>
        </div>
        <div class="bg-white p-2 rounded-b-lg mb-2" v-for="(item, index) in 2" :key="item.id">
            <div class="flex items-center">
                <el-image
                    :src="index == 0 ? shop1 : shop2"
                    class="h-[44px] w-[44px] rounded-lg"
                    fit="cover"
                >
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-[#ebf2ff]">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
                <div class="ml-2">
                    <div>顽刻olieskate</div>
                    <div class="flex mt-[5px]">
                        <img src="./shopself.png" alt="" class="mr-2" />
                        <img src="./star.png" alt="" />
                    </div>
                </div>
                <div class="ml-auto btn">进店</div>
            </div>

            <div class="flex text-center" v-if="content.show_type == 1">
                <div class="mr-2 w-[33.3%]">
                    <img src="./img1.png" alt="" />
                    <div>¥268</div>
                </div>
                <div class="mr-2 w-[33.3%]">
                    <img src="./img2.png" alt="" />
                    <div>¥268</div>
                </div>
                <div class="mr-2 w-[33.3%]">
                    <img src="./img3.png" alt="" />
                    <div>¥268</div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import DecorationImg from '../../decoration-img.vue'
import useAppStore from '@/stores/modules/app'
import shop1 from './shop1.png'
import shop2 from './shop2.png'
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
.btn {
    background: rgba(182, 123, 46, 0.1);
    text-align: center;
    border-radius: 20px;
    color: #b67b2e;
    width: 70px;
    height: 30px;
    line-height: 30px;
}
</style>
