<template>
    <div class="ml-[80px] pr-8">
        <div class="flex m-4">
            <!-- :disabled="specItem.length >= 3" -->
            <el-button type="primary" @click="addSpecItem">添加规格项</el-button>
            <!-- <div class="ml-4 form-tips">最多支持3个规格项</div> -->
        </div>

        <template v-for="(item, index) in specItem" :key="index">
            <del-wrap @close="handleDelSpecItem(index)">
                <div class="flex p-[16px] ml-4 mt-[16px] spec-item">
                    <div class="flex-none mr-[10px]">
                        <div class="mt-2">规格名</div>
                        <div class="mt-6">规格值</div>
                    </div>
                    <div class="spec-item__content">
                        <div>
                            <el-input v-model="item.name" style="width: 240px" maxlength="20" show-word-limit>
                            </el-input>
                            <el-checkbox class="ml-4" :false-label="0" :true-label="1" v-model="item.has_image"
                                @change="addImage(index, $event)">规格图片
                            </el-checkbox>
                        </div>
                        <div class="flex flex-wrap col-top">
                            <div class="mt-4 mr-4" v-for="(subItem, subIndex) in item.value" :key="subIndex">
                                <del-wrap @close="removeSpecValue(index, subIndex)">
                                    <el-input class="w-40" v-model="subItem.value" maxlength="20" show-word-limit
                                        @blur="checkValue(index, subIndex)"></el-input>
                                </del-wrap>
                                <div v-if="item.has_image">
                                    <material-picker class="mt-4" :limit="1" size="60px" v-model="subItem.image">
                                    </material-picker>
                                </div>
                            </div>
                            <div class="mt-4">
                                <el-button @click="addSpecValue(index)">+ 添加规格值</el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </del-wrap>
        </template>
    </div>

    <el-form-item label="规格明细" class="mt-8">
        <div class="flex pl-[10px] mb-4">
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'price')">
                <el-button :disabled="disabledBatchBtn">设置价格</el-button>
            </popover-input>
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'line_price')">
                <el-button :disabled="disabledBatchBtn">设置划线价</el-button>
            </popover-input>
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'market_price')">
                <el-button :disabled="disabledBatchBtn">设置成本价</el-button>
            </popover-input>
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'stock')">
                <el-button :disabled="disabledBatchBtn">设置库存</el-button>
            </popover-input>
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'volume')">
                <el-button :disabled="disabledBatchBtn">设置体积</el-button>
            </popover-input>
            <popover-input class="mr-2" :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'weight')">
                <el-button :disabled="disabledBatchBtn">设置重量</el-button>
            </popover-input>
            <popover-input :disabled="disabledBatchBtn" @confirm="batchSetting($event, 'code')">
                <el-button :disabled="disabledBatchBtn">设置条码</el-button>
            </popover-input>
        </div>

        <el-table class="pl-[10px]" :data="specParams.tableData" max-height="600" :row-height="75" tooltip-effect="dark"
            :border="false" big-data-checkbox @selection-change="selectDataChange">
            <el-table-column type="selection" width="55" />
            <el-table-column v-for="(item, index) in specItem" :key="index" :label="item.name" min-width="140"
                :show-overflow-tooltip="true">
                <template #default="{ row }">
                    {{ row.sku_value_arr[index] }}
                </template>
            </el-table-column>
            <el-table-column label="规格图片" min-width="90">
                <template #default="{ row, $index }">
                    <del-wrap @close="removeSpecImage($index)" v-if="row.image">
                        <el-image style="width: 50px; height: 50px" :src="row.image" @click="addSpecImage($index)">
                        </el-image>
                    </del-wrap>
                    <div class="flex items-center justify-center spec-image" @click="addSpecImage($index)" v-else>
                        <el-icon>
                            <Plus />
                        </el-icon>
                    </div>
                </template>
            </el-table-column>
            <el-table-column min-width="100">
                <template #header>
                    <span class="require-text">*</span> 价格
                </template>
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.price"></el-input>
                </template>
            </el-table-column>
            <el-table-column min-width="100">
                <template #header> 划线价 </template>
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.line_price"></el-input>
                </template>
            </el-table-column>
            <el-table-column label="成本价" min-width="100">
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.market_price"></el-input>
                </template>
            </el-table-column>
            <el-table-column min-width="100">
                <template #header>
                    <span class="require-text">*</span> 库存
                </template>
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.stock"></el-input>
                </template>
            </el-table-column>
            <el-table-column min-width="100">
                <template #header> 体积 </template>
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.volume"></el-input>
                </template>
            </el-table-column>
            <el-table-column label="重量" min-width="100">
                <template #header> 重量 </template>
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.weight"></el-input>
                </template>
            </el-table-column>
            <el-table-column label="条码" min-width="100">
                <template #default="{ row }">
                    <el-input class="spec-input" type="number" v-model="row.code"></el-input>
                </template>
            </el-table-column>
        </el-table>
    </el-form-item>
    <material-picker ref="materialRef" :hiddenUpload="true" @change="changeSpecImage" />
</template>

<script lang="ts" setup>
import feedback from "@/utils/feedback";
import { flatten } from "@/utils/util";
import type { SkuItemList, SkuNameList } from "@/api/goods/goods"

const props = withDefaults(
    defineProps<{
        modelValue: any;
    }>(),
    {
        modelValue: {},
    }
);

const materialRef = shallowRef();
const specParams = reactive<any>({
    tableData: [],
    selectData: [],
    tableDataIndex: 0 as number,
});

const disabledBatchBtn = computed(() => !specParams.selectData.length);

