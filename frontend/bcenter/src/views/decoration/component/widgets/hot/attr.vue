<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                热卖推荐
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium justify-between">
                风格选择
                <el-button link type="primary" @click="dialogVisible = true">切换风格</el-button>
            </div>
            <div class="bg-[#f8f8f8] box">
                <!-- <img src="./images/style1.png" alt="" v-if="content.style == 1" /> -->
                <img src="./images/style2.png" alt="" v-if="content.style == 1" />
            </div>
            <el-dialog
                v-model="dialogVisible"
                title="选择风格"
                :before-close="handleClose"
                width="40%"
            >
                <div class="flex">
                    <!-- <div
                        class="inner-box text-center"
                        :class="{ active: content.style == 1 }"
                        @click="content.style = 1"
                    >
                        <img src="./images/style1.png" alt="" />
                        <div class="mt-2">风格1</div>
                    </div> -->
                    <div
                        class="inner-box text-center"
                        :class="{ active: content.style == 1 }"
                        @click="content.style = 1"
                    >
                        <img src="./images/style2.png" alt="" />
                        <div class="mt-2">风格1</div>
                    </div>
                </div>

                <template #footer>
                    <div class="dialog-footer">
                        <el-button @click="dialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="dialogVisible = false"> 确定 </el-button>
                    </div>
                </template>
            </el-dialog>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">主标题</div>
            <el-form-item label="主标题">
                <el-input
                    v-model="content.title"
                    placeholder="请输入名称"
                    maxlength="8"
                    show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="字体颜色">
                <color-picker v-model="content.title_color" defaultColor="#000000" />
            </el-form-item>
            <el-form-item label="字体大小">
                <el-slider v-model="content.title_size" show-input :max="32" />
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">副标题</div>
            <el-form-item label="副标题">
                <el-input
                    v-model="content.subtitle"
                    placeholder="请输入名称"
                    maxlength="10"
                    show-word-limit
                ></el-input>
            </el-form-item>
            <el-form-item label="字体颜色">
                <color-picker v-model="content.subtitle_color" defaultColor="#666666" />
            </el-form-item>
            <el-form-item label="字体大小">
                <el-slider v-model="content.subtitle_size" show-input :max="32" />
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择商品</div>
            <el-form-item label="商品来源">
                <el-radio-group v-model="content.data_source">
                    <el-radio :label="1">自动获取</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="显示数量">
                <el-slider v-model="content.show_nums" show-input :max="20" />
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">商品排序</div>
            <el-form-item label="排序方式">
                <el-radio-group v-model="content.sort_type">
                    <el-radio :label="1">默认排序</el-radio>
                    <el-radio :label="2">自定义排序</el-radio>
                </el-radio-group>
                <el-select v-model="content.use_sort" v-if="content.sort_type == 2" class="w-full">
                    <el-option label="综合" value="" />
                    <el-option label="按销量" value="sale_desc" />
                    <el-option label="访问量" value="visit_desc" />
                    <el-option label="价格降序" value=" price_desc" />
                    <el-option label="价格升序" value=" price_asc" />
                    <el-option label="成交转化率" value="sale_change" />
                </el-select>
            </el-form-item>
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
const activeIndex = ref(1)
const dialogVisible = ref(false)
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
.box {
    @apply flex items-center justify-center;
    padding: 15px;
    height: 200px;
    text-align: center;
    box-sizing: border-box;
}
.inner-box {
    background: #f9f9f9;
    padding: 20px 15px;
    margin-right: 20px;
    cursor: pointer;
    border: 1px solid transparent;
}
.active {
    border-color: #4073fa;
}
</style>
