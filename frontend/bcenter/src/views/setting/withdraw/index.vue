<template>
    <div class="article-lists">
        <el-card class="!border-none" shadow="never">
            <el-alert type="warning" title="温馨提示：商家需要设置好提现账号才能申请提现。" :closable="false"
                show-icon />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <div class="flex mb-4">
                <el-button type="primary" @click="handleAdd">
                    <template #icon>
                        <icon name="el-icon-Plus" />
                    </template>
                    添加银行卡
                </el-button>
            </div>
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="ID" prop="id" width="55" />
                <el-table-column label="提现银行" min-width="100">
                    <template #default="{ row }">
                        {{ row.name }}
                        <el-tag v-if="row.is_default">默认</el-tag>
                    </template>
                </el-table-column>
                <el-table-column property="branch" label="银行支行" max-width="266" />
                <el-table-column property="username" label="开户人姓名" max-width="266" />
                <el-table-column label="银行卡账号" prop="card_number" min-width="100" />
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button v-perms="['mall.shopBank/detail']" type="primary" link @click="handleDetail(row)">
                            查看
                        </el-button>
                        <el-button v-perms="['mall.shopBank/edit']" type="primary" link @click="handleEdit(row)">
                            编辑
                        </el-button>
                        <el-button v-perms="['mall.shopBank/delete']" type="danger" link
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
        <detail-popup v-if="showDetail" ref="detailRef" @close="showDetail = false" />
    </div>
</template>
<script lang="ts" setup>
import { shopBankDelete, getShopBankListss } from "@/api/setting/withdraw";
import { usePaging } from "@/hooks/usePaging";
import feedback from "@/utils/feedback";
import EditPopup from "./edit.vue";
import DetailPopup from "./detail.vue";
import NullImage from "../../../assets/images/null_image.png"

const editRef = shallowRef<InstanceType<typeof EditPopup>>();
const detailRef = shallowRef<InstanceType<typeof DetailPopup>>();
const showEdit = ref(false);
const showDetail = ref(false);

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: getShopBankListss
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

const handleDetail = async (data: any) => {
    showDetail.value = true;
    await nextTick();
    detailRef.value?.open();
    detailRef.value?.getDetail(data);
}

const handleEdit = async (data: any) => {
    showEdit.value = true;
    await nextTick();
    editRef.value?.open("edit");
    editRef.value?.getDetail(data);
};

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm("确定要删除？");
        await shopBankDelete({ id });
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
