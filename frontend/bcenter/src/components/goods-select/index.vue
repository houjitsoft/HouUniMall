<template>
    <div class="goods-select-popup">
        <!-- 商品选择 -->
        <g-select
            v-model="selectData"
            :type="type"
            :disabled="disabled"
            :extend="extend"
        ></g-select>

        <!-- 商品已选择信息 -->
        <template v-if="extend.showTable">
            <g-info v-model="selectData"></g-info>
        </template>

        <!-- 商品表格----用于单个商品及编辑商品规格价格等 -->
        <template v-if="extend.showTable">
            <g-spec-table v-model="selectData" :disabled="disabled" :extend="extend"></g-spec-table>
        </template>
    </div>
</template>
<script lang="ts" setup>
import { useVModels } from '@vueuse/core'
import { withDefaults } from 'vue'
const GSelect = defineAsyncComponent(() => import('./select.vue'))
const GInfo = defineAsyncComponent(() => import('./goods-info.vue'))
const GSpecTable = defineAsyncComponent(() => import('./spec-table.vue'))

const props = withDefaults(
    defineProps<{
        modelValue?: any
        type?: string
        disabled?: boolean
        extend?: any
    }>(),
    {
        modelValue: [],
        // 类型: 多选(multiple) ｜ 单选(single)
        type: 'single',
        // 是否禁用
        disabled: false,
        // 拓展extend
        extend: {
            name: '',
            showTable: true
        }
    }
)

const emit = defineEmits(['update:modelValue', 'update:goods_id', 'update:sku'])
const { modelValue: selectData } = useVModels(props, emit)

watch(
    () => selectData.value,
    (value) => {
        try {
            // 选择或修改完成后将已选数据抛出到外部
            const newSkuArr = value.sku?.map((item: any) => {
                return {
                    goods_sku_id: item.id,
                    price: item.current_price,
                    total_stock: item.current_stock
                }
            })
            if (Array.isArray(value)) {
                emit('update:sku', newSkuArr)
                emit(
                    'update:goods_id',
                    value.map((item) => item.id)
                )
            } else {
                emit('update:sku', newSkuArr)
                emit('update:goods_id', value.id)
            }
        } catch (error) {
            console.log('数据有误', error)
        }
    },
    { deep: true }
)
</script>
