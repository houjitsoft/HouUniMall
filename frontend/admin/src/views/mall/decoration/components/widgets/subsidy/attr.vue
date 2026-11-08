<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                国家补贴
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
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">补贴说明</div>
            <el-form-item label="提示文案">
                <el-input
                    v-model="content.subsidy_tip"
                    show-word-limit
                    maxlength="20"
                    placeholder="如：政府补贴 结算时自动抵扣"
                ></el-input>
                <div class="form-tips">
                    仅作静态展示，真实补贴金额与资格由后台 national-subsidy 插件按用户/地区/类目实时计算，以结算页为准
                </div>
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref, onMounted } from 'vue'

import { getGoodsCategoryLists } from '@/api/goods/category'
import { getGoodsLists } from '@/api/goods/goods'
const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    }
})
const handleSelect = async (e) => {
    const { lists } = await getGoodsLists({ category_id: e.id })
    props.content.goodsLists.list = lists
}
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
</style>
