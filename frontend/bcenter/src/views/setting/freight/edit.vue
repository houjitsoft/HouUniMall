<template>
    <div class="freight-edit">
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="$route.meta.title" @back="$router.back()" />
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-form ref="form" :model="formData" :rules="rules" label-width="120px">

                <el-form-item label="模板名称" prop="name">
                    <div class="w-80">
                        <el-input v-model="formData.name" placeholder="请输入模板名称"></el-input>
                    </div>
                </el-form-item>

                <el-form-item label="计费方式" prop="type">
                    <div>
                        <el-radio-group v-model="formData.type" :disabled="formData.id != ''">
                            <el-radio :label="0">件数计费</el-radio>
                            <el-radio :label="1">重量计费</el-radio>
                            <el-radio :label="2">体积计费</el-radio>
                        </el-radio-group>
                        <div class="form-tips">保存后计费方式不能更改</div>
                    </div>
                </el-form-item>

                <el-form-item label="配送区费用">

                    <!-- 配送区域表单 -->
                    <el-table :data="formData.content" style="width: 100%" size="mini">
                        <el-table-column label="配送区域" prop="area_names" min-width="300" />

                        <!-- 首件 -->
                        <el-table-column min-width="100" :label="`首${getTableTitle}`">
                            <template #default="{ row }">
                                <el-input class="pt-4 pb-4" style="width: 90%" type="number"
                                    v-model="row.first_num">
                                </el-input>
                            </template>
                        </el-table-column>
                        
                        <!-- 运费 -->
                        <el-table-column min-width="100" label="运费（元）">
                            <template #default="{ row }">
                                <el-input class="pt-4 pb-4" style="width: 90%" type="number"
                                    v-model="row.first_price">
                                </el-input>
                            </template>
                        </el-table-column>

                        <!-- 续件 -->
                        <el-table-column min-width="100" :label="`续${getTableTitle}`">
                            <template #default="{ row }">
                                <el-input class="pt-4 pb-4" style="width: 90%" type="number"
                                    v-model="row.continue_num">
                                </el-input>
                            </template>
                        </el-table-column>

                        <!-- 续费 -->
                        <el-table-column min-width="100" label="续费（元）">
                            <template #default="{ row }">
                                <el-input class="pt-4 pb-4" style="width: 90%" type="number"
                                    v-model="row.continue_price"></el-input>
                            </template>
                        </el-table-column>

                        <!-- 操作 -->
                        <el-table-column fixed="right" label="操作" width="150">
                            <template #default="{ row, $index }">
                                <el-button 
                                    v-if="$index >= 1"
                                    type="primary" link 
                                    @click="choiseArea(row.area_ids, $index)"
                                >
                                    编辑
                                </el-button>
                                <el-button 
                                    v-if="$index >= 1"
                                    type="primary" link 
                                    @click="areaDelete($index)"
                                >
                                    删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>

                    <div class="flex mt-4">
                        <el-button type="primary" @click="choiseArea">制定配送区域和运费</el-button>
                        <div class="ml-4 form-tips">
                            根据件数来计算运费，当物品不足《首件数量》时，按照《首件费用》计算，超过部分按照《续件件数》和《续件费用》乘积来计算
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input style="width: 600px" v-model="formData.remark" rows="8" type="textarea"
                        placeholder="请输入备注内容"></el-input>
                </el-form-item>
            </el-form>
        </el-card>

        <footer-btns>
            <el-button type="primary" @click="handleSave">保存</el-button>
        </footer-btns>

        <!-- 配送区域 -->
        <DeliveryArea 
            v-if="showArea" 
            ref="deliveryAreaRef" 
            @success="areaChange" 
            @close="showArea=false"
        ></DeliveryArea>
    </div>
</template>

<script lang="ts" setup name="freightEdit">
import type { FormInstance, FormRules } from "element-plus";
import { useDictOptions } from "@/hooks/useDictOptions";
import type { FreightModal } from "@/api/setting/freight.d"
import { getFreightDetail, apiFreightEdit, apiFreightAdd } from "@/api/setting/freight";
import useMultipleTabs from "@/hooks/useMultipleTabs";

import { filterText, selected } from "./components/useAreaEffect"
import DeliveryArea from "./components/delivery-area.vue"

const route = useRoute();
const router = useRouter();
const showArea = ref(false);
const deliveryAreaRef = shallowRef();
const formData = reactive<FreightModal>({
    id: "",
    name: "",
    type: 0,
    remark: "",
    content: [{
        area_ids: ['all'],
        area_names: '全国统一运费',// 地区名称
        first_num: 0,       // 是	示例值：2说明：首 件/重/体积
        first_price: 0,     // 是	示例值：3说明：首 件/重/体积 运费
        continue_num: 0,    // 是	示例值：4说明：续 件/重/体积
        continue_price: 0   // 是	示例值：5说明：续 件/重/体积 运费
    }]
});

const { removeTab } = useMultipleTabs();
const formRef = shallowRef<FormInstance>();
const rules = reactive<FormRules>({
    name: [{ required: true, message: "模版名称", trigger: "blur" }],
    type: [
        { required: true, message: "请选择计费方式", trigger: "blur" },
    ]
});

const getTableTitle = computed(() => {
    const { type } = formData;
    switch (type) {
        case 0:
            return "件（件）";
        case 1:
            return "重（Kg）";
        case 2:
            return "体积（m³）";
    }
});

const choiseArea = async (area_ids: number[] = [], index: number = -1) => {
    showArea.value = true
    await nextTick();
    deliveryAreaRef.value.open(area_ids, index);
}

const areaChange = (options: { index:number, ids: number[], names: string[] }) => {
    const { ids, names, index } = options;
    if( index != -1 ) {
        formData.content[index].area_ids = ids;
        formData.content[index].area_names = names;
        return
    }
    formData.content.push({
        area_ids: ids,
        area_names: names,
        first_num: 0,
        first_price: 0,
        continue_num: 0,
        continue_price: 0
    })
}

const areaDelete = (index: number) => {
    formData.content.splice(index, 1);
}

const getDetails = async () => {
    const data: FreightModal = await getFreightDetail({
        id: route.query.id,
    });
    Object.keys(formData).forEach((key) => {
        formData[key] = data[key];
    });
};

const handleSave = async () => {
    await formRef.value?.validate();
    if (route.query.id) {
        await apiFreightEdit(formData);
    } else {
        await apiFreightAdd(formData);
    }
    removeTab();
    router.back();
};

route.query.id && getDetails();

onBeforeUnmount(() => {
    console.log('???/销毁');
    filterText.value = "";
    selected.map_ids.clear();
})
</script>
