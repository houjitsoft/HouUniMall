<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <div class="font-medium">佣金概览</div>
            <div class="mt-[30px] grid grid-cols-2 lg:grid-cols-4 gap-4">
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">累计已入账佣金</div>
                    <div class="text-6xl">{{ data.all_money }}</div>
                </div>
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">今日入账佣金</div>
                    <div class="text-6xl">{{ data.today_money }}</div>
                </div>
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">待结算佣金</div>
                    <div class="text-6xl">{{ data.wait_money }}</div>
                </div>
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">今日新增待结算佣金</div>
                    <div class="text-6xl">{{ data.today_wait_money }}</div>
                </div>
            </div>
        </el-card>
        <!-- <el-card class="!border-none mt-4" shadow="never">
            <div class="font-medium">分销概览</div>
            <div class="mt-[30px] grid grid-cols-2 lg:grid-cols-4 gap-4">
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">分销商</div>
                    <div class="text-6xl">{{ data.user_nums }}</div>
                </div>
                <div class="flex flex-col justify-center items-center">
                    <div class="text-sm">分销商占比</div>
                    <div class="text-6xl">{{ data.user_distribution_nums }}</div>
                </div>
            </div>
        </el-card> -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <el-card class="!border-none mt-4" shadow="never">
                <div class="font-medium">分销商收入排行榜</div>
                <div class="mt-4">
                    <el-table :data="data.income_rank">
                        <el-table-column label="排行">
                            <template #default="{ $index }">
                                <div>{{ $index + 1 }}</div>
                            </template>
                        </el-table-column>
                        <el-table-column label="用户信息" prop="nickname">
                            <template #default="{ row }">
                                <div class="flex items-center">
                                    <el-image
                                        class="w-[45px] h-[45px]"
                                        :src="row.user_distribution.avatar"
                                    ></el-image>
                                    <div class="ml-2">{{ row.user_distribution.nickname }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="已入账佣金" prop="all_money">
                            <template #default="{ row }">
                                <div>{{ row.user_distribution.distribution_total_money }}</div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <div class="font-medium">分销商下级人数排行</div>
                <div class="mt-4">
                    <el-table :data="data.next_rank">
                        <el-table-column label="排行">
                            <template #default="{ $index }">
                                <div>{{ $index + 1 }}</div>
                            </template>
                        </el-table-column>
                        <el-table-column label="用户信息" prop="nickname">
                            <template #default="{ row }">
                                <div class="flex items-center">
                                    <el-image
                                        class="w-[45px] h-[45px]"
                                        :src="row.user_distribution.avatar"
                                    ></el-image>
                                    <div class="ml-2">{{ row.user_distribution.nickname }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="下级人数" prop="next_nums">
                            <template #default="{ row }">
                                <div>{{ row.user_distribution.next_nums }}</div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { getDistributionOverView } from '@/api/application/distribution'

//页面数据
const data: any = ref({})

//获取数据
const getData = async () => {
    data.value = await getDistributionOverView()
}

onMounted(() => {
    getData()
})
</script>
