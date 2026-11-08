<template>
    <div class="type-select-popup">
        <popup ref="popupRef" :title="'选择分类样式'" :async="true" width="1100px" @confirm="handleSubmit"
            @close="handleClose">
            <el-row>
                <el-col :span="6" v-for="(item, index) in imageArr" :key="index">
                    <!--  -->
                    <div class="relative mx-2 type-img" :class="{ 'type-active': classType == item.type }">
                        <div class="type-mask flex items-center justify-center" @click="classType = item.type">

                            <div class="operation-btn flex flex-col justify-center items-center">
                                <div class="text-white mt-4 cursor-pointer" @click.stop="previewImage = item.image">查看
                                </div>
                            </div>

                        </div>
                        <img :src="item.image" class="w-[250px] " :alt="`分类布局${index}`">
                    </div>
                    <div class="form-tips text-center mt-4">{{ item.name }}</div>
                </el-col>
            </el-row>

            <el-image-viewer v-if="previewImage.length" :url-list="[previewImage]" hide-on-click-modal
                @close="handleCloseImage" />
        </popup>
    </div>
</template>
<script setup>
// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref, shallowRef } from 'vue'
import No1 from "./images/No.1.png"
import No2 from "./images/No.2.png"
import No3 from "./images/No.3.png"
import No4 from "./images/No.4.png"
import Popup from "@/components/popup/index.vue";

const imageArr = [{
    image: No1,
    type: 1,
    name: '一级布局，适合商品分类较少情形'
}, {
    image: No2,
    type: 2,
    name: '一级布局，适合商品分类较少情形'
}, {
    image: No3,
    type: 3,
    name: '二级布局，适合商品分类适中情形'
}, {
    image: No4,
    type: 4,
    name: '三级布局，适合商品分类丰富情形'
}]

const classType = ref(1);
const previewImage = ref("")
const emit = defineEmits(["success", "close"]);
const popupRef = shallowRef(null);


const handleSubmit = async () => {
    popupRef.value?.close();
    emit("success", classType.value);
};

const open = (type) => {
    classType.value = type
    popupRef.value?.open();
};


const handleClose = () => {
    emit("close");
};

const handleCloseImage = () => {
    previewImage.value = ""
}

defineExpose({
    open
});
</script>

<style lang="scss" scoped>
.type-img {
    border-color: transparent;
    @apply border-2 border-solid;
}

.type-active {
    @apply border-2 border-solid border-primary;
}

.type-mask {
    position: absolute;
    width: 100%;
    z-index: 99;
    height: 436px;

    .operation-btn {
        visibility: hidden;
    }
}

.type-mask:hover {
    background-color: rgba($color: #000000, $alpha: 0.5);

    .operation-btn {
        visibility: visible;
    }
}
</style>