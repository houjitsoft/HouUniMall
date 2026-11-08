<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                商品列表
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择商品</div>
            <div class="flex-1 w-full bg-[#f7f7f7] p-2 rounded-lg pt-6">
                <el-form-item label="商品来源">
                    <div class="w-full">
                        <el-radio-group v-model="content.get_type">
                            <el-radio :label="1">手动选择</el-radio>
                            <el-radio :label="2">指定分类</el-radio>
                        </el-radio-group>
                        <div class="flex-1 w-full">
                            <goods-select
                                v-model="content.goodsLists"
                                v-if="content.get_type === 1"
                                type="multiple"
                            ></goods-select>
                            <el-tree-select
                                v-if="content.get_type === 2"
                                class="w-full"
                                v-model="content.category_id"
                                :data="categotyList"
                                check-strictly
                                :props="{
                                    label: 'name',
                                    children: 'sons',
                                    value: 'id'
                                }"
                                @node-click="handleSelect"
                            />
                        </div>
                    </div>
                </el-form-item>
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示方式</div>
            <el-radio-group v-model="content.show_type">
                <el-radio :label="1" border class="mb-2 w-[100px]">大图</el-radio>
                <el-radio :label="2" border class="mb-2 w-[100px]">单列</el-radio>
                <el-radio :label="3" border class="mb-2 w-[100px]">两列</el-radio>
                <el-radio :label="4" border class="mb-2 w-[100px]">三列</el-radio>
                <el-radio :label="5" border class="mb-2 w-[100px]">横向滑动</el-radio>
            </el-radio-group>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">显示内容</div>
            <el-checkbox-group v-model="content.show_content">
                <el-checkbox :label="1">商品标题</el-checkbox>
                <el-checkbox :label="2">商品价格</el-checkbox>
                <el-checkbox :label="3">商品划线价</el-checkbox>
                <el-checkbox :label="4">商品销量</el-checkbox>
                <el-checkbox
                    :label="5"
                    v-if="
                        content.show_type == 1 || content.show_type == 2 || content.show_type == 3
                    "
                    >商户名称</el-checkbox
                >
            </el-checkbox-group>
        </el-card>
        <!-- //TODO -->

        <!-- <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">购物车按钮</div>
            <el-form-item label="按钮状态">
                <el-radio-group v-model="content.cart_btn">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="按钮样式">
                <div>
                    <el-radio-group v-model="content.btn_type">
                        <el-radio :label="1">默认</el-radio>
                        <el-radio :label="2">自定义</el-radio>
                    </el-radio-group>
                    <div class="flex" v-if="content.btn_type == 1">
                        <div
                            class="border-box px-3 py-1 mr-2 rounded-sm"
                            :class="{
                                active: content.cart_style == 1
                            }"
                            @click="handleClick(1)"
                        >
                            <div class="add-btn">+</div>
                        </div>

                        <div
                            class="border-box px-3 py-1 rounded-sm"
                            @click="handleClick(2)"
                            :class="{
                                active: content.cart_style == 2
                            }"
                        >
                            <div class="flex justify-center items-center cart-btn">
                                <img src="./icon.png" />
                            </div>
                        </div>
                    </div>
                    <div class="" v-if="content.btn_type == 2">
                        <material-picker
                            v-model="content.cart_image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="form-tips">建议图片尺寸：20px*20px</div>
                    </div>
                </div>
            </el-form-item>
        </el-card> -->
    </el-form>
</template>
<script lang="ts" setup>
import feedback from '@/utils/feedback'
import type { PropType } from 'vue'
import type options from './options'
import { getGoodsCategoryLists } from '@/api/goods/category'
import { getGoodsLists } from '@/api/goods/goods'
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
const handleClick = (val: number) => {
    props.content.cart_style = val
}
const handleSelect = async (e: any) => {
    const { lists } = await getGoodsLists({ category_id: e.id })
    props.content.goodsLists = lists
}
const avtiveIndex = ref(0)
const categotyList = ref([])
onMounted(async () => {
    const { lists } = await getGoodsCategoryLists({
        page_no: 1,
        page_size: 999
    })
    categotyList.value = lists
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
