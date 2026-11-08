<template>
    <goods-select
        v-model="selectGoods"
        :max-num="25"
        type="multiple"
        :extend="{
            name: '优惠券',
            showTable: false
        }"
        :disabled="disabled"
    >
        <template #btn>
            <el-button type="primary" :disabled="disabled"> 选择商品</el-button>
            <span class="ml-4 text-[#999]"> 最多支持选择25件商品 </span>
        </template>
    </goods-select>
    <el-table
        v-if="selectGoods.length"
        class="my-4"
        style="max-width: 800px"
        ref="paneTable"
        :data="selectGoods"
    >
        <el-table-column label="商品编码" prop="code" min-width="80" />
        <el-table-column label="商品信息" min-width="200">
            <template #default="{ row }">
                <div class="flex items-center">
                    <el-image
                        class="flex-none"
                        style="width: 58px; height: 58px"
                        :src="row.image"
                    />
                    <div class="ml-4">
                        {{ row.name }}
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="商品价格" min-width="120">
            <template #default="{ row }"> ¥{{ row.min_price }} </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
            <template #header>
                <el-button type="primary" :link="true" :disabled="disabled" @click="removeAll">
                    清空所有
                </el-button>
            </template>
            <template #default="{ row, $index }">
                <el-button :link="true" :disabled="disabled" @click="removeGoods($index)">
                    删除
                </el-button>
            </template>
        </el-table-column>
    </el-table>
</template>
<script setup lang="ts">
import { useVModels } from '@vueuse/core'
import feedback from '@/utils/feedback'

const props = withDefaults(
    defineProps<{
        modelValue?: any
        disabled: boolean
    }>(),
    {
        modelValue: {},
        disabled: false
    }
)

const emit = defineEmits(['update:modelValue'])
const { modelValue: selectGoods } = useVModels(props, emit)

const removeGoods = (index: number) => {
    selectGoods.value.splice(index, 1)
}

const removeAll = async () => {
    try {
        await feedback.confirm('是否清空所有已选择商品？')

        selectGoods.value = []
    } catch (error) {
        console.log(error)
    }
}
</script>
