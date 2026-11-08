<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：1、采集到的商品默认是没有上架的，请手动上架商品。"
                :closable="false"
                show-icon
            />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="商品类型" min-width="180">
                    <template #default="{ row }"> 实物商品 </template>
                </el-table-column>
                <el-table-column label="平台分类" prop="sn" min-width="180">
                    <template #default="{ row }">
                        <div>{{ row.category.name }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="采集数" prop="sn" min-width="180">
                    <template #default="{ row }">
                        <div>{{ row.nums }}</div>
                    </template>
                </el-table-column>

                <el-table-column label="成功数" prop="success" min-width="120"> </el-table-column>

                <el-table-column label="消耗金币" prop="gather_money" min-width="120" />
                <el-table-column label="采集时间" prop="create_time" min-width="120" />
                <el-table-column label="操作" prop="create_time" min-width="120">
                    <template #default="{ row }">
                        <router-link
                            :to="{
                                path: getRoutePath('gather.gather/detial'),
                                query: { id: row.id }
                            }"
                        >
                            <el-button link type="primary"> 查看</el-button>
                        </router-link>
                        <el-button link type="primary" @click="handleDel(row.id)"> 删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script setup lang="ts">
import { usePaging } from '@/hooks/usePaging'
import { apiGatherlists, apiGatherDel } from '@/api/application/gather'
import { getRoutePath } from '@/router'

const queryParams: any = ref({
    start_time: '', //开始时间
    end_time: '' //结束时间
})

//分页组件
const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: apiGatherlists,
    params: queryParams.value
})
const handleDel = async (id: number) => {
    await apiGatherDel({ id })
    getLists()
}

getLists()
</script>
