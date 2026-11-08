<!-- eslint-disable vue/valid-v-model -->
<template>
    <div>
        <div
            class="flex bg-white rounded-lg p-2 mb-[10px]"
            v-for="item in content?.showNum"
            :key="item"
        >
            <el-image
                :src="storeData[item - 1] ? storeData[item - 1]?.logo : shopImg"
                class="w-[50px] h-[50px] flex-none"
            ></el-image>
            <div class="flex flex-col justify-between ml-2">
                <div class="text_hidden_line1">{{ storeData[item - 1]?.name || '-' }}</div>

                <el-rate
                    :model-value="storeData[item - 1]?.score"
                    disabled
                    show-score
                    score-templat="100"
                    text-color="#ff9900"
                    score-template="{value}"
                />
            </div>
            <div class="form-tips ml-auto self-end flex-none">
                {{ storeData[item - 1]?.visit_num || '-' }}人进店
            </div>
        </div>
    </div>
</template>

<script setup>
import shopImg from '../images/shop.png'
const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    },
    storeData: {
        type: Object,
        default: () => ({})
    }
})
</script>

<style scoped lang="scss"></style>
