<template>
    <!-- 表格 -->
    <el-table
        ref="tableRef"
        size="large"
        :data="selectData.sku"
        class="mt-4"
        v-if="selectData?.sku?.length"
    >
        <el-table-column label="商品规格" min-width="240">
            <template #default="{ row }">
                <div class="flex">
                    <el-image
                        fit="cover"
                        :src="row.image"
                        v-if="row.image"
                        class="flex-none w-[58px] h-[58px]"
                    />
                    <div class="ml-4 overflow-hidden">
                        <el-tooltip
                            effect="dark"
                            placement="top"
                            :content="row?.sku_value_arr?.join(',') || '默认'"
                        >
                            <div class="text-base line-2">
                                {{ row?.sku_value_arr?.join(',') || '默认' }}
                            </div>
                        </el-tooltip>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column label="商品价格" prop="price" min-width="120">
            <template #default="{ row }"> ¥{{ row.price }} </template>
        </el-table-column>
        <el-table-column label="商品库存" prop="stock" min-width="120" />
        <el-table-column :label="extend.name + '价格'" min-width="120">
            <template #default="{ row }">
                <el-input
                    type="number"
                    placeholder="请输入"
                    :disabled="disabled"
                    v-model="row.current_price"
                />
            </template>
        </el-table-column>
        <el-table-column v-if="!extend.isGroupBuy" :label="extend.name + '库存'" min-width="120">
            <template #default="{ row }">
                <el-input
                    type="number"
                    placeholder="请输入"
                    :disabled="disabled"
                    v-model="row.current_stock"
                />
            </template>
        </el-table-column>
    </el-table>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core'
import { withDefaults } from 'vue'

const props = withDefaults(
    defineProps<{
        modelValue?: any
        disabled: boolean
        extend: any
    }>(),
    {
        modelValue: [],
        // 是否禁用
        disabled: false,
        // 拓展
        extend: {}
    }
)

const emit = defineEmits(['update:modelValue'])
const { modelValue: selectData } = useVModels(props, emit)
</script>
