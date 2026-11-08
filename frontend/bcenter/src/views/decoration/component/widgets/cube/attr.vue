<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                图片魔方
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">图片风格</div>
            <el-form-item label="轮播风格">
                <el-radio-group v-model="content.image_type">
                    <el-radio :label="0" border class="mb-2 w-[100px]">1行1个</el-radio>
                    <el-radio :label="1" border class="mb-2 w-[100px]">1行2个</el-radio>
                    <el-radio :label="2" border class="mb-2 w-[100px]">1行3个</el-radio>
                    <el-radio :label="3" border class="w-[100px]">左1右2</el-radio>
                    <el-radio :label="4" border class="w-[100px]">左2右2</el-radio>
                    <el-radio :label="5" border class="w-[100px]">上1下2</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>图片布局</span>
            </div>

            <div class="flex-1 w-full cursor-pointer" v-if="content.image_type == 0">
                <div
                    @click="activeIndex = 0"
                    class="w-full bg-[#f7f7f7] box text-[#999]"
                    :class="{ active: activeIndex == 0 }"
                >
                    <div v-if="!content.data[0].image" class="h-[260px] leading-[260px]">
                        750*高度不限
                    </div>
                    <el-image
                        fit="cover"
                        :src="getImageUrl(content.data[0].image)"
                        v-if="content.data[0].image"
                    ></el-image>
                </div>
            </div>
            <div class="flex w-full cursor-pointer" v-if="content.image_type == 1">
                <div @click="activeIndex = 0" class="w-[50%]">
                    <div :class="{ active: activeIndex == 0 }">
                        <div
                            v-if="!content.data[0].image"
                            class="h-[260px] leading-[260px] box text-[#999] bg-[#f7f7f7]"
                        >
                            375*高度不限
                        </div>
                        <el-image
                            fit="cover"
                            :src="getImageUrl(content.data[0].image)"
                            v-if="content.data[0].image"
                        ></el-image>
                    </div>
                </div>
                <div @click="activeIndex = 1" class="w-[50%]">
                    <div :class="{ active: activeIndex == 1 }">
                        <div
                            v-if="!content.data[1].image"
                            class="h-[260px] leading-[260px] box text-[#999] bg-[#f7f7f7]"
                        >
                            375*高度不限
                        </div>
                        <el-image
                            fit="cover"
                            :src="getImageUrl(content.data[1].image)"
                            v-if="content.data[1].image"
                        ></el-image>
                    </div>
                </div>
            </div>
            <div class="flex w-full cursor-pointer" v-if="content.image_type == 2">
                <div class="w-1/3" @click="activeIndex = 0">
                    <div :class="{ active: activeIndex == 0 }">
                        <div
                            v-if="!content.data[0].image"
                            class="h-[158px] leading-[158px] box text-[#999] bg-[#f7f7f7]"
                        >
                            250*高度不限
                        </div>
                        <el-image
                            fit="cover"
                            :src="getImageUrl(content.data[0].image)"
                            v-if="content.data[0].image"
                        ></el-image>
                    </div>
                </div>

                <div class="w-1/3" @click="activeIndex = 1">
                    <div :class="{ active: activeIndex == 1 }">
                        <div
                            v-if="!content.data[1].image"
                            class="h-[158px] leading-[158px] box text-[#999] bg-[#f7f7f7]"
                        >
                            250*高度不限
                        </div>
                        <el-image
                            fit="cover"
                            :src="getImageUrl(content.data[1].image)"
                            v-if="content.data[1].image"
                        ></el-image>
                    </div>
                </div>
                <div class="w-1/3" @click="activeIndex = 2">
                    <div :class="{ active: activeIndex == 2 }">
                        <div
                            v-if="!content.data[2].image"
                            class="h-[158px] leading-[158px] box text-[#999] bg-[#f7f7f7]"
                        >
                            250*高度不限
                        </div>
                        <el-image
                            fit="cover"
                            :src="getImageUrl(content.data[2].image)"
                            v-if="content.data[2].image"
                        ></el-image>
                    </div>
                </div>
            </div>
            <div class="flex w-full cursor-pointer" v-if="content.image_type == 3">
                <div
                    @click="activeIndex = 0"
                    class="w-1/2 bg-[#f7f7f7] box-1 h-[260px] text-[#999]"
                    :class="{ active: activeIndex == 0 }"
                >
                    <div v-if="!content.data[0].image">375*375</div>
                    <el-image
                        class="h-[260px]"
                        fit="cover"
                        :src="getImageUrl(content.data[0].image)"
                        v-if="content.data[0].image"
                    ></el-image>
                </div>
                <div class="w-1/2">
                    <div
                        @click="activeIndex = 1"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 1 }"
                    >
                        <div v-if="!content.data[1].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[1].image)"
                            v-if="content.data[1].image"
                        ></el-image>
                    </div>
                    <div
                        @click="activeIndex = 2"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 2 }"
                    >
                        <div v-if="!content.data[2].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[2].image)"
                            v-if="content.data[2].image"
                        ></el-image>
                    </div>
                </div>
            </div>
            <div class="flex w-full cursor-pointer" v-if="content.image_type == 4">
                <div class="w-1/2">
                    <div
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        @click="activeIndex = 0"
                        :class="{ active: activeIndex == 0 }"
                    >
                        <div v-if="!content.data[0].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[0].image)"
                            v-if="content.data[0].image"
                        ></el-image>
                    </div>
                    <div
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 1 }"
                        @click="activeIndex = 1"
                    >
                        <div v-if="!content.data[1].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[1].image)"
                            v-if="content.data[1].image"
                        ></el-image>
                    </div>
                </div>
                <div class="w-1/2">
                    <div
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        @click="activeIndex = 2"
                        :class="{ active: activeIndex == 2 }"
                    >
                        <div v-if="!content.data[2].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[2].image)"
                            v-if="content.data[2].image"
                        ></el-image>
                    </div>
                    <div
                        @click="activeIndex = 3"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 3 }"
                    >
                        <div v-if="!content.data[3].image">375*375</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[3].image)"
                            v-if="content.data[3].image"
                        ></el-image>
                    </div>
                </div>
            </div>
            <div class="w-full cursor-pointer" v-if="content.image_type == 5">
                <div class="w-full">
                    <div
                        @click="activeIndex = 0"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 0 }"
                    >
                        <div v-if="!content.data[0].image">375*188</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[0].image)"
                            v-if="content.data[0].image"
                        ></el-image>
                    </div>
                </div>
                <div class="flex w-full">
                    <div
                        @click="activeIndex = 1"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 1 }"
                    >
                        <div v-if="!content.data[1].image">375*188</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[1].image)"
                            v-if="content.data[1].image"
                        ></el-image>
                    </div>
                    <div
                        @click="activeIndex = 2"
                        class="w-full bg-[#f7f7f7] box-2 h-[130px] text-[#999]"
                        :class="{ active: activeIndex == 2 }"
                    >
                        <div v-if="!content.data[2].image">375*188</div>
                        <el-image
                            class="h-[130px]"
                            fit="cover"
                            :src="getImageUrl(content.data[2].image)"
                            v-if="content.data[2].image"
                        ></el-image>
                    </div>
                </div>
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">
                <span>图片设置</span>
            </div>
            <div class="bg-[#f9f9f9] p-1">
                <el-form-item label="图片" class="mt-[18px]">
                    <material-picker
                        v-model="content.data[activeIndex].image"
                        upload-class="bg-body"
                        exclude-domain
                    />
                </el-form-item>
                <el-form-item class="mt-[18px]" label="图片链接">
                    <link-picker v-model="content.data[activeIndex].link" />
                </el-form-item>
            </div>
        </el-card>
    </el-form>
</template>
<script lang="ts" setup>
import feedback from '@/utils/feedback'
import type { PropType } from 'vue'
import type options from './options'
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
const activeIndex = ref(0)
const handleAdd = () => {
    props.content.data.push({
        image: '',
        name: '',
        link: {}
    })
}
const handleDelete = (index: number) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
:deep(.el-image) {
    display: block;
}
.box {
    border: 1px solid hsla(0, 0%, 89.8%, 0.9);
    text-align: center;
}
.box-1 {
    border: 1px solid hsla(0, 0%, 89.8%, 0.9);
    text-align: center;
    line-height: 260px;
}
.box-2 {
    border: 1px solid hsla(0, 0%, 89.8%, 0.9);
    text-align: center;
    line-height: 130px;
}
.active {
    border: 1px solid #4073fa;
}
</style>
