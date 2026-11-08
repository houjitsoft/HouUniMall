<template>
    <div>
        <div class="header pt-[11px]">
            <div class="search w-[223px] h-[32px] ml-[45px]">
                <div class="search-con flex items-center px-[15px]">
                    <icon name="el-icon-Search" :size="17" />
                    <span class="ml-[5px]">请输入关键词搜索</span>
                </div>
            </div>
        </div>
        <div class="px-2">
            <div class="bg-white h-[64px] rounded-lg flex items-center justify-between p-2">
                <div class="flex">
                    <img src="./images/shopimg.png" alt="" />
                    <div class="ml-2 flex flex-col justify-around">
                        <div>顽刻olieskate</div>
                        <div class="flex">
                            <img src="./images/image2.png" alt="" />
                            <img src="./images/image1.png" alt="" class="ml-1" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'
import type options from './options'
import DecorationImg from '../../decoration-img.vue'
import useAppStore from '@/stores/modules/app'
type OptionsType = ReturnType<typeof options>
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    },
    height: {
        type: String,
        default: '170px'
    }
})

const { getImageUrl } = useAppStore()
</script>

<style lang="scss" scoped>
.header {
    background: url(./images/nav.png) no-repeat;
    background-size: 100% 54px;
    height: 54px;
}
.search {
    .search-con {
        width: 223px;
        height: 32px;
        border-radius: 36px;
        background: #ffffff;
        color: #999999;
    }
}
.shop-info {
    background-repeat: no-repeat;
    background-size: 100% auto;

    .store-tag {
        display: inline-block;
        background: linear-gradient(267deg, #ff2c3c 0%, #f52e99 100%);
        border-radius: 6px;
        padding: 2px 4px;
    }

    .tab {
        // margin-top: 20px;
        height: 40px;
        display: flex;
        div {
            width: 25%;
            padding-left: 16px;
            line-height: 30px;
            color: #ffffff;
            font-size: 14px;
            font-weight: 500;
        }
        &-active {
            span {
                padding-bottom: 4px;
                border-bottom: 3px solid #ffffff;
            }
        }
    }
}
</style>
