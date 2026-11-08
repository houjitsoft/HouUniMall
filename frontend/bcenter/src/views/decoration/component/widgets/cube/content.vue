<template>
    <!-- type==1 -->
    <div class="" v-if="content.image_type == 0">
        <el-image
            fit="cover"
            :src="getImageUrl(content.data[0].image)"
            v-if="content.data[0].image"
        >
        </el-image>
        <el-image v-else style="height: 375px; width: 100%">
            <template #error>
                <div class="w-full h-full flex justify-center items-center bg-white">
                    <el-icon :size="28"><PictureFilled /></el-icon>
                </div>
            </template>
        </el-image>
    </div>
    <!-- type==1 -->
    <div class="flex" v-if="content.image_type == 1">
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[0].image)"
                v-if="content.data[0].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[1].image)"
                v-if="content.data[1].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
    </div>
    <!-- type==2 -->
    <div class="flex" v-if="content.image_type == 2">
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[0].image)"
                v-if="content.data[0].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[1].image)"
                v-if="content.data[1].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[2].image)"
                v-if="content.data[2].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
    </div>
    <!-- type==3 -->
    <div class="flex h-[188px]" v-if="content.image_type == 3">
        <div class="w-full">
            <el-image
                fit="cover"
                :src="getImageUrl(content.data[0].image)"
                v-if="content.data[0].image"
            >
            </el-image>
            <el-image v-else style="height: 188px; width: 100%">
                <template #error>
                    <div class="w-full h-full flex justify-center items-center bg-white">
                        <el-icon :size="28"><PictureFilled /></el-icon>
                    </div>
                </template>
            </el-image>
        </div>
        <div class="w-full">
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[1].image)"
                    v-if="content.data[1].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[2].image)"
                    v-if="content.data[2].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
    </div>
    <!-- type==4-->
    <div class="flex h-[188px]" v-if="content.image_type == 4">
        <div class="w-full">
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[0].image)"
                    v-if="content.data[0].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[1].image)"
                    v-if="content.data[1].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
        <div class="w-full">
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[2].image)"
                    v-if="content.data[2].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[3].image)"
                    v-if="content.data[3].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
    </div>
    <!-- type==5-->
    <div class="h-[188px]" v-if="content.image_type == 5">
        <div class="w-full">
            <div class="h-[94px]">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[0].image)"
                    v-if="content.data[0].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
        <div class="w-full flex">
            <div class="h-[94px] w-full">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[1].image)"
                    v-if="content.data[1].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="h-[94px] w-full">
                <el-image
                    class="h-[94px]"
                    fit="cover"
                    :src="getImageUrl(content.data[2].image)"
                    v-if="content.data[2].image"
                >
                </el-image>
                <el-image v-else style="height: 94px; width: 100%">
                    <template #error>
                        <div class="w-full h-full flex justify-center items-center bg-white">
                            <el-icon :size="28"><PictureFilled /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type { PropType } from 'vue'
import type options from './options'
import DecorationImg from '../../decoration-img.vue'
import useAppStore from '@/stores/modules/app'

const { getImageUrl } = useAppStore()
type OptionsType = ReturnType<typeof options>
const props = defineProps({
    content: {
        type: Object as PropType<OptionsType['content']>,
        default: () => ({})
    },
    styles: {
        type: Object as PropType<OptionsType['styles']>,
        default: () => ({})
    }
})
const imageHeght = ref(170)
</script>

<style lang="scss" scoped>
:deep(.el-image) {
    display: block;
}
</style>