const specItem = computed(() => props.modelValue.sku_name_list || []);
const specSubItem = computed(() => props.modelValue.sku || []);

// 新增规格项
const addSpecItem = () => {
    const len = props.modelValue.sku_name_list.length;
    // if (len >= 3) return feedback.msgError("最多添加3个规格项");
    props.modelValue.sku_name_list.push({
        has_image: 0,
        id: "",
        name: "",
        value: [
            {
                value: "",
                image: "",
            },
        ],
    });
};

// 删除规格项
const handleDelSpecItem = (index: number) => {
    const len = props.modelValue.sku_name_list.length;
    if (len <= 1) return feedback.msgError("至少一个规格项");
    props.modelValue.sku_name_list.splice(index, 1);
};

// 新增规格值
const addSpecValue = (index: number) => {
    props.modelValue.sku_name_list[index].value.push({
        // id: "",
        value: "",
        image: "",
    });
};

// 删除规格值
const removeSpecValue = (index: number, subIndex: number) => {
    props.modelValue.sku_name_list[index].value.splice(subIndex, 1);
};

const addImage = (i: number, v: any) => {
    const skuItem: SkuNameList[] = props.modelValue.sku_name_list;
    const skuSubItem: SkuItemList[] = props.modelValue.sku;

    console.log(skuItem)
    console.log(skuSubItem)

    skuItem.forEach((item, index: number) => {
        item.has_image = 0;
        if (i == index) {
            item.has_image = v;
        }
    });
    skuSubItem.forEach(sitem => {
        sitem.image = "";
    });
    specParams.tableData.forEach((item: { image: string; }) => {
        item.image = "";
    });
};

// 娇艳规格值
const checkValue = (index: number, subIndex: number) => {
    const skuItem = props.modelValue?.sku_name_list[index];
    const value = skuItem?.value[subIndex].value;
    const res = skuItem?.value.filter(
        (item: { value: string }) =>
            item.value == value && value != "" && value.length != 0
    );
    const lessTops = res.length >= 2;
    if (lessTops) {
        feedback.msgWarning("已存在相同规格值");
        skuItem.value[subIndex].value = "";
    }
};

const selectDataChange = (value: SkuItemList[]) => {
    specParams.selectData = value.map(item => item.ids);
};

const batchSetting = (value: string, fields: string | never) => {
    specParams.tableData.forEach((item: { [x: string]: string; ids: any; }) => {
        if (specParams.selectData.includes(item.ids)) {
            item[fields] != undefined && (item[fields] = value);
        }
    });
};

//设置字段名称
const setFields = (prev: any, next: any) => {
    let valueArr = [prev, next]
    valueArr = valueArr.filter(item => item.value !== undefined)
    const ids = flatten(valueArr.map(item => item.ids)).join()
    const value = flatten(valueArr.map(item => item.value))
    return {
        id: prev.id ? prev.id : '',
        ids: ids,
        value,
        sku_value_arr: value,
        image: prev.image ? prev.image : next.image,
        price: prev.price ? prev.price : '',
        line_price: prev.line_price ? prev.line_price : '',
        market_price: prev.market_price ? prev.market_price : '',
        stock: prev.stock ? prev.stock : '',
        volume: prev.volume ? prev.volume : '',
        weight: prev.weight ? prev.weight : '',
        code: prev.code ? prev.code : ''
    }
};

// 通过规格项和规格值得到一个表格data
const getTableData = (arr: any[]) => {
    arr = JSON.parse(JSON.stringify(arr))
    return arr.reduce(
        (prev, next) => {
            const newArr = []
            for (let i = 0; i < prev.length; i++) {
                if (!next.length) {
                    newArr.push(setFields(prev[i], {}))
                }
                for (let j = 0; j < next.length; j++) {
                    next[j].ids = j
                    newArr.push(setFields(prev[i], next[j]))
                }
            }
            return newArr
        },
        [{}]
    )
}

const setTableData = () => {
    const skuNameList = props.modelValue.sku_name_list;
    const tableData = specParams.tableData;
    const specList = skuNameList.map((item: SkuNameList) => item.value)
    const newData = getTableData(specList);
    const rawData = JSON.parse(JSON.stringify(tableData));
    const rawObject: any = {};
    rawData.forEach((item: any) => {
        if (item.sku_value_arr !== undefined) {
            rawObject[item.sku_value_arr] = item;
        }
    })

    specParams.tableData = newData.map((item: any) =>
        rawObject[item.sku_value_arr]
            ? {
                ...rawObject[item.sku_value_arr],
                value: item.value,
                ids: item.ids,
                image: item.image || rawObject[item.sku_value_arr].image,
            }
            : item
    );
};

watch(
    () => specItem.value,
    () => {
        setTableData();
    },
    { deep: true, immediate: true }
);

watch(
    () => props.modelValue.sku,
    (value) => {
        specParams.tableData = value
    }
);

watch(
    () => specParams.tableData,
    (value) => {
        props.modelValue.sku = value
    },
    { deep: true, immediate: true }
);

const addSpecImage = (index: number) => {
    specParams.tableDataIndex = index;
    materialRef.value?.showPopup();
};
const changeSpecImage = (value: string) => {
    specParams.tableData[specParams.tableDataIndex].image = value;
};
const removeSpecImage = (index: number) => {
    specParams.tableData[index].image = "";
};
</script>

<style>
.spec-item {
    transition: all 1s;
    background-color: var(--el-color-primary-light-9);
}

.spec-image {
    width: 50px;
    height: 50px;
    cursor: pointer;
    border: 1px dashed #e5e5e5;
}
</style>
