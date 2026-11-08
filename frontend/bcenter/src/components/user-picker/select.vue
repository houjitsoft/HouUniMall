<template>
    <popup
        ref="popupRef"
        :title="`选择用户`"
        :async="false"
        width="880px"
        @confirm="emit('update:modelValue', JSON.parse(JSON.stringify(selectData)))"
        @open="open"
    >
        <template #trigger>
            <slot></slot>
        </template>

        <!-- 表单-->
        <el-form :model="queryParams" label-width="80px" :inline="true" :disabled="disabled">
            <el-form-item label="用户信息">
                <el-input
                    class="w-[280px]"
                    v-model="queryParams.name"
                    placeholder="请输入用户编号/昵称"
                    clearable
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
            <el-table-column label="选择" width="50">
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
            <el-table-column label="用户编号" prop="sn" min-width="100" />
            <el-table-column label="用户昵称" min-width="200">
                <template #default="{ row }">
                    <div class="flex items-center">
                        <el-avatar size="58" :src="row.avatar" />
                        <div class="ml-4 overflow-hidden">
                            <el-tooltip effect="dark" :content="row.nickname" placement="top">
                                <div class="text-base line-2">
                                    {{ row.nickname }}
                                </div>
                            </el-tooltip>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="用户余额" prop="user_money" min-width="120" />
            <el-table-column label="手机号码" prop="mobile" min-width="160">
                <template #default="{ row }">
                    {{ row.mobile || '-' }}
                </template>
            </el-table-column>
            <el-table-column label="注册时间" prop="create_time" min-width="180" />
        </el-table>

        <div class="flex justify-end mt-4">
            <pagination v-model="pager" @change="getLists" />
        </div>
    </popup>
</template>

<script lang="ts" setup>
import { Search } from '@element-plus/icons-vue'
import { usePaging } from '@/hooks/usePaging'
import { getUserList } from '@/api/consumer'
import { watch, withDefaults } from 'vue'

const emit = defineEmits(['update:modelValue'])
const props = withDefaults(
    defineProps<{
        modelValue?: any
        type?: string
        disabled: boolean
        maxNum: number
    }>(),
    {
        modelValue: [],
        // 类型: 多选(multiple) ｜ 单选(single)
        type: 'single',
        // 禁用
        disabled: false,
        // 最大选择数量
        maxNum: 10
    }
)

const selectData = ref<any>([])
const queryParams = reactive<any>({
    keyword: '',
    channel: '',
    start_time: '',
    end_time: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getUserList,
    params: queryParams
})

watch(
    () => props.modelValue,
    (val) => {
        selectData.value = JSON.parse(JSON.stringify(val))
    },
    { deep: true, immediate: true }
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
                if (!ids.includes(item.id) && selectData.value.length < props.maxNum) {
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
    } else if ($event && selectData.value.length < props.maxNum) {
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

const open = async () => {
    await getLists()
}
</script>
