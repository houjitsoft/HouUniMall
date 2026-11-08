<template>
    <div>
        <slot>
            <el-button type="primary" @click="popupRef?.open()"
                >选择商品</el-button
            >
        </slot>

        <draggable class="draggable" v-model="goodsLists.list" animation="300">
            <template v-slot:item="{ element: item, index }">
                <div class="max-w-[120px] inline-block m-2">
                    <del-wrap
                        class="max-w-[120px] inline-block"
                        :key="index"
                        @close="handleDelete(index)"
                    >
                        <image-contain width="120px" height="120px" :src="item.image" />
                    </del-wrap>
                </div>
            </template>
        </draggable>
        <popup
            ref="popupRef"
            width="700px"
            title="链接选择"
            @confirm="handleConfirm"
        >
            <link-content v-model="activeLink" />
        </popup>
    </div>
</template>
<script lang="ts" setup>
import feedback from "@/utils/feedback";
import type { PropType } from "vue";
import Draggable from "vuedraggable";
import LinkContent from "@/components/link/index.vue";
import Popup from "@/components/popup/index.vue";
const props = defineProps({
    modelValue: {
        type: Array as PropType<any[]>,
        default: () => [],
    },
    min: {
        type: Number,
        default: 1,
    },
});

const popupRef = shallowRef<InstanceType<typeof Popup>>();
const emit = defineEmits(["update:modelValue"]);
const activeLink = ref({
    type: "goods",
    list: [],
});
const goodsLists = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emit("update:modelValue", value);
    },
});

const handleConfirm = () => {
    emit("update:modelValue", activeLink.value);
};
const handleDelete = (index: number) => {
    if (props.modelValue?.length <= props.min) {
        return feedback.msgError(`最少保留${props.min}个`);
    }
    goodsLists.value.list.splice(index, 1);
};
</script>

<style lang="scss" scoped></style>
