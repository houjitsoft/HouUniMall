<template>
    <div class="">
        <div class="flex justify-between p-2" v-if="content.type == 2">
            <div class="flex flex-col bg-white w-[68px]text-center rounded-lg p-1">
                <img src="./images/cate1.png" alt="" class="w-[42px] h-[42px] mx-auto" />
                <div class="mt-1 text-[#F8292F]">全部商品</div>
            </div>
            <div class="bg-white w-[68px] text-center rounded-lg p-1 flex flex-col">
                <img src="./images/cate2.png" alt="" class="w-[42px] h-[42px] mx-auto" />
                <div class="mt-1">家具家电</div>
            </div>
            <div class="bg-white w-[68px] text-center rounded-lg p-1 flex flex-col">
                <img src="./images/cate3.png" alt="" class="w-[42px] h-[42px] mx-auto" />
                <div class="mt-1">服饰美妆</div>
            </div>
            <div class="bg-white w-[68px]text-center rounded-lg p-1 flex flex-col">
                <img src="./images/cate4.png" alt="" class="w-[42px] h-[42px] mx-auto" />
                <div class="mt-1">食品饮料</div>
            </div>
        </div>
        <div class="bg-white p-2 rounded-lg">
            <div class="flex justify-between p-2" v-if="content.type == 1">
                <div class="text-[#B67B2E]">全部商品</div>
                <div>家具家电</div>
                <div>服饰美妆</div>
                <div>食品饮料</div>
            </div>
            <div class="flex mb-2">
                <img src="./images/image1.png" alt="" class="w-[80px] h-[80px]" />
                <div class="bg-white p-1 rounded-b-lg ml-2">
                    <div>品牌秋季男士卫衣灰色时尚戴帽...</div>
                    <div class="flex items-center">
                        <span class="text-[10px]">已抢100件</span>
                    </div>
                    <div class="text-[red] flex items-center mt-2">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span class="line-through text-[10px] text-[#999]">原价¥899</span>
                    </div>
                </div>
            </div>
            <div class="flex mb-2">
                <img src="./images/image2.png" alt="" class="w-[80px] h-[80px]" />
                <div class="bg-white p-1 rounded-b-lg ml-2">
                    <div>品牌秋季男士卫衣灰色时尚戴帽...</div>
                    <div class="flex items-center">
                        <span class="text-[10px]">已抢100件</span>
                    </div>
                    <div class="text-[red] flex items-center mt-2">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span class="line-through text-[10px] text-[#999]">原价¥899</span>
                    </div>
                </div>
            </div>
            <div class="flex mb-2">
                <img src="./images/image3.png" alt="" class="w-[80px] h-[80px]" />
                <div class="bg-white p-1 rounded-b-lg ml-2">
                    <div>品牌秋季男士卫衣灰色时尚戴帽...</div>
                    <div class="flex items-center">
                        <span class="text-[10px]">已抢100件</span>
                    </div>
                    <div class="text-[red] flex items-center mt-2">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span class="line-through text-[10px] text-[#999]">原价¥899</span>
                    </div>
                </div>
            </div>
        </div>
        <!-- <div class="box rounded-lg flex p-2 justify-between">
            <div class="w-[30%]">
                <img src="./images/image1.png" alt="" />
                <div class="bg-white p-1 rounded-b-lg">
                    <div>马克图布联名..</div>
                    <div class="text-[red] flex items-center">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span
                            class="bg-[#F2D573] text-[8px] text-[black] p-[1px] rounded-[3px] text-center"
                        >
                            直降88
                        </span>
                    </div>
                </div>
            </div>
            <div class="w-[30%]">
                <img src="./images/image2.png" alt="" />
                <div class="bg-white p-1 rounded-b-lg">
                    <div>马克图布联名..</div>
                    <div class="text-[red] flex items-center">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span
                            class="bg-[#F2D573] text-[8px] text-[black] p-[1px] rounded-[3px] text-center"
                        >
                            直降88
                        </span>
                    </div>
                </div>
            </div>
            <div class="w-[30%]">
                <img src="./images/image3.png" alt="" />
                <div class="bg-white p-1 rounded-b-lg">
                    <div>马克图布联名..</div>
                    <div class="text-[red] flex items-center">
                        <span class="text-[10px]">¥</span>
                        <span class="mr-2">268</span>
                        <span
                            class="bg-[#F2D573] text-[8px] text-[black] p-[1px] rounded-[3px] text-center"
                        >
                            直降88
                        </span>
                    </div>
                </div>
            </div>
        </div> -->
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
.box {
    background: rgba(255, 255, 255, 0.3);
}
</style>
