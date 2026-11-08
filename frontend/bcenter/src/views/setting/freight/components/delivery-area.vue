<template>
    <div class="edit-popup">
        <popup ref="popupRef" title="区域选择" :async="true" width="880px" @confirm="onConfirm"
            @close="handleClose">
            <div class="flex">
                <!-- Left Main -->
                <div class="flex-1 inline-block mr-10">

                    <!-- Left Header -->
                    <div class="h-[40px] flex items-center justify-between" >
                        <div class="left-title">地区选择</div>
                        <el-input
                            class="w-[200px]"
                            v-model="filterText" 
                            placeholder="请输入地区名称搜索" 
                        />
                    </div>
                
                    <!-- Left Unselected Area -->
                    <div class="left-main">
                        <AreaPanel 
                            :data="area" 
                            :isSelect="true"
                            :filterText="filterText"
                        ></AreaPanel>
                    </div>
                </div>

                <!-- Right Main -->
                <div class="flex-1 inline-block">

                    <!-- Right Header -->
                    <div class="flex items-center h-[40px]">已选择</div>

                    <!-- Right Selected Area -->
                    <div class="right-main">
                        <AreaPanel :data="area" :isSelect="false"></AreaPanel>
                    </div>
                </div>
            </div>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import { getAreaLists } from "@/api/app"
import type { Tree } from "./type"

import AreaPanel from "./area-panel.vue"
import Popup from "@/components/popup/index.vue";
import { number } from "echarts/core";

import { filterText, selected, transformAreaIdsToJson, transformAreaIdsToParse } from "./useAreaEffect"

const emit = defineEmits(["success", "close"]);
const popupRef = shallowRef<InstanceType<typeof Popup>>();

const area = ref<Tree[]>([]);
const editIndex = ref(-1);

// 获取地区列表
const getAreaListData = async () => {
    try {
       area.value = await getAreaLists();
    } catch (error) {
        console.error(error);
    }
}

const open = async (area_ids: number[], index: number) => {
    await getAreaListData();
    const areaLen = area_ids.length;
    if(areaLen) {
        editIndex.value = index;
        area_ids = transformAreaIdsToParse(area.value, area_ids);
        area_ids.map(item => selected.map_ids.set(item, item));
    }
    popupRef.value?.open();
};

const onConfirm = () => {
    // console.log('结果------------', transformAreaIdsToJson(area.value).ids);
    emit('success', {
        index: editIndex.value, 
        ...transformAreaIdsToJson(area.value)
    });
    popupRef.value?.close();
}

const handleClose = () => { emit("close"); };


onBeforeUnmount(() => {
    console.log('???/销毁');
    filterText.value = "";
    selected.map_ids.clear();
})

defineExpose({ open });
</script>
