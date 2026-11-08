<template>
    <el-form ref="form" label-width="80px" size="large">
        <el-card shadow="never" class="!border-none flex">
            <div
                class="title flex items-center before:w-[3px] before:h-[14px] before:block before:bg-primary before:mr-2 text-xl font-medium"
            >
                店铺列表
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">选择店铺</div>
            <div class="flex-1 w-full bg-[#f7f7f7] p-2 rounded-lg pt-6">
                <el-form-item label="店铺来源">
                    <div class="w-full">
                        <el-radio-group v-model="content.get_type">
                            <el-radio :label="1">自动获取</el-radio>
                            <el-radio :label="2">手动选择</el-radio>
                        </el-radio-group>
                        <store-select
                            v-model="content.storelists"
                            :multiple="true"
                            v-if="content.get_type == 2"
                        />
                    </div>
                </el-form-item>
                <el-form-item label="显示数量" v-if="content.get_type == 1">
                    <el-slider v-model="content.showNums" show-input :max="10" />
                </el-form-item>
            </div>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full" v-if="content.get_type == 1">
            <div class="mb-2 title flex items-center text-lg font-medium">店铺排序</div>
            <el-form-item label="排序方式">
                <div class="w-full">
                    <el-radio-group v-model="content.sort_type">
                        <el-radio :label="1">默认排序</el-radio>
                        <el-radio :label="2">自定义排序</el-radio>
                    </el-radio-group>
                    <div class="w-full">
                        <el-select
                            v-model="content.sort"
                            class="w-full"
                            v-if="content.sort_type == 2"
                        >
                            <el-option label="综合" value="" />
                            <el-option label="按销量" value="sale_desc" />
                            <el-option label="访问量" value="visit_desc" />
                            <el-option label="在售商品数" value="sale_in_goods" />
                            <el-option label="入驻时间升序" value="create_asc" />
                            <el-option label="入驻时间降序" value="create_desc" />
                            <el-option label="店铺成交转化率" value="sale_change" />
                        </el-select>
                    </div>
                </div>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">展示方式</div>
            <el-radio-group v-model="content.show_type">
                <el-radio :label="1" border class="w-[100px]">卡片</el-radio>
                <el-radio :label="2" border class="w-[100px]">列表</el-radio>
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
            <el-form-item label="标题文字" v-if="content.title_type == 1">
                <el-input
                    v-model="content.title"
                    placeholder="请输入标题文字"
                    show-word-limit
                    maxlength="6"
                ></el-input>
            </el-form-item>
        </el-card>
        <el-card shadow="never" class="!border-none flex mt-2 w-full">
            <div class="mb-2 title flex items-center text-lg font-medium">更多按钮</div>
            <el-form-item label="右侧按钮">
                <el-radio-group v-model="content.right_btn">
                    <el-radio :label="1">显示</el-radio>
                    <el-radio :label="0">隐藏</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="按钮文字">
                <el-input
                    v-model="content.right_title"
                    placeholder="请输入标题文字"
                    show-word-limit
                    maxlength="6"
                ></el-input>
            </el-form-item>
            <el-form-item label="按钮颜色">
                <color-picker v-model="content.right_color" defaultColor="#888888" />
            </el-form-item>
        </el-card>
    </el-form>
</template>
<script setup>// ⚠️ vue API 必须显式导入：缺失时构建期不报错，运行时报 'xxx is not defined' 导致页面白屏。
import { ref, onMounted } from 'vue'

import feedback from '@/utils/feedback'
import { getGoodsCategoryLists } from '@/api/goods/category'
import { getGoodsLists } from '@/api/goods/goods'
// import storeSelect from '@/components/store-select.vue'
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
const handleClick = (val) => {
    props.content.cart_style = val
}
const handleSelect = async (e) => {
    const { lists } = await getGoodsLists({ category_id: e.id })
    props.content.goodsLists.list = lists
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
