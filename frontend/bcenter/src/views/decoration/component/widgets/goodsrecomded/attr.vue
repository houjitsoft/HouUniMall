<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                推荐商品
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示方式</div>
            <el-radio-group v-model="content.show_type">
                <el-radio :label="1" border class="mb-2 w-[100px]">风格1</el-radio>
            </el-radio-group>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">头部设置</div>
            <el-form-item label="标题类型">
                <div>
                    <el-radio-group v-model="content.title_type">
                        <el-radio :label="1">文字</el-radio>
                        <el-radio :label="2">图片</el-radio>
                    </el-radio-group>
                    <material-picker
                        v-model="content.titile_img"
                        upload-class="bg-body"
                        exclude-domain
                        v-if="content.title_type == 2"
                    />
                    <div class="form-tips" v-if="content.title_type == 2">
                        建议图片尺寸：56px*20px
                    </div>
                </div>
            </el-form-item>

            <el-form-item label="标题文字" v-if="content.title_type === 1">
                <el-input
                    v-model="content.title"
                    placeholder="请输入名称"
                    maxlength="6"
                    show-word-limit
                ></el-input>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择商品</div>
            <div class="flex-1 w-full bg-[#f7f7f7] p-2 rounded-lg pt-6">
                <el-form-item label="商品来源">
                    <div class="w-full">
                        <el-radio-group v-model="content.get_type">
                            <el-radio :label="1">手动选择</el-radio>
                        </el-radio-group>
                        <div class="flex-1 w-full">
                            <goods-select
                                v-model="content.goodsLists"
                                type="multiple"
                                v-if="content.get_type === 1"
                            ></goods-select>
                        </div>
                    </div>
                </el-form-item>
            </div>
        </el-card>
    </el-form>
</template>
<script lang="ts" setup>
import feedback from '@/utils/feedback'
import type { PropType } from 'vue'
import type options from './options'

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
</script>

<style lang="scss" scoped>
:deep(.el-card__body) {
    width: 100%;
}
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
.add-btn {
    width: 25px;
    height: 25px;
    line-height: 23px;
    border-radius: 50%;
    background-color: #1a69fc;
    text-align: center;
    color: white;
    font-size: 16px;
}
.cart-btn {
    width: 25px;
    height: 25px;
    border-radius: 50%;
    background-color: #1a69fc;
}
.border-box {
    border: 1px solid rgb(233, 233, 238);
    // border-color: inherit;
    cursor: pointer;
}
.active {
    border: 1px solid var(--el-color-primary);
}
</style>
