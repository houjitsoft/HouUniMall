<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" class="mb-[-16px] mt-4" :model="queryParams" :inline="true">
                <el-form-item label="变动类型">
                    <el-select class="w-[280px]" v-model="queryParams.change_type">
                        <el-option label="全部" value />
                        <el-option label="采集商品" value="2020" />
                        <el-option label="平台扣减" value="2010" />
                        <el-option label="平台增加" value="2000" />
                    </el-select>
                </el-form-item>
                <el-form-item label="记录时间">
                    <daterange-picker
                        v-model:startTime="queryParams.start_time"
                        v-model:endTime="queryParams.end_time"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="变动金币" min-width="180">
                    <template #default="{ row }">
                        <div v-if="row.account_status == 1" style="color: red">
                            +{{ row.change_amount }}
                        </div>
                        <div v-if="row.account_status == 2" style="color: green">
                            -{{ row.change_amount }}
                        </div>
                        <div v-if="row.account_status == 3">{{ row.change_amount }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="剩余金币" prop="sn" min-width="180">
                    <template #default="{ row }">
                        <div>{{ row.left_amount }}</div>
                    </template>
                </el-table-column>

                <el-table-column label="类型" prop="change_type_text" min-width="120">
                </el-table-column>

                <el-table-column label="记录时间" prop="create_time" min-width="120" />
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script setup lang="ts">
import { usePaging } from '@/hooks/usePaging'
import { apimoneyLists } from '@/api/application/gather'
const queryParams: any = ref({
    is_gold: 1,
    change_type: '',
    start_time: '', //开始时间
    end_time: '' //结束时间
})

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: apimoneyLists,
    params: queryParams.value
})

getLists()
</script>
