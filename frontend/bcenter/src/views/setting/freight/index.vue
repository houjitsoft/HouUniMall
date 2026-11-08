<template>
    <div class="freight-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert type="warning" title="温馨提示：设置快递配送的运费模板；需要开启快递发货的配送方式，运费模板才能生效" :closable="false" show-icon />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-button type="primary" v-perms="['mall.expressTpl/add', 'mall.expressTpl/add:edit']">
                <router-link :to="{
                    path: getRoutePath('mall.expressTpl/add:edit'),
                }">
                    + 新增
                </router-link>
            </el-button>

            <el-table class="mt-4" size="large" v-loading="pager.loading" :data="pager.lists" row-key="id">
                <el-table-column property="name" label="模版名称" min-width="120" />
                <el-table-column property="type_text" label="计费方式" min-width="120" />
                <el-table-column property="remark" label="备注" min-width="120" />
                <el-table-column property="create_time" label="创建时间" min-width="120" />
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['mall.expressTpl/add', 'mall.expressTpl/edit']" type="primary" link>
                            <router-link :to="{
                                path: getRoutePath('mall.expressTpl/add:edit'),
                                query: {
                                    id: row.id
                                }
                            }">
                                编辑
                            </router-link>
                        </el-button>
                        <el-button v-perms="['article.article/delete']" type="danger" link
                            @click="handleDelete(row.id)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup>
import { getFreightLists, apiFreightDel } from "@/api/setting/freight";
import { usePaging } from "@/hooks/usePaging";
import { getRoutePath } from "@/router";
import feedback from "@/utils/feedback";

const queryParams = reactive({
    title: "",
    cid: "",
    is_show: "",
});

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getFreightLists,
    params: queryParams,
});

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm("确定要删除？");
        await apiFreightDel({ id });
        getLists();
    } catch (error) {
        console.log("删除运费模版失败=>", error);
    }
};

getLists();
</script>
