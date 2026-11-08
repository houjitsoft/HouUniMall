<template>
    <div class="area-select">
        <el-cascader
            :style="{ width }"
            v-model="areaValue"
            :options="options"
            :props="props2"
        ></el-cascader>
    </div>
</template>

<script lang="ts" setup>
import { getAreaLists } from "@/api/app"
import { ref, computed } from 'vue'

/** Emit Start **/
const emit = defineEmits(['update:province', 'update:city', 'update:district'])
/** Emit End **/

/** Props Start **/
const props = withDefaults(
    defineProps<{
        province?: any
        city: any
        district: any
        width: string
        checkStrictly: boolean
    }>(),
    {
        province: null,
        city: null,
        district: null,
        width: '320px',
        checkStrictly: true
    }
)

const props2 = {
    checkStrictly: true,
    value: 'id', 
    label: 'name', 
    children: 'sub' 
}
/** Props End **/

/** Data End **/
const options = ref<any>() // 地区列表
/** Data Start **/

/** Computed Start **/
// 更新绑定数据
const areaValue = computed({
    get: () => {
        return [props.province, props.city, props.district]
    },
    set: (value: any) => {
        emit('update:province', value[0])
        emit('update:city', value[1])
        emit('update:district', value[2])
    }
})
/** Computed End **/

/** Methods Start **/
const getAreaList = async () => {
    try {
       options.value = await getAreaLists();
    } catch (error) {
        console.error(error);
    }
}
/** Methods End **/


/** Cycle Life Start **/
getAreaList();
/** Cycle Life End **/
</script>
