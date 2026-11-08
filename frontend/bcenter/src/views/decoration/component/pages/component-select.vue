<template>
    <el-card class="!border-none flex" shadow="never">
        <div class="flex">
            <el-page-header @back="$router.back()" title=" " />
            <el-input
                v-model="pagetitle"
                placeholder="请输入页面名称"
                show-word-limit
                maxlength="20"
                @change="handlechange"
            ></el-input>
        </div>
    </el-card>
    <el-collapse
        v-model="activeNames"
        class="w-[290px] ml-2"
        v-for="item in decorateMenu"
        :key="item.name"
    >
        <el-collapse-item :title="item.title" :name="item.name">
            <div class="flex flex-wrap">
                <div v-for="citem in item.children" :key="citem.name">
                    <div
                        class="card flex flex-col justify-around items-center"
                        draggable="true"
                        @dragstart="dragStart(citem)"
                        @dragend="dragend"
                    >
                        <img :draggable="false" class="item-icon" :src="citem.icon" mode="" />
                        <div>
                            {{ citem.title }}
                        </div>
                    </div>
                </div>
            </div>
        </el-collapse-item>
    </el-collapse>
</template>
<script lang="ts" setup>
import { decorateMenu } from '@/utils/decoration'
const props = defineProps({
    title: {
        type: String,
        default: ''
    }
})
const pagetitle = ref('')
watch(
    () => props.title,
    (value) => {
        pagetitle.value = value
    },
    {
        immediate: true
    }
)
const emit = defineEmits<{
    (event: 'addwidgetName', value: any): void
    (event: 'update:title', value: any): void
    (event: 'resetdragIndex', value: any): void
}>()
const activeNames = ref(['basics', 'marketing', 'tools'])
const dragStart = ({ name }: any) => {
    emit('addwidgetName', name)
}
const dragend = () => {
    emit('resetdragIndex', -2)
}
const handlechange = (e: any) => {
    emit('update:title', e)
}
</script>
<style lang="scss" scoped>
:deep(.el-card__body) {
    padding: 20px 10px;
}
.card {
    background-color: skyblue;
    width: 72px;
    height: 72px;
    margin-right: 18px;
    margin-top: 16px;
    cursor: move;
    border-radius: 5px;
    overflow: hidden;
    background-color: #fff;
    box-shadow: 0px 0px 6px rgba(0, 0, 0, 0.06);
    box-sizing: border-box;
    .item-icon {
        width: 26px;
        height: 26px;
    }
    &:hover {
        z-index: 99;
        border: 1px solid #4073fa;
    }
    &:nth-of-type(3n) {
        margin-right: 0;
    }
}
</style>
