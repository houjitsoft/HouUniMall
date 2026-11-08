<template>
    <div class="goods-lists">
        <el-form ref="formRef" :model="queryParams" :inline="true">
            <el-form-item label="商品名称">
                <el-input
                    class="w-[280px]"
                    v-model="queryParams.name"
                    placeholder="请输入名称"
                    clearable
                    @keyup.enter="resetPage"
                >
                    <template #append>
                        <el-button @click="resetPage" :icon="Search" />
                    </template>
                </el-input>
            </el-form-item>
        </el-form>

        <!-- 表格 -->
        <el-table
            size="large"
            v-loading="pager.loading"
            :data="pager.lists"
            height="270px"
            @row-click="handleSelectItem"
        >
            <el-table-column label="选择" min-width="60">
                <template #default="{ row }">
                    <div class="flex row-center">
                        <el-checkbox
                            :model-value="getSelectItem(row)"
                            @change="handleSelectItem(row)"
                            size="large"
                        ></el-checkbox>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="商品信息" min-width="300">
                <template #default="{ row }">
                    <div class="flex">
                        <el-image
                            fit="cover"
                            :src="row.image"
                            class="flex-none w-[58px] h-[58px]"
                        />
                        <div class="ml-4 overflow-hidden">
                            <el-tooltip effect="dark" :content="row.name" placement="top">
                                <div class="text-base line-2">
                                    {{ row.name }}
                                </div>
                            </el-tooltip>
                            <el-tag checked v-if="row.sku_type">多规格</el-tag>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="价格" prop="price" min-width="100">
                <template #default="{ row }"> ¥{{ row.price_text }} </template>
            </el-table-column>
            <el-table-column label="库存" prop="total_stock" min-width="90" />
        </el-table>

        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import type { PropType } from 'vue'
import { LinkTypeEnum, type Link } from '.'
import { usePaging } from '@/hooks/usePaging'
import { getGoodsLists } from '@/api/goods/goods'
import type { GoodsList } from '@/api/goods/goods'
import { Search } from '@element-plus/icons-vue'

defineProps({
    modelValue: {
        type: Object as PropType<GoodsList>,
        default: () => ({})
    }
})
const emit = defineEmits<{
    (event: 'update:modelValue', value: any): void
}>()

const selectData = ref<any[]>([])

const queryParams = reactive<GoodsList>({
    name: '',
    category_id: '',
    shop_category_id: '',
    status: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getGoodsLists,
    params: queryParams
})

/**
 * @description 计算当前选择的是哪个子规则
 */
const getSelectItem = computed(() => {
    return (event: any) => {
        return selectData.value.some((item: any) => item.id == event.id)
    }
})
/**
 * @description 选择
 */
const handleSelectItem = (event: any) => {
    const index = selectData.value.findIndex((item: any) => {
        return item.id == event.id
    })
    if (index != -1) {
        selectData.value.splice(index, 1)
        emit('update:modelValue', {
            type: 'goods',
            list: selectData.value
        })
        return
    }
    // selectData.value.push({
    //     id: event.id,
    //     name: event.name,
    //     image: event.image,
    //     price: event.price_text
    // })
    // emit('update:modelValue', {
    //     type: 'goods',
    //     list: selectData.value
    // })
    selectData.value = [
        {
            id: event.id,
            name: event.name,
            image: event.image,
            price: event.price_text
        }
    ]
    emit('update:modelValue', {
        path: '/pages/goods_detail/goods_detail',
        query: {
            id: selectData.value[0].id
        },
        type: 'goods',
        list: selectData.value,
        ...selectData.value[0]
    })
}

getLists()
</script>
