<template>
    <div>
        <el-form label-width="70px">
            <!-- <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item> -->
            <el-form-item label="推荐商品">
                <div class="flex-1">
                    <goods-select v-model="content.data"></goods-select>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from "vue";
import type options from "./options";
import GoodsSelect from "../../goods-select.vue";
import Draggable from "vuedraggable";
type OptionsType = ReturnType<typeof options>;
defineProps({
    content: {
        type: Object as PropType<OptionsType["content"]>,
        default: () => ({}),
    },
    styles: {
        type: Object as PropType<OptionsType["styles"]>,
        default: () => ({}),
    },
});
</script>

<style lang="scss" scoped></style>
