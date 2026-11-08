<template>
    <popup
        ref="popupRef"
        :title="`选择${extend.name}商品`"
        :async="false"
        width="880px"
        @confirm="emit('update:modelValue', selectData)"
        @open="open"
    >
        <template #trigger>
            <el-button type="primary" :disabled="disabled">选择商品</el-button>
        </template>

        <!-- 表单-->
        <el-form
            :model="queryParams"
            label-width="80px"
            :rules="formRules"
            :inline="true"
            :disabled="disabled"
        >
            <el-form-item label="商品分类" prop="shop_category_id">
                <el-select class="w-[220px]" v-model="queryParams.shop_category_id">
                    <el-option
                        v-for="itme in otherList"
                        :key="itme.id"
                        :label="itme.name"
                        :value="itme.id"
                    ></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="商品搜索">
                <el-input
                    class="w-[220px]"
                    v-model="queryParams.name"
                    placeholder="请输入名称"
                    c
                    learable
                    @keyup.enter="resetPage"
                >
                    <template #append>
                        <el-button @click="resetPage" :icon="Search" />
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="resetPage">查询</el-button>
                <el-button @click="resetParams">重置</el-button>
            </el-form-item>
        </el-form>

        <!-- 表格 -->
        <el-table
            ref="tableRef"
            size="large"
            v-loading="pager.loading"
            :data="pager.lists"
            height="420px"
        >
            <el-table-column label="选择" width="60">
                <template #header>
                    <span v-if="type == 'single'">选择</span>
                    <el-checkbox
                        size="large"
                        v-model="selectAll"
                        v-if="type == 'multiple'"
                        :disabled="disabled"
                    ></el-checkbox>
                </template>
                <template #default="{ row }">
                    <div class="flex row-center" @click.stop>
                        <el-checkbox
                            :model-value="selectItem(row)"
                            @change="handleSelect($event, row)"
                            size="large"
                            :disabled="disabled"
                        ></el-checkbox>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="商品信息" min-width="180">
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
            <el-table-column label="分类" prop="category_ids_text" min-width="90" />
        </el-table>

        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </popup>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core'
import { Search } from '@element-plus/icons-vue'
import { usePaging } from '@/hooks/usePaging'
import type { GoodsList } from '@/api/goods/goods'
import { getGoodsCommonLists } from '@/api/goods/goods'
import { getGoodsCategoryOtherLists } from '@/api/goods/category'
import { watch, withDefaults } from 'vue'

const emit = defineEmits(['update:modelValue'])
const props = withDefaults(
    defineProps<{
        modelValue?: any
        type?: string
        disabled: boolean
        extend: any
    }>(),
    {
        modelValue: [],
        // 类型: 多选(multiple) ｜ 单选(single)
        type: 'single',
        // 禁用
        disabled: false,
        // 拓展
        extend: {}
    }
)

const selectData = ref<any>([])
const otherList = shallowRef([])
const queryParams = reactive<GoodsList>({
    name: '',
    category_id: '',
    shop_category_id: '',
    type: 'sale'
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getGoodsCommonLists,
    params: queryParams
})

watch(
    () => props.modelValue,
    (val) => {
        selectData.value = val
    },
    { deep: true }
)

const selectAll = computed({
    get: () => {
        const { lists } = pager
        if (!selectData.value) return false
        const ids: any[] = selectData.value.map((item: any) => item.id)
        if (!lists.length) {
            return false
        }
        return lists.every((item) => ids.includes(item.id))
    },
    set: (val) => {
        const { lists } = pager
        if (val) {
            for (let i = 0; i < lists.length; i++) {
                const item = lists[i]
                const ids: any[] = selectData.value.map((item: any) => item.id)
                if (!ids.includes(item.id)) {
                    selectData.value.push(item)
                }
            }
        } else {
            lists.forEach((row) => {
                deleteSelectedData(row)
            })
        }
    }
})

const selectItem = computed(() => {
    return (row: any) => {
        if (props.type == 'single') {
            return selectData.value.id == row.id
        }
        if (!selectData.value) return false

        return selectData.value.some((item: any) => item.id == row.id)
    }
})

const handleSelect = ($event: boolean, row: any) => {
    if (props.type == 'single') {
        if ($event) {
            selectData.value = row
        } else {
            selectData.value = {}
        }
    } else if ($event) {
        selectData.value.push(row)
    } else {
        deleteSelectedData(row)
    }
}

const deleteSelectedData = (row: any) => {
    const index = selectData.value.findIndex((item: any) => item.id == row.id)
    if (index != -1) {
        selectData?.value.splice(index, 1)
    }
}

const getOtherList = async () => {
    try {
        const data = await getGoodsCategoryOtherLists()
        otherList.value = data
    } catch (error) {
        console.log(error)
    }
}

const open = async () => {
    //TODO
    selectData.value = props.modelValue
    await getLists()
    await getOtherList()
}
</script>
