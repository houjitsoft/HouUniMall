<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            title="全部商品"
            width="850px"
            @confirm="handleSubmit"
            @close="handleClose"
            clickModalClose
            cancelButtonText
            confirmButtonText
        >
            <el-table size="large" :data="goodsList">
                <el-table-column prop="goods" label="商品信息" min-width="220">
                    <template #default="{ row }">
                        <div class="flex mb-[5px]">
                            <image-contain class="flex-none" :src="row.goods_image" :width="70" :height="70"
                                :preview-src-list="[row.goods_image]" preview-teleported fit="cover" />
                            <div class="ml-[6px] text-xs overflow-hidden">
                                <div class="mt-2 goods-name"> {{ row.goods_name }} </div>
                                <div class="text-gray-300">{{ row.goods_sku_value.join(',') }}</div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="商品金额" min-width="100">
                    <template #default="{ row }">
                        <div class="ml-2">¥{{ row.goods_price }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="数量" min-width="100">
                    <template #default="{ row }">
                        <div class="ml-2">
                            <div>x <span>{{ row.goods_num }}</span></div>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import Popup from "@/components/popup/index.vue";
import GoodsTable from "./goods-table.vue"
const emit = defineEmits(["success", "close"]);
const popupRef = shallowRef<InstanceType<typeof Popup>>();

const goodsList = ref<any>([]);

const handleSubmit = () => {
    popupRef.value?.close();
    emit("success");
};

const open = (goods: any[]) => {
    goodsList.value = goods
    popupRef.value?.open();
};

const handleClose = () => {
    emit("close");
};

defineExpose({ open });
</script>
