<template>
    <el-form-item label="商品货号" prop="code">
        <div class="w-80">
            <el-input
                v-model="formData.code"
                placeholder="请输入商品货号"
                type="text"
                show-word-limit
                clearable
            />
        </div>
    </el-form-item>
    <el-form-item label="商品名称" prop="name">
        <div class="w-80">
            <el-input
                v-model="formData.name"
                placeholder="请输入商品名称"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 2 }"
                maxlength="64"
                show-word-limit
                clearable
            />
        </div>
    </el-form-item>
    <el-form-item label="商品描述">
        <div class="w-80">
            <el-input
                v-model="formData.descript"
                placeholder="请输入商品描述"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 5 }"
                show-word-limit
                clearable
            />
        </div>
    </el-form-item>
    <el-form-item label="平台分类" prop="category_ids">
        <div class="flex">
            <el-cascader
                class="w-80"
                v-model="formData.category_ids"
                :options="shopCategoryList"
                :props="cascaderOpt"
                clearable
                filterable
            />
        </div>
    </el-form-item>
    <el-form-item label="商品分类" prop="shop_category_id">
        <el-select class="w-80" v-model="formData.shop_category_id">
            <el-option
                v-for="(itme, index) in otherList"
                :key="itme.id"
                :label="itme.name"
                :value="itme.id"
            ></el-option>
        </el-select>
        <div class="ml-4">
            <a href="category" target="_blank">
                <el-button type="primary" link size="small">新建分类</el-button>
            </a>
            <el-button type="primary" link size="small" @click="handleRefresh"
                >|&nbsp; 刷新</el-button
            >
        </div>
    </el-form-item>
    <el-form-item label="商品轮播图" prop="goods_image">
        <div>
            <material-picker v-model="formData.goods_image" :limit="9" />

            <div class="form-tips">
                建议尺寸：750*750,可拖拽改变图片顺序，默认首张图为主图，最多可上传9张
            </div>
        </div>
    </el-form-item>
    <el-form-item label="添加视频">
        <div class="w-80">
            <el-switch v-model="formData.video_is" :active-value="1" :inactive-value="0" />
        </div>
    </el-form-item>
    <template v-if="formData.video_is">
        <el-form-item label="视频来源" prop="video_from">
            <div class="w-80">
                <el-radio-group v-model="formData.video_from">
                    <el-radio :label="1">素材库</el-radio>
                    <el-radio :label="2">链接</el-radio>
                </el-radio-group>
            </div>
        </el-form-item>
        <el-form-item label="视频封面">
            <div class="w-80">
                <material-picker v-model="formData.video_cover" :limit="1" />
            </div>
        </el-form-item>
        <el-form-item label="选择视频" prop="video_uri" v-if="formData.video_from === 1">
            <div class="w-80">
                <material-picker v-model="formData.video_uri" type="video" :limit="1" />
            </div>
        </el-form-item>
        <el-form-item label="视频链接" prop="video_uri" v-if="formData.video_from === 2">
            <div class="w-80">
                <el-input
                    v-model="formData.video_uri"
                    placeholder="请输入或粘贴视频链接"
                    show-word-limit
                    clearable
                />
            </div>
        </el-form-item>
    </template>
</template>

<script lang="ts" setup>
import { getRoutePath } from '@/router'
import { useVModels } from '@vueuse/core'
import { getGoodsCategoryOtherLists, getShopCategoryLists } from '@/api/goods/category'
import useAppStore from '@/stores/modules/app'

const appStore = useAppStore()

const props = withDefaults(
    defineProps<{
        modelValue?: any
    }>(),
    {
        modelValue: {}
    }
)

const emit = defineEmits(['update:modelValue'])
const { modelValue: formData } = useVModels(props, emit)

const otherList = shallowRef([])
const shopCategoryList = shallowRef([])
const handleRefresh = () => {
    appStore.refreshView()
}
// 分类组件配置数据
const cascaderOpt = reactive({
    multiple: true,
    checkStrictly: true,
    label: 'name',
    value: 'id',
    children: 'sons',
    emitPath: false
})

const getOtherList = async () => {
    try {
        const data = await getGoodsCategoryOtherLists()
        otherList.value = data
    } catch (error) {
        console.log(error)
    }
}

const getShopCategoryList = async () => {
    try {
        const lists: any = await getShopCategoryLists()
        shopCategoryList.value = lists
    } catch (error) {
        console.log(error)
    }
}

getOtherList()
getShopCategoryList()
</script>

<style lang="scss"></style>
