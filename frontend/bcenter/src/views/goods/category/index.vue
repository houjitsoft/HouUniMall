<template>
    <div class="article-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert type="warning" title="温馨提示：商家发布商品时可以选择对应的商家商品分类，方便用户在商家内根据商家商品分类搜索商品" :closable="false"
                show-icon />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <div class="flex mb-4">
                <el-button type="primary" @click="handleAdd">
                    新增分类
                </el-button>
            </div>
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column width="55" />
                <el-table-column label="分类图标" min-width="100">
                    <template #default="{ row }">
                        <image-contain v-if="row.image" :src="row.image" :width="60" :height="45"
                            :preview-src-list="[row.image]" preview-teleported fit="contain" />
                        <image-contain v-else :src="NullImage" :width="60" :height="45"
                             preview-teleported fit="contain" />
                    </template>
                </el-table-column>
                <el-table-column property="name" label="分类名称" max-width="266" />
                <el-table-column property="goods_num" label="商品数量" max-width="266" />
                <el-table-column label="排序" prop="sort" min-width="100" />
                <el-table-column property="status" label="状态" max-width="220">
                    <template #default="{ row }">
                        <el-switch v-perms="['mall.shopCategory/status']" v-model="row.is_show" :active-value="1"
                            :inactive-value="0" @change="changeStatus($event, row.id)" />
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="create_time" min-width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['article.articleCate/edit']" type="primary" link @click="handleEdit(row)">
                            编辑
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

        <edit-popup v-if="showEdit" ref="editRef" @success="requestPagination" @close="showEdit = false" />
    </div>
</template>
<script lang="ts" setup>
import { apiGoodsCategoryDel, apiGoodsCategoryStatus, getGoodsCategoryLists } from "@/api/goods/category";
import { usePaging } from "@/hooks/usePaging";
import feedback from "@/utils/feedback";
import EditPopup from "./edit.vue";
import NullImage from "../../../assets/images/null_image.png"

const editRef = shallowRef<InstanceType<typeof EditPopup>>();
const showEdit = ref(false);

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getGoodsCategoryLists
});

const expand = ref<string[]>([]);

const requestPagination = () => {
    getLists();
    expand.value = [];
};

const handleAdd = async () => {
    showEdit.value = true;
    await nextTick();
    editRef.value?.open("add");
};

const changeStatus = async (status: number, id: number) => {
    try {
        await apiGoodsCategoryStatus({ id, is_show: status });
        requestPagination();
    } catch (error) {
        console.log(error)
    }
};

const handleEdit = async (data: any) => {
    showEdit.value = true;
    await nextTick();
    editRef.value?.open("edit");
    editRef.value?.getDetail(data);
};

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm("确定要删除？");
        await apiGoodsCategoryDel({ id });
        requestPagination();
    } catch (error) {
        console.log(error)
    }
};

onActivated(() => {
    requestPagination();
});

requestPagination();
</script>
